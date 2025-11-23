package abeshutt.staracademy.init;

import abeshutt.staracademy.StarAcademyMod;
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
            // 2D inventory icon - use Cobblemon namespace to match cobblemon-main17 structure
            // Resolves to: assets/cobblemon/models/item/great_safari_ball.json -> cobblemon:textures/item/poke_balls/great_safari_ball.png
            new Identifier("cobblemon", "great_safari_ball"),
            new Identifier("cobblemon", "item/great_safari_ball_model"),  // 3D model - cobblemon namespace (maps to cobblemon:models/item/great_safari_ball_model.json)
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
            // 2D inventory icon - use Cobblemon namespace to match cobblemon-main17 structure
            // Resolves to: assets/cobblemon/models/item/golden_safari_ball.json -> cobblemon:textures/item/poke_balls/golden_safari_ball.png
            new Identifier("cobblemon", "golden_safari_ball"),
            new Identifier("cobblemon", "item/golden_safari_ball_model"),  // 3D model - cobblemon namespace (maps to cobblemon:models/item/golden_safari_ball_model.json)
            1.25F, false);
}
