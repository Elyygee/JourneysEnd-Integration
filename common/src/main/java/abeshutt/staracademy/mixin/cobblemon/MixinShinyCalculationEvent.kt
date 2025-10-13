package abeshutt.staracademy.mixin.cobblemon

import com.cobblemon.mod.common.api.events.pokemon.ShinyChanceCalculationEvent
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.registry.tag.TagKey
import net.minecraft.world.World
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import abeshutt.staracademy.config.LunarBoostConfig

/**
 * Mixin to boost shiny Pokemon spawn rates during Blue Moon events
 * Based on Academy's implementation
 */
@Mixin(ShinyChanceCalculationEvent::class)
class MixinShinyCalculationEvent {
    
    @Inject(
        method = ["calculate"],
        at = [At("RETURN")],
        cancellable = true
    )
    private fun calculate(player: ServerPlayerEntity?, ci: CallbackInfoReturnable<Float>) {
        if (player == null || !FabricLoader.getInstance().isModLoaded("enhancedcelestials")) {
            return // No player or Enhanced Celestials not loaded, skip
        }

        val chance = ci.returnValue
        
        try {
            // Use reflection to access Enhanced Celestials APIs
            val ecClass = Class.forName("dev.corgitaco.enhancedcelestials.EnhancedCelestials")
            val lunarForecastWorldData = ecClass.getMethod("lunarForecastWorldData", World::class.java)
            val dataOpt = lunarForecastWorldData.invoke(null, player.serverWorld) as java.util.Optional<*>
            if (!dataOpt.isPresent) return

            val data = dataOpt.get()

            // Get current lunar event holder (type is a Minecraft RegistryEntry<ECLunarEvent>)
            val currentHolderMethod = data.javaClass.getMethod("currentLunarEventHolder")
            val holderAny = currentHolderMethod.invoke(data) ?: return
            @Suppress("UNCHECKED_CAST")
            val holder = holderAny as RegistryEntry<Any>

            // Get EC tag constants via reflection (they're TagKey<ECLunarEvent>)
            val tagsClass = Class.forName("dev.corgitaco.enhancedcelestials.api.ECLunarEventTags")
            @Suppress("UNCHECKED_CAST")
            val blueMoonTag = tagsClass.getField("BLUE_MOON").get(null) as TagKey<Any>
            @Suppress("UNCHECKED_CAST")
            val superMoonTag = tagsClass.getField("SUPER_MOON").get(null) as TagKey<Any>

            // ✅ Call the MC API directly (no reflection on Minecraft classes)
            val isBlueMoon = holder.isIn(blueMoonTag)
            val isSuperMoon = holder.isIn(superMoonTag)

            if (isBlueMoon) {
                val config = LunarBoostConfig.getInstance()
                val multiplier = if (isSuperMoon) {
                    config.superBlueMoonShinyMultiplier
                } else {
                    config.blueMoonShinyMultiplier
                }
                
                // In Cobblemon, chance is the denominator (lower = better odds)
                // So we divide by the multiplier to make it smaller (better odds)
                val newChance = (ci.returnValue / multiplier).coerceAtLeast(1.0f)
                ci.returnValue = newChance
            }
        } catch (e: Exception) {
            // Log the error or handle it gracefully if Enhanced Celestials APIs are not found or fail
            System.err.println("Journey's End: Failed to apply shiny boost due to Enhanced Celestials API access error: ${e.message}")
            e.printStackTrace()
        }
    }
}
