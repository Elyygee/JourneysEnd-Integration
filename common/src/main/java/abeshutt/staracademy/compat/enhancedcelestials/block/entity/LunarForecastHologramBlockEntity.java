package abeshutt.staracademy.compat.enhancedcelestials.block.entity;

import abeshutt.staracademy.compat.enhancedcelestials.block.LunarForecastHologramBlock;
import abeshutt.staracademy.compat.enhancedcelestials.EnhancedCelestialsCompat;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public class LunarForecastHologramBlockEntity extends BlockEntity {
    private int idx;
    private int ticks;

    public LunarForecastHologramBlockEntity(BlockPos pos, BlockState state) {
        super(EnhancedCelestialsCompat.INSTANCE.getLUNAR_FORECAST_HOLOGRAM_BLOCK_ENTITY().get(), pos, state);
    }

    @Override
    public BlockEntityUpdateS2CPacket toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        nbt.putInt("idx", this.idx);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        this.idx = nbt.getInt("idx");
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
        return createNbt(registryLookup);
    }

    public static void serverTick(World world, BlockPos pos, BlockState state, LunarForecastHologramBlockEntity hologramBlockEntity) {
        if (world.isClient || state.get(LunarForecastHologramBlock.LIT)) {
            return;
        }
        hologramBlockEntity.ticks++;

        if (hologramBlockEntity.ticks % 20 == 0) { // Default 1 second
            hologramBlockEntity.next();
        }
    }

    public void next() {
        if (world == null) return;
        if (world.isClient) return;

        try {
            // Use reflection to access Enhanced Celestials API
            Class<?> enhancedCelestialsClass = Class.forName("dev.corgitaco.enhancedcelestials.EnhancedCelestials");
            var lunarForecastWorldDataMethod = enhancedCelestialsClass.getMethod("lunarForecastWorldData", World.class);
            var lunarDataOptional = lunarForecastWorldDataMethod.invoke(null, world);
            
            if (lunarDataOptional instanceof java.util.Optional<?> optional && optional.isPresent()) {
                var lunarData = optional.get();
                var currentLunarEventHolderMethod = lunarData.getClass().getMethod("currentLunarEventHolder");
                var holder = currentLunarEventHolderMethod.invoke(lunarData);
                
                var getForecastMethod = lunarData.getClass().getMethod("getForecast");
                var getCurrentDayMethod = lunarData.getClass().getMethod("getCurrentDay");
                
                @SuppressWarnings("unchecked")
                List<Object> forecast = (List<Object>) getForecastMethod.invoke(lunarData);
                int currentDay = (int) getCurrentDayMethod.invoke(lunarData);
                
                int size = forecast.size();
                int nextIdx = (this.idx + 1) % (size - 1);

                if (forecast.isEmpty()) {
                    this.idx = -1;
                    sync();
                    return;
                }

                // Check if first event is too far in the future
                var firstEvent = forecast.get(0);
                var getDaysUntilMethod = firstEvent.getClass().getMethod("getDaysUntil", int.class);
                int daysUntil = (int) getDaysUntilMethod.invoke(firstEvent, currentDay);
                
                if (daysUntil >= 7) { // Default 7 days
                    this.idx = -1;
                    sync();
                    return;
                }

                var lunarEventInstance = forecast.get(nextIdx);
                int daysUntilNext = (int) getDaysUntilMethod.invoke(lunarEventInstance, currentDay);
                
                if (daysUntilNext <= 7) { // Default 7 days
                    this.idx = nextIdx;
                    sync();
                } else {
                    this.idx = 0;
                    sync();
                }
            }
        } catch (Exception e) {
            System.err.println("Journey's End: Failed to update hologram due to Enhanced Celestials API access error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void sync() {
        if (world instanceof ServerWorld serverWorld) {
            serverWorld.getChunkManager().markForUpdate(getPos());
        }
    }

    public int getIdx() {
        return idx;
    }
}
