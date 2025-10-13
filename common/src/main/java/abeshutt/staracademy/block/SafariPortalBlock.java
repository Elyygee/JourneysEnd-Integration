package abeshutt.staracademy.block;

import abeshutt.staracademy.StarAcademyMod;
import abeshutt.staracademy.block.entity.SafariPortalBlockEntity;
import abeshutt.staracademy.init.ModConfigs;
import abeshutt.staracademy.init.ModWorldData;
import abeshutt.staracademy.util.ProxyEntity;
import abeshutt.staracademy.world.data.EntityState;
import abeshutt.staracademy.world.data.PortalAnchor;
import abeshutt.staracademy.world.data.SafariData;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Formatting;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.*;
import org.jetbrains.annotations.Nullable;

public class SafariPortalBlock extends Block implements BlockEntityProvider, Portal {

    public static final EnumProperty<Direction.Axis> AXIS = Properties.HORIZONTAL_AXIS;
    private static final boolean DEBUG_PORTAL = false; // Set to true for debugging
    protected static final VoxelShape X_SHAPE = Block.createCuboidShape(0.0, 0.0, 2.0, 16.0, 16.0, 14.0);
    protected static final VoxelShape Z_SHAPE = Block.createCuboidShape(2.0, 0.0, 0.0, 14.0, 16.0, 16.0);

    public SafariPortalBlock() {
        super(AbstractBlock.Settings.create()
                .noCollision()
                .strength(-1.0F)
                .sounds(BlockSoundGroup.GLASS)
                .luminance(state -> 11)
                .pistonBehavior(PistonBehavior.BLOCK));
        this.setDefaultState(this.stateManager.getDefaultState().with(AXIS, Direction.Axis.X));
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return switch(state.get(AXIS)) {
            case Z -> Z_SHAPE;
            default -> X_SHAPE;
        };
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState,
                                                WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        Direction.Axis axis = direction.getAxis();
        Direction.Axis axis2 = state.get(AXIS);
        boolean bl = axis2 != axis && axis.isHorizontal();
        return !bl && !neighborState.isOf(this) && !new SafariPortal(world, pos, axis2).wasAlreadyValid()
                ? Blocks.AIR.getDefaultState()
                : super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    // Helper method to avoid regressions if the constant is ever wrong
    public static boolean isSafariWorld(ServerWorld world) {
        var id = world.getRegistryKey().getValue();
        // prefer the constant, but also accept the id by value
        return world.getRegistryKey().equals(StarAcademyMod.SAFARI)
            || ("journeysend".equals(id.getNamespace()) && "safari".equals(id.getPath()));
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if(!(world instanceof ServerWorld serverWorld)) {
            return;
        }

        ProxyEntity proxy = ProxyEntity.of(entity).orElseThrow();

        if (DEBUG_PORTAL) {
            System.out.println("Journey's End: Portal collision detected - player: " + (entity instanceof ServerPlayerEntity) + 
                             ", canUsePortals: " + entity.canUsePortals(false) + 
                             ", hasCooldown: " + proxy.hasSafariPortalCooldown() + 
                             ", inPortal: " + proxy.isInSafariPortal() +
                             ", world: " + serverWorld.getRegistryKey().getValue() +
                             ", isSafari: " + isSafariWorld(serverWorld));
        }

        boolean voxelMatch = VoxelShapes.matchesAnywhere(
                VoxelShapes.cuboid(entity.getBoundingBox().offset(-pos.getX(), -pos.getY(), -pos.getZ())),
                state.getOutlineShape(world, pos),
                BooleanBiFunction.AND);
        
        if (DEBUG_PORTAL) {
            System.out.println("Journey's End: VoxelShapes match: " + voxelMatch + 
                             ", entityBB: " + entity.getBoundingBox() + 
                             ", portalShape: " + state.getOutlineShape(world, pos) +
                             ", portalAxis: " + state.get(AXIS));
        }

        if(entity instanceof ServerPlayerEntity player && entity.canUsePortals(false) && !proxy.hasSafariPortalCooldown()
                && !proxy.isInSafariPortal() && voxelMatch) {

            // Sanity check: ensure the block we collided with is our portal
            if (!(world.getBlockState(pos).getBlock() instanceof SafariPortalBlock)) {
                if (DEBUG_PORTAL) {
                    System.out.println("Journey's End: tryUsePortal pos is not SafariPortalBlock: " + world.getBlockState(pos));
                }
                return;
            }

            if (DEBUG_PORTAL) {
                System.out.println("Journey's End: Portal collision conditions met, processing teleportation");
            }

            // only remember entry portal if we're NOT in Safari right now
            if (!isSafariWorld(serverWorld)) {
                SafariData data = ModWorldData.SAFARI.getGlobal(serverWorld);
                SafariData.Entry entry = data.getOrCreate(player.getUuid());
                PortalAnchor anchor = new PortalAnchor(serverWorld.getRegistryKey(), pos.toImmutable(), state.get(AXIS));
                entry.setReturnPortal(anchor);
                data.markDirty();
                if (DEBUG_PORTAL) {
                    System.out.println("Journey's End: stored return portal " + anchor.dimension().getValue() + " @ " + anchor.framePos());
                }
            } else {
                if (DEBUG_PORTAL) {
                    System.out.println("Journey's End: In Safari world, skipping return portal storage");
                }
            }

            proxy.setInSafariPortal(true);

            if (isSafariWorld(serverWorld)) {
                // *** leaving Safari – do it manually ***
                if (DEBUG_PORTAL) {
                    System.out.println("Journey's End: Manual teleportation for leaving Safari");
                }

                // Use centralized logic but extract values manually to avoid TeleportTarget field access issues
                SafariData data = ModWorldData.SAFARI.getGlobal(serverWorld);
                SafariData.Entry entry = data.get(player.getUuid()).orElse(null);
                if (entry != null) {
                    PortalAnchor anchor = entry.getReturnPortal();
                    EntityState last = entry.getLastState();
                    
                    // Try anchor first
                    if (anchor != null) {
                        ServerWorld dest = serverWorld.getServer().getWorld(anchor.dimension());
                        if (dest != null) {
                            BlockPos frame = anchor.framePos();
                            if (dest.getBlockState(frame).getBlock() instanceof SafariPortalBlock) {
                                Direction normal = normalFromAxis(anchor.axis());
                                BlockPos front = frame.offset(normal);
                                BlockPos back = frame.offset(normal.getOpposite());
                                BlockPos spawnBlock = isOpen(dest, front) ? front : (isOpen(dest, back) ? back : frame);
                                Vec3d spawn = Vec3d.ofCenter(spawnBlock).add(0, -0.5, 0);
                                float yaw = normal.asRotation();
                                
                                if (DEBUG_PORTAL) {
                                    System.out.println("Journey's End: Manual teleport to " + dest.getRegistryKey().getValue() + " at " + spawn);
                                }
                                
                                // Teleport the player
                                player.teleport(dest, spawn.x, spawn.y, spawn.z, yaw, 0f);
                                
                                // Apply post-teleport actions
                                if (last != null && last.getGameMode() != null) {
                                    player.interactionManager.changeGameMode(last.getGameMode());
                                }
                                ProxyEntity.of(player).ifPresent(pxy -> {
                                    pxy.setSafariPortalCooldown(true);
                                    pxy.setInSafariPortal(false);
                                });
                                player.setPortalCooldown(20);
                                
                                if (DEBUG_PORTAL) {
                                    System.out.println("Journey's End: Manual teleportation completed successfully");
                                }
                                return;
                            }
                        }
                    }
                    
                    // Fallback to last state
                    if (last != null) {
                        ServerWorld dl = serverWorld.getServer().getWorld(last.getDimension());
                        if (dl != null) {
                            if (DEBUG_PORTAL) {
                                System.out.println("Journey's End: Manual teleport to last state " + dl.getRegistryKey().getValue() + " at " + last.getPos());
                            }
                            
                            player.teleport(dl, last.getPos().x, last.getPos().y, last.getPos().z, last.getYaw(), last.getPitch());
                            
                            // Apply post-teleport actions
                            if (last.getGameMode() != null) {
                                player.interactionManager.changeGameMode(last.getGameMode());
                            }
                            ProxyEntity.of(player).ifPresent(pxy -> {
                                pxy.setSafariPortalCooldown(true);
                                pxy.setInSafariPortal(false);
                            });
                            player.setPortalCooldown(20);
                            
                            if (DEBUG_PORTAL) {
                                System.out.println("Journey's End: Manual teleportation to last state completed successfully");
                            }
                            return;
                        }
                    }
                }
                
                // ensure we don't soft-lock reentry if resolution failed
                ProxyEntity.of(player).ifPresent(pxy -> pxy.setInSafariPortal(false));
                if (DEBUG_PORTAL) {
                    System.out.println("Journey's End: Manual teleportation failed, falling back to vanilla");
                }
            }

            // vanilla path (entering Safari, or fallback if manual failed)
            if (DEBUG_PORTAL) {
                System.out.println("Journey's End: Using vanilla tryUsePortal");
            }
            entity.tryUsePortal(this, pos);
            // Note: tryUsePortal returns void, so we can't check success/failure directly
            // The portal cooldown and in-portal flags will prevent immediate re-entry
        } else {
            if (DEBUG_PORTAL) {
                System.out.println("Journey's End: Portal collision conditions NOT met");
            }
        }
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        for(int i = 0; i < 4; i++) {
            double posX = (double)pos.getX() + random.nextDouble();
            double posY = (double)pos.getY() + random.nextDouble();
            double posZ = (double)pos.getZ() + random.nextDouble();
            double velocityX = ((double)random.nextFloat() - 0.5) * 0.5;
            double velocityY = ((double)random.nextFloat() - 0.5) * 0.5;
            double velocityZ = ((double)random.nextFloat() - 0.5) * 0.5;
            int direction = random.nextInt(2) * 2 - 1;

            if(!world.getBlockState(pos.west()).isOf(this) && !world.getBlockState(pos.east()).isOf(this)) {
                posX = (double)pos.getX() + 0.5 + 0.25 * (double)direction;
                velocityX = random.nextFloat() * 2.0F * (float)direction;
            } else {
                posZ = (double)pos.getZ() + 0.5 + 0.25 * (double)direction;
                velocityZ = random.nextFloat() * 2.0F * (float)direction;
            }

            world.addParticle(ParticleTypes.ASH, posX, posY, posZ, velocityX, velocityY, velocityZ);
        }
    }

    @Override
    public ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state) {
        return ItemStack.EMPTY;
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return switch(rotation) {
            case COUNTERCLOCKWISE_90, CLOCKWISE_90 -> switch(state.get(AXIS)) {
                case Z -> state.with(AXIS, Direction.Axis.X);
                case X -> state.with(AXIS, Direction.Axis.Z);
                default -> state;
            };
            default -> state;
        };
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(AXIS);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new SafariPortalBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return (world1, pos, state1, blockEntity) -> {
            if(blockEntity instanceof SafariPortalBlockEntity portal) {
                portal.tick();
            }
        };
    }

    @Override
    public TeleportTarget createTeleportTarget(ServerWorld world, Entity entity, BlockPos pos) {
        if (DEBUG_PORTAL) {
            System.out.println("Journey's End: createTeleportTarget called at " + world.getRegistryKey().getValue() + " pos " + pos);
        }
        if (!(entity instanceof ServerPlayerEntity player)) return null;

        SafariData data = ModWorldData.SAFARI.getGlobal(world.getServer());
        boolean inSafari = isSafariWorld(world);

        if (DEBUG_PORTAL) {
            System.out.println("Journey's End: createTeleportTarget called - player: " + player.getName().getString() + 
                             ", world: " + world.getRegistryKey().getValue() + 
                             ", inSafari: " + inSafari + 
                             ", pos: " + pos);
        }

        if (inSafari) {
            // Leave Safari → central logic
            return computeReturnTarget(world, player);
        } else {
            // ENTERING SAFARI
            SafariData.Entry entry = data.getOrCreate(player.getUuid());
            boolean isOp = world.getServer() != null && world.getServer().getPlayerManager().isOperator(player.getGameProfile());
            if (!entry.isUnlocked() && !isOp) {
                player.sendMessage(Text.translatable("text.journeysend.safari.enter_locked").formatted(Formatting.RED), true);
                return null;
            } else if (entry.getTimeLeft() <= 0) {
                player.sendMessage(Text.translatable("text.journeysend.safari.enter_no_time").formatted(Formatting.RED), true);
                return null;
            } else if (data.isPaused()) {
                player.sendMessage(Text.translatable("text.journeysend.safari.enter_paused").formatted(Formatting.RED), true);
                return null;
            }

            EntityState lastState = new EntityState(player);
            entry.setLastState(lastState);
            data.markDirty();

            BlockPos target = ModConfigs.SAFARI.getPlacementOffset().add(ModConfigs.SAFARI.getRelativeSpawnPosition());
            ServerWorld destination = world.getServer().getWorld(StarAcademyMod.SAFARI);
            if (destination == null) {
                System.err.println("Journey's End: SAFARI world not found for key " + StarAcademyMod.SAFARI.getValue());
                return null;
            }
            return new TeleportTarget(
                destination,
                new Vec3d(target.getX() + 0.5D, target.getY(), target.getZ() + 0.5D),
                Vec3d.ZERO,
                ModConfigs.SAFARI.getSpawnYaw(),
                ModConfigs.SAFARI.getSpawnPitch(),
                post -> {}
            );
        }
    }

    // --- helpers ---
    private static Direction normalFromAxis(Direction.Axis axis) {
        // Your portal shapes: X_SHAPE is thin on Z ⇒ plane normal is Z; Z_SHAPE thin on X ⇒ normal is X
        return axis == Direction.Axis.X ? Direction.SOUTH : Direction.EAST; // base normal; we'll choose side with air anyway
    }

    private static boolean isOpen(ServerWorld world, BlockPos pos) {
        return world.isAir(pos) && world.isAir(pos.up()); // 2-block tall space
    }

    // Centralized: figure out where to go when leaving Safari
    private @Nullable TeleportTarget computeReturnTarget(ServerWorld safariWorld, ServerPlayerEntity player) {
        SafariData data = ModWorldData.SAFARI.getGlobal(safariWorld.getServer());
        SafariData.Entry entry = data.get(player.getUuid()).orElse(null);
        if (entry == null) return null;

        PortalAnchor anchor = entry.getReturnPortal();
        EntityState last = entry.getLastState();

        // Prefer the saved frame anchor
        if (anchor != null) {
            ServerWorld dest = safariWorld.getServer().getWorld(anchor.dimension());
            if (dest != null) {
                BlockPos frame = anchor.framePos();
                if (dest.getBlockState(frame).getBlock() instanceof SafariPortalBlock) {
                    Direction normal = normalFromAxis(anchor.axis());
                    BlockPos front = frame.offset(normal);
                    BlockPos back = frame.offset(normal.getOpposite());
                    BlockPos spawnBlock = isOpen(dest, front) ? front : (isOpen(dest, back) ? back : frame);
                    Vec3d spawn = Vec3d.ofCenter(spawnBlock).add(0, -0.5, 0);
                    float yaw = normal.asRotation();
                    return makeTarget(dest, spawn, yaw, 0f, last);
                }
            }
        }

        // Fallback to last known state
        if (last != null) {
            ServerWorld dl = safariWorld.getServer().getWorld(last.getDimension());
            if (dl != null) {
                return makeTarget(dl, last.getPos(), last.getYaw(), last.getPitch(), last);
            }
        }
        return null;
    }

    // Build a TeleportTarget that also restores gamemode + sets cooldown via postTeleport
    private static TeleportTarget makeTarget(ServerWorld dest, Vec3d pos, float yaw, float pitch, EntityState lastOrNull) {
        return new TeleportTarget(
            dest,
            pos,
            Vec3d.ZERO,
            yaw,
            pitch,
            entity -> {
                if (entity instanceof ServerPlayerEntity sp) {
                    if (lastOrNull != null && lastOrNull.getGameMode() != null) {
                        sp.interactionManager.changeGameMode(lastOrNull.getGameMode());
                    }
                    ProxyEntity.of(sp).ifPresent(pxy -> pxy.setSafariPortalCooldown(true));
                    sp.setPortalCooldown(20);
                }
            }
        );
    }

}
