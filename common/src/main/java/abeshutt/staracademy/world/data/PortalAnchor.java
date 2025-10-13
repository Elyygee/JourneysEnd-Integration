package abeshutt.staracademy.world.data;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;

public record PortalAnchor(RegistryKey<World> dimension, BlockPos framePos, Direction.Axis axis) {}
