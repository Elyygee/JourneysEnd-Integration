package abeshutt.staracademy.mixin.cobblemon;

import abeshutt.staracademy.StarAcademyMod;
import abeshutt.staracademy.init.ModPokeBalls;
import com.cobblemon.mod.common.api.pokeball.PokeBalls;
import com.cobblemon.mod.common.pokeball.PokeBall;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PokeBalls.class)
public class MixinPokeBalls {

    @Inject(method = "<clinit>", at = @At("TAIL"), remap = false)
    private static void clinit(CallbackInfo ci) {
        // Add our custom Safari Balls to both defaults and custom maps
        try {
            java.lang.reflect.Field defaultsField = PokeBalls.class.getDeclaredField("defaults");
            defaultsField.setAccessible(true);
            java.util.Map defaults = (java.util.Map) defaultsField.get(null);
            
            java.lang.reflect.Field customField = PokeBalls.class.getDeclaredField("custom");
            customField.setAccessible(true);
            java.util.Map custom = (java.util.Map) customField.get(null);
            
            // Use reflection to get the name field from PokeBall
            java.lang.reflect.Field nameField = PokeBall.class.getDeclaredField("name");
            nameField.setAccessible(true);
            
            Object greatName = nameField.get(ModPokeBalls.GREAT_SAFARI_BALL);
            Object goldenName = nameField.get(ModPokeBalls.GOLDEN_SAFARI_BALL);
            
            // Add to both maps to ensure lookup works
            defaults.put(greatName, ModPokeBalls.GREAT_SAFARI_BALL);
            defaults.put(goldenName, ModPokeBalls.GOLDEN_SAFARI_BALL);
            custom.put(greatName, ModPokeBalls.GREAT_SAFARI_BALL);
            custom.put(goldenName, ModPokeBalls.GOLDEN_SAFARI_BALL);
        } catch (Exception e) {
            // If reflection fails, log the error
            System.err.println("Failed to register custom Safari Balls: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @ModifyVariable(method = "getPokeBall(Lnet/minecraft/resources/ResourceLocation;)Lcom/cobblemon/mod/common/pokeball/PokeBall;", at = @At("RETURN"), ordinal = 0, remap = false, require = 0)
    private static PokeBall modifyGetPokeBallResult(PokeBall result, Identifier name) {
        // If lookup returned null for our custom Safari Balls, return our custom ball instead
        if (result == null && name != null && name.getNamespace().equals(StarAcademyMod.ID)) {
            if (name.getPath().equals("great_safari_ball")) {
                return ModPokeBalls.GREAT_SAFARI_BALL;
            } else if (name.getPath().equals("golden_safari_ball")) {
                return ModPokeBalls.GOLDEN_SAFARI_BALL;
            }
        }
        return result;
    }
}
