package abeshutt.staracademy.mixin;

import abeshutt.staracademy.block.SafariPortal;
import abeshutt.staracademy.init.ModBlocks;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(AbstractFireBlock.class)
public class MixinAbstractFireBlock {

    @Inject(method = "onBlockAdded", at = @At("HEAD"), cancellable = true)
    private void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify, CallbackInfo ci) {
        if(oldState.isOf(state.getBlock())) {
            return;
        }

        Optional<SafariPortal> optional = SafariPortal.getNewPortal(world, pos, Direction.Axis.X);

        if(optional.isPresent()) {
            optional.get().createPortal();
            ci.cancel();
        }
    }

    @Inject(method = "shouldLightPortalAt", at = @At("HEAD"), cancellable = true)
    private static void shouldLightPortalAt(World world, BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> ci) {
        BlockPos.Mutable mutable = pos.mutableCopy();
        boolean hasPortalFrame = false;

        for(Direction facing : Direction.values()) {
            if(world.getBlockState(mutable.set(pos).move(facing)).isOf(ModBlocks.SAFARI_PORTAL_FRAME.get())) {
                hasPortalFrame = true;
                break;
            }
        }

        if(hasPortalFrame) {
            Direction.Axis axis = direction.getAxis().isHorizontal() ? direction.rotateYCounterclockwise().getAxis()
                    : Direction.Type.HORIZONTAL.randomAxis(world.random);
            Optional<SafariPortal> optional = SafariPortal.getNewPortal(world, pos, axis);

            if(optional.isPresent()) {
                ci.setReturnValue(true);
            }
        }
    }

}
