package abeshutt.staracademy.init;

import abeshutt.staracademy.StarAcademyMod;
import com.cobblemon.mod.common.api.pokeball.catching.modifiers.MultiplierModifier;
import com.cobblemon.mod.common.api.pokeball.catching.modifiers.WorldStateModifier;
import com.cobblemon.mod.common.pokeball.PokeBall;
import net.minecraft.util.Identifier;

import java.util.List;

public class ModPokeBalls {

    public static final PokeBall GREAT_SAFARI_BALL = new PokeBall(StarAcademyMod.id("great_safari_ball"),
            new WorldStateModifier((thrower, entity) -> {
                // Safari Ball logic: 1.5x if not battling, 1x if battling
                // Then multiply by 4x for Great Safari Ball
                if (!entity.isBattling()) {
                    return 1.5F * 4.0F; // 6.0F total
                }
                return 1.0F * 4.0F; // 4.0F total
            }), 
            List.of(), 0.8F, 
            Identifier.of("cobblemon", "safari_ball"),  // Use Cobblemon's Safari Ball 2D icon
            Identifier.of("cobblemon", "item/safari_ball_model"),  // Use Cobblemon's Safari Ball 3D model
            1.25F, false);
            
    public static final PokeBall GOLDEN_SAFARI_BALL = new PokeBall(StarAcademyMod.id("golden_safari_ball"),
            new WorldStateModifier((thrower, entity) -> {
                // Safari Ball logic: 1.5x if not battling, 1x if battling
                // Then multiply by 8x for Golden Safari Ball
                if (!entity.isBattling()) {
                    return 1.5F * 8.0F; // 12.0F total
                }
                return 1.0F * 8.0F; // 8.0F total
            }),
            List.of((thrower, pokemon) -> pokemon.setShiny(true)),
            0.8F, 
            Identifier.of("cobblemon", "safari_ball"),  // Use Cobblemon's Safari Ball 2D icon
            Identifier.of("cobblemon", "item/safari_ball_model"),  // Use Cobblemon's Safari Ball 3D model
            1.25F, false);
}
