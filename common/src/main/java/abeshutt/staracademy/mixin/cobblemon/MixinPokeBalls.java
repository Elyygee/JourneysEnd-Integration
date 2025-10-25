package abeshutt.staracademy.mixin.cobblemon;

import abeshutt.staracademy.init.ModPokeBalls;
import com.cobblemon.mod.common.api.pokeball.PokeBalls;
import com.cobblemon.mod.common.pokeball.PokeBall;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PokeBalls.class)
public class MixinPokeBalls {

    @Inject(method = "<clinit>", at = @At("TAIL"), remap = false)
    private static void clinit(CallbackInfo ci) {
        // Add our custom Safari Balls to the defaults map after Cobblemon's init block
        try {
            java.lang.reflect.Field defaultsField = PokeBalls.class.getDeclaredField("defaults");
            defaultsField.setAccessible(true);
            java.util.Map defaults = (java.util.Map) defaultsField.get(null);
            
            // Use reflection to get the name field from PokeBall
            java.lang.reflect.Field nameField = PokeBall.class.getDeclaredField("name");
            nameField.setAccessible(true);
            
            Object greatName = nameField.get(ModPokeBalls.GREAT_SAFARI_BALL);
            Object goldenName = nameField.get(ModPokeBalls.GOLDEN_SAFARI_BALL);
            
            defaults.put(greatName, ModPokeBalls.GREAT_SAFARI_BALL);
            defaults.put(goldenName, ModPokeBalls.GOLDEN_SAFARI_BALL);
            
            // Link the PokeBalls to their items after registration
            // This will be done when the items are actually created during registration
        } catch (Exception e) {
            // If reflection fails, log the error
            System.err.println("Failed to register custom Safari Balls: " + e.getMessage());
        }
    }
}
