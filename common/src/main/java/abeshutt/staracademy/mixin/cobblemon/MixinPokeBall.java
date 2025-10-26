package abeshutt.staracademy.mixin.cobblemon;

import abeshutt.staracademy.init.ModPokeBalls;
import com.cobblemon.mod.common.pokeball.PokeBall;
import com.cobblemon.mod.common.pokemon.Pokemon;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PokeBall.class)
public class MixinPokeBall {

    @Inject(method = "hpForCalculation(Lcom/cobblemon/mod/common/pokemon/Pokemon;)I", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void onHpForCalculation(Pokemon target, CallbackInfoReturnable<Integer> cir) {
        PokeBall ball = (PokeBall) (Object) this;
        
        // Check if this is any Safari Ball (original or our custom ones)
        boolean isSafariBall = false;
        
        // Direct comparison with our custom Safari Balls
        if (ball == ModPokeBalls.GREAT_SAFARI_BALL || ball == ModPokeBalls.GOLDEN_SAFARI_BALL) {
            isSafariBall = true;
        }
        // Check by name for the original Cobblemon Safari Ball using reflection
        else {
            try {
                java.lang.reflect.Field nameField = PokeBall.class.getDeclaredField("name");
                nameField.setAccessible(true);
                Identifier ballName = (Identifier) nameField.get(ball);
                
                if (ballName != null && ballName.getNamespace().equals("cobblemon") && ballName.getPath().equals("safari_ball")) {
                    isSafariBall = true;
                }
            } catch (Exception e) {
                // If reflection fails, continue without setting the flag
            }
        }
        
        if (isSafariBall) {
            // Safari Balls use full HP for catch rate calculation
            cir.setReturnValue(target.getStat(com.cobblemon.mod.common.api.pokemon.stats.Stats.HP));
        }
    }
}
