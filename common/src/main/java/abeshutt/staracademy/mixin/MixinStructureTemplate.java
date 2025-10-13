package abeshutt.staracademy.mixin;

import abeshutt.staracademy.util.ProxyStructureTemplate;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mixin(StructureTemplate.class)
public abstract class MixinStructureTemplate implements ProxyStructureTemplate {

    @Unique private boolean custom;
    @Unique private Map<BlockPos, StructureTemplate.StructureBlockInfo> blockCache;
    @Unique private final Object lock = new Object();

    @Shadow private Vec3i size;
    @Shadow @Final private List<StructureTemplate.PalettedBlockInfoList> blockInfoLists;
    @Shadow @Final private List<StructureTemplate.StructureEntityInfo> entities;

    @Override
    public boolean isCustom() {
        return this.custom;
    }

    @Override
    public void setCustom(boolean custom) {
        this.custom = custom;
    }

    @Override
    public StructureTemplate.StructureBlockInfo get(BlockPos pos) {
        synchronized(this.lock) {
            if(this.blockCache == null) {
                this.blockCache = new HashMap<>();

                for(StructureTemplate.PalettedBlockInfoList list : this.blockInfoLists) {
                    for(StructureTemplate.StructureBlockInfo entry : list.getAll()) {
                        this.blockCache.put(entry.pos(), entry);
                    }
                }
            }
        }

        return this.blockCache.get(pos);
    }

    @Shadow protected abstract void addEntitiesFromWorld(World world, BlockPos firstCorner, BlockPos secondCorner);
    @Shadow private static void categorize(StructureTemplate.StructureBlockInfo bi, List<StructureTemplate.StructureBlockInfo> a, List<StructureTemplate.StructureBlockInfo> b, List<StructureTemplate.StructureBlockInfo> c) {}
    @Shadow private static List<StructureTemplate.StructureBlockInfo> combineSorted(List<StructureTemplate.StructureBlockInfo> a, List<StructureTemplate.StructureBlockInfo> b, List<StructureTemplate.StructureBlockInfo> c) { return null; }

    // Runs before StructureTemplate reads anything
    @Inject(method = "readNbt", at = @At("HEAD"), require = 0)
    private void JE_readNbt(@Coerce Object lookup, NbtCompound nbt, CallbackInfo ci) {
        // Helper that renames block IDs inside a palette list (list of compounds)
        java.util.function.Consumer<NbtList> patchPalette = (paletteList) -> {
            for (int i = 0; i < paletteList.size(); i++) {
                NbtCompound blockNbt = paletteList.getCompound(i); // each entry is a compound
                if (blockNbt.contains("Name", 8)) { // 8 = string
                    String name = blockNbt.getString("Name");
                    if ("academy:safari_portal_frame".equals(name)) {
                        blockNbt.putString("Name", "journeysend:safari_portal_frame");
                    } else if ("academy:safari_portal".equals(name)) {
                        blockNbt.putString("Name", "journeysend:safari_portal");
                    }
                }
            }
        };

        // Format A: single "palette": List<Compound>
        if (nbt.contains("palette", 9)) { // 9 = list
            NbtList palette = nbt.getList("palette", 10); // 10 = compound
            patchPalette.accept(palette);
        }

        // Format B: "palettes": List<List<Compound>>
        if (nbt.contains("palettes", 9)) {
            NbtList palettesOuter = nbt.getList("palettes", 9); // list of lists
            for (int p = 0; p < palettesOuter.size(); p++) {
                // inner list is also type 9; tell MC we expect compounds (10) inside it
                NbtList inner = (NbtList) palettesOuter.get(p);
                // Some serializers store inner as a raw ListTag; be explicit:
                // If it's not compounds, skip gracefully
                if (inner.getHeldType() == 10 || inner.size() == 0) {
                    patchPalette.accept(inner);
                } else {
                    // try coercing in case getHeldType() is unreliable
                    try { patchPalette.accept(nbt.getList("palettes", 9).getList(p)); } catch (Throwable ignored) {}
                }
            }
        }
    }

    // 2) Your custom save logic (unchanged, but keep require=0)
    @Inject(method = "saveFromWorld", at = @At("HEAD"), cancellable = true, require = 0)
    private void JE_saveFromWorld(World world, BlockPos start, Vec3i dimensions, boolean includeEntities, Block ignoredBlock, CallbackInfo ci) {
        if (!this.custom) return;
        if (dimensions.getX() < 1 || dimensions.getY() < 1 || dimensions.getZ() < 1) { ci.cancel(); return; }

        BlockPos end = start.add(dimensions).add(-1, -1, -1);
        List<StructureTemplate.StructureBlockInfo> full = Lists.newArrayList();
        List<StructureTemplate.StructureBlockInfo> withNbt = Lists.newArrayList();
        List<StructureTemplate.StructureBlockInfo> other = Lists.newArrayList();

        BlockPos min = new BlockPos(Math.min(start.getX(), end.getX()), Math.min(start.getY(), end.getY()), Math.min(start.getZ(), end.getZ()));
        BlockPos max = new BlockPos(Math.max(start.getX(), end.getX()), Math.max(start.getY(), end.getY()), Math.max(start.getZ(), end.getZ()));
        this.size = dimensions;

        for (BlockPos worldPos : BlockPos.iterate(min, max)) {
            BlockPos rel = worldPos.subtract(min);
            BlockState state = world.getBlockState(worldPos);
            if ((ignoredBlock != null && state.isOf(ignoredBlock)) || state.isAir()) continue;

            BlockEntity be = world.getBlockEntity(worldPos);
            StructureTemplate.StructureBlockInfo info = be != null
                    ? new StructureTemplate.StructureBlockInfo(rel, state, be.createNbtWithId(world.getRegistryManager()))
                    : new StructureTemplate.StructureBlockInfo(rel, state, null);
            categorize(info, full, withNbt, other);
        }

        List<StructureTemplate.StructureBlockInfo> combined = combineSorted(full, withNbt, other);
        this.blockInfoLists.clear();
        this.blockInfoLists.add(new StructureTemplate.PalettedBlockInfoList(combined));
        if (includeEntities) this.addEntitiesFromWorld(world, min, max);
        else this.entities.clear();

        ci.cancel();
    }

}
