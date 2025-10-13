package abeshutt.staracademy.world.data;

import abeshutt.staracademy.data.adapter.Adapters;
import abeshutt.staracademy.data.bit.BitBuffer;
import abeshutt.staracademy.data.serializable.ISerializable;
import com.google.gson.JsonObject;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;

import java.util.Optional;

import static abeshutt.staracademy.data.adapter.basic.EnumAdapter.Mode.NAME;

public class EntityState implements ISerializable<NbtCompound, JsonObject> {

    private double posX;
    private double posY;
    private double posZ;
    private float yaw;
    private float pitch;
    private GameMode gameMode;
    private RegistryKey<World> dimension;

    public EntityState() {

    }

    public EntityState(ServerPlayerEntity entity) {
        this(entity.getPos().x, entity.getPos().y, entity.getPos().z, entity.getYaw(), entity.getPitch(),
                entity.interactionManager.getGameMode(), entity.getWorld().getRegistryKey());
    }

    public EntityState(double posX, double posY, double posZ, float yaw, float pitch, GameMode gameMode, RegistryKey<World> dimension) {
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
        this.yaw = yaw;
        this.pitch = pitch;
        this.gameMode = gameMode;
        this.dimension = dimension;
    }

    public Vec3d getPos() {
        return new Vec3d(this.posX, this.posY, this.posZ);
    }

    public double getPosX() {
        return this.posX;
    }

    public void setPosX(double posX) {
        this.posX = posX;
    }

    public double getPosY() {
        return this.posY;
    }

    public void setPosY(double posY) {
        this.posY = posY;
    }

    public double getPosZ() {
        return this.posZ;
    }

    public void setPosZ(double posZ) {
        this.posZ = posZ;
    }

    public float getYaw() {
        return this.yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public float getPitch() {
        return this.pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public GameMode getGameMode() {
        return this.gameMode;
    }

    public void setGameMode(GameMode gameMode) {
        this.gameMode = gameMode;
    }

    public RegistryKey<World> getDimension() {
        return this.dimension;
    }

    public void setDimension(RegistryKey<World> dimension) {
        this.dimension = dimension;
    }

    @Override
    public void writeBits(BitBuffer buffer) {
        Adapters.DOUBLE.writeBits(this.posX, buffer);
        Adapters.DOUBLE.writeBits(this.posY, buffer);
        Adapters.DOUBLE.writeBits(this.posZ, buffer);
        Adapters.FLOAT.writeBits(this.yaw, buffer);
        Adapters.FLOAT.writeBits(this.pitch, buffer);
        Adapters.ofEnum(GameMode.class, NAME).writeBits(this.gameMode, buffer);
        Adapters.IDENTIFIER.writeBits(this.dimension.getValue(), buffer);
    }

    @Override
    public void readBits(BitBuffer buffer) {
        this.posX = Adapters.DOUBLE.readBits(buffer).orElseThrow();
        this.posY = Adapters.DOUBLE.readBits(buffer).orElseThrow();
        this.posZ = Adapters.DOUBLE.readBits(buffer).orElseThrow();
        this.yaw = Adapters.FLOAT.readBits(buffer).orElseThrow();
        this.pitch = Adapters.FLOAT.readBits(buffer).orElseThrow();
        this.gameMode = Adapters.ofEnum(GameMode.class, NAME).readBits(buffer).orElseThrow();
        this.dimension = RegistryKey.of(RegistryKeys.WORLD, Adapters.IDENTIFIER.readBits(buffer).orElseThrow());
    }

    @Override
    public Optional<NbtCompound> writeNbt() {
        return Optional.of(new NbtCompound()).map(nbt -> {
            Adapters.DOUBLE.writeNbt(this.posX).ifPresent(tag -> nbt.put("posX", tag));
            Adapters.DOUBLE.writeNbt(this.posY).ifPresent(tag -> nbt.put("posY", tag));
            Adapters.DOUBLE.writeNbt(this.posZ).ifPresent(tag -> nbt.put("posZ", tag));
            Adapters.FLOAT.writeNbt(this.yaw).ifPresent(tag -> nbt.put("yaw", tag));
            Adapters.FLOAT.writeNbt(this.pitch).ifPresent(tag -> nbt.put("pitch", tag));
            Adapters.ofEnum(GameMode.class, NAME).writeNbt(this.gameMode)
                    .ifPresent(tag -> nbt.put("gameMode", tag));
            Adapters.IDENTIFIER.writeNbt(this.dimension.getValue()).ifPresent(tag -> nbt.put("dimension", tag));
            return nbt;
        });
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        this.posX = Adapters.DOUBLE.readNbt(nbt.get("posX")).orElseThrow();
        this.posY = Adapters.DOUBLE.readNbt(nbt.get("posY")).orElseThrow();
        this.posZ = Adapters.DOUBLE.readNbt(nbt.get("posZ")).orElseThrow();
        this.yaw = Adapters.FLOAT.readNbt(nbt.get("yaw")).orElseThrow();
        this.pitch = Adapters.FLOAT.readNbt(nbt.get("pitch")).orElseThrow();
        this.gameMode = Adapters.ofEnum(GameMode.class, NAME).readNbt(nbt.get("gameMode")).orElseThrow();
        Identifier dimensionId = Adapters.IDENTIFIER.readNbt(nbt.get("dimension")).orElseThrow();
        
        // Migration: Convert academy:safari to journeysend:safari
        if("academy".equals(dimensionId.getNamespace()) && "safari".equals(dimensionId.getPath())) {
            dimensionId = Identifier.of("journeysend", "safari");
        }
        
        this.dimension = RegistryKey.of(RegistryKeys.WORLD, dimensionId);
    }

}
