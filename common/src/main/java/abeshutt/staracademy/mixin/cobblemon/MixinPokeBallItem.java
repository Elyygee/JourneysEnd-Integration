package abeshutt.staracademy.mixin.cobblemon;

import abeshutt.staracademy.StarAcademyMod;
import abeshutt.staracademy.init.ModItems;
import com.cobblemon.mod.common.CobblemonItems;
import com.cobblemon.mod.common.item.PokeBallItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PokeBallItem.class)
public abstract class MixinPokeBallItem {

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void throwPokeBall(World world, PlayerEntity player, Hand usedHand, CallbackInfoReturnable<TypedActionResult<ItemStack>> ci) {
        PokeBallItem item = (PokeBallItem) (Object) this;
        
        // Check if this is any Safari Ball (Cobblemon's default or our custom ones)
        boolean isSafariBall = item == CobblemonItems.SAFARI_BALL 
            || item == ModItems.GREAT_SAFARI_BALL.get() 
            || item == ModItems.GOLDEN_SAFARI_BALL.get();
        
        // Safari Balls can only be used in Safari dimension
        // Other PokeBalls cannot be used in Safari dimension
        if(world.getRegistryKey() == StarAcademyMod.SAFARI && !isSafariBall
            || world.getRegistryKey() != StarAcademyMod.SAFARI && isSafariBall) {
            ci.setReturnValue(TypedActionResult.success(player.getStackInHand(usedHand), world.isClient));
        }
    }

}
