package abeshutt.staracademy.fabric

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import dev.corgitaco.enhancedcelestials.EnhancedCelestials
import dev.corgitaco.enhancedcelestials.api.ECLunarEventTags
import dev.corgitaco.enhancedcelestials.lunarevent.EnhancedCelestialsLunarForecastWorldData
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import abeshutt.staracademy.config.LunarBoostConfig
import net.fabricmc.loader.api.FabricLoader

object FabricLunarEventCommand {
    
    fun register() {
        println("Journey's End: FabricLunarEventCommand.register() called")
        CommandRegistrationCallback.EVENT.register { dispatcher, registryAccess, environment ->
            println("Journey's End: CommandRegistrationCallback triggered, registering lunar commands")
            registerCommands(dispatcher)
        }
    }
    
    private fun registerCommands(dispatcher: CommandDispatcher<ServerCommandSource>) {
        println("Journey's End: registerCommands() called")
        
        // Register /journeysend lunar commands
        dispatcher.register(
            CommandManager.literal("journeysend")
                .then(CommandManager.literal("lunar")
                    .then(CommandManager.literal("check")
                        .executes { context -> checkLunarEvent(context) }
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                            .requires { source -> source.hasPermissionLevel(2) }
                            .executes { context -> checkLunarEventForPlayer(context) }
                        )
                    )
                    .then(CommandManager.literal("spawnrates")
                        .executes { context -> checkSpawnRates(context) }
                    )
                    .then(CommandManager.literal("reload")
                        .requires { source -> source.hasPermissionLevel(2) }
                        .executes { context -> reloadConfig(context) }
                    )
                )
        )
        println("Journey's End: Registered /journeysend lunar commands")
    }
    
    private fun checkLunarEvent(context: CommandContext<ServerCommandSource>): Int {
        val source = context.source
        val player = source.player
        if (player == null) {
            source.sendFeedback({
                Text.literal("❌ This command can only be used by players")
                    .formatted(Formatting.RED)
            }, false)
            return 0
        }
        val world = player.world
        
        val data = EnhancedCelestials.lunarForecastWorldData(world).orElse(null) as? EnhancedCelestialsLunarForecastWorldData
        if (data == null) {
            source.sendFeedback({
                Text.literal("❌ Enhanced Celestials is not active in this world")
                    .formatted(Formatting.RED)
            }, false)
            return 0
        }
        
        val holder = data.currentLunarEventHolder()
        val config = LunarBoostConfig.getInstance()
        
        // Send header
        source.sendFeedback({
            Text.literal("🌙 Journey's End - Lunar Event Status")
                .formatted(Formatting.GOLD, Formatting.BOLD)
        }, false)
        
        // Check for active lunar events
        val activeEvents = mutableListOf<String>()
        val activeBoosts = mutableListOf<String>()
        
        // Blood Moon detection
        val isBloodMoon = holder.isIn(ECLunarEventTags.BLOOD_MOON)
        val isSuperMoon = holder.isIn(ECLunarEventTags.SUPER_MOON)
        if (isBloodMoon) {
            val multiplier = if (isSuperMoon) {
                config.superBloodMoonIVsMultiplier
            } else {
                config.bloodMoonIVsMultiplier
            }
            val moonType = if (isSuperMoon) "Super Blood Moon" else "Blood Moon"
            activeEvents.add("🩸 $moonType")
            activeBoosts.add("• IV Boost: ${multiplier}x")
        }
        
        // Blue Moon detection
        val isBlueMoon = holder.isIn(ECLunarEventTags.BLUE_MOON)
        if (isBlueMoon) {
            val multiplier = if (isSuperMoon) {
                config.superBlueMoonShinyMultiplier
            } else {
                config.blueMoonShinyMultiplier
            }
            val moonType = if (isSuperMoon) "Super Blue Moon" else "Blue Moon"
            activeEvents.add("🌊 $moonType")
            activeBoosts.add("• Shiny Boost: ${multiplier}x")
        }
        
        // Harvest Moon detection
        val isHarvestMoon = holder.isIn(ECLunarEventTags.HARVEST_MOON)
        if (isHarvestMoon) {
            val multiplier = if (isSuperMoon) {
                config.superHarvestMoonExpShareMultiplier
            } else {
                config.harvestMoonExpShareMultiplier
            }
            val moonType = if (isSuperMoon) "Super Harvest Moon" else "Harvest Moon"
            activeEvents.add("🌾 $moonType")
            activeBoosts.add("• EXP Boost: ${multiplier}x")
        }
        
        // Check for Aurora Moon events by checking the lunar event name
        try {
            val currentEvent = data.currentLunarEvent()
            if (currentEvent != null) {
                val textComponents = currentEvent.textComponents
                if (textComponents != null) {
                    val nameComponent = textComponents.name
                    if (nameComponent != null) {
                        val nameKey = nameComponent.key
                        if (nameKey != null && (nameKey == "enhancedcelestials.name.aurora_moon" || nameKey == "enhancedcelestials.name.super_aurora_moon")) {
                            val isSuperAurora = nameKey == "enhancedcelestials.name.super_aurora_moon"
                            if (isSuperAurora) {
                                activeEvents.add("🌟 Super Aurora Moon")
                            } else {
                                activeEvents.add("🌟 Aurora Moon")
                            }
                            
                            val multiplier = if (isSuperAurora) {
                                config.superAuroraMoonRarePokemonSpawnMultiplier
                            } else {
                                config.auroraMoonRarePokemonSpawnMultiplier
                            }
                            activeBoosts.add("• Rare Spawn Boost: ${multiplier}x")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Fallback: Aurora Moon events can be set with: /enhancedcelestials setLunarEvent enhancedcelestials:aurora_moon
        }
        
        if (holder.isIn(ECLunarEventTags.SUPER_MOON)) {
            activeEvents.add("✨ Super Moon")
        }
        
        // Display results
        if (activeEvents.isEmpty()) {
            source.sendFeedback({
                Text.literal("🌕 No active lunar events")
                    .formatted(Formatting.GRAY)
            }, false)
        } else {
            source.sendFeedback({
                Text.literal("Active Events: ${activeEvents.joinToString(", ")}")
                    .formatted(Formatting.YELLOW)
            }, false)
            
            if (activeBoosts.isNotEmpty()) {
                source.sendFeedback({
                    Text.literal("Active Boosts:")
                        .formatted(Formatting.GREEN)
                }, false)
                
                activeBoosts.forEach { boost ->
                    source.sendFeedback({
                        Text.literal(boost)
                            .formatted(Formatting.GREEN)
                    }, false)
                }
            }
        }
        
        // Show config location
        source.sendFeedback({
            Text.literal("📁 Config: /config/JourneysEnd/lunar_boosts.json")
                .formatted(Formatting.DARK_GRAY)
        }, false)
        
        return 1
    }
    
    private fun checkLunarEventForPlayer(context: CommandContext<ServerCommandSource>): Int {
        val source = context.source
        val targetPlayer = EntityArgumentType.getPlayer(context, "player")
        
        source.sendFeedback({
            Text.literal("Checking lunar event for ${targetPlayer.displayName?.string ?: "Unknown Player"}...")
                .formatted(Formatting.YELLOW)
        }, false)
        
        return checkLunarEvent(context)
    }
    
    private fun reloadConfig(context: CommandContext<ServerCommandSource>): Int {
        val source = context.source
        
        try {
            LunarBoostConfig.reload()
            source.sendFeedback({
                Text.literal("✅ Journey's End lunar config reloaded successfully!")
                    .formatted(Formatting.GREEN)
            }, true)
        } catch (e: Exception) {
            source.sendFeedback({
                Text.literal("❌ Failed to reload config: ${e.message}")
                    .formatted(Formatting.RED)
            }, false)
        }
        
        return 1
    }
    
    private fun checkSpawnRates(context: CommandContext<ServerCommandSource>): Int {
        val source = context.source
        val player = source.player
        if (player == null) {
            source.sendFeedback({
                Text.literal("❌ This command can only be used by players")
                    .formatted(Formatting.RED)
            }, false)
            return 0
        }
        
        // Check if Cobblemon is loaded
        if (!FabricLoader.getInstance().isModLoaded("cobblemon")) {
            source.sendFeedback({
                Text.literal("❌ Cobblemon is not installed - spawn rates not available")
                    .formatted(Formatting.RED)
            }, false)
            return 0
        }
        
        val world = player.world
        val data = EnhancedCelestials.lunarForecastWorldData(world).orElse(null) as? EnhancedCelestialsLunarForecastWorldData
        if (data == null) {
            source.sendFeedback({
                Text.literal("❌ Enhanced Celestials is not active in this world")
                    .formatted(Formatting.RED)
            }, false)
            return 0
        }
        
        val holder = data.currentLunarEventHolder()
        val config = LunarBoostConfig.getInstance()
        
        // Send header
        source.sendFeedback({
            Text.literal("🎲 Journey's End - Spawn Rate Analysis")
                .formatted(Formatting.GOLD, Formatting.BOLD)
        }, false)
        
        // Check current lunar events and their spawn multipliers
        val spawnMultipliers = mutableListOf<String>()
        
        // Get Super Moon status once for all checks
        val isSuperMoon = holder.isIn(ECLunarEventTags.SUPER_MOON)
        
        // Blood Moon detection
        val isBloodMoon = holder.isIn(ECLunarEventTags.BLOOD_MOON)
        if (isBloodMoon) {
            val multiplier = if (isSuperMoon) {
                config.superBloodMoonIVsMultiplier
            } else {
                config.bloodMoonIVsMultiplier
            }
            val moonType = if (isSuperMoon) "Super Blood Moon" else "Blood Moon"
            spawnMultipliers.add("🩸 $moonType: IV spawns ${multiplier}x more likely")
        }
        
        // Blue Moon detection
        val isBlueMoon = holder.isIn(ECLunarEventTags.BLUE_MOON)
        if (isBlueMoon) {
            val multiplier = if (isSuperMoon) {
                config.superBlueMoonShinyMultiplier
            } else {
                config.blueMoonShinyMultiplier
            }
            val moonType = if (isSuperMoon) "Super Blue Moon" else "Blue Moon"
            spawnMultipliers.add("🌊 $moonType: Shiny spawns ${multiplier}x more likely")
        }
        
        // Harvest Moon detection
        val isHarvestMoon = holder.isIn(ECLunarEventTags.HARVEST_MOON)
        if (isHarvestMoon) {
            val multiplier = if (isSuperMoon) {
                config.superHarvestMoonExpShareMultiplier
            } else {
                config.harvestMoonExpShareMultiplier
            }
            val moonType = if (isSuperMoon) "Super Harvest Moon" else "Harvest Moon"
            spawnMultipliers.add("🌾 $moonType: EXP gain ${multiplier}x more")
        }
        
        // Check for Aurora Moon
        try {
            val currentEvent = data.currentLunarEvent()
            if (currentEvent != null) {
                val textComponents = currentEvent.textComponents
                if (textComponents != null) {
                    val nameComponent = textComponents.name
                    if (nameComponent != null) {
                        val nameKey = nameComponent.key
                        if (nameKey != null && (nameKey == "enhancedcelestials.name.aurora_moon" || nameKey == "enhancedcelestials.name.super_aurora_moon")) {
                            val isSuperAurora = nameKey == "enhancedcelestials.name.super_aurora_moon"
                            val multiplier = if (isSuperAurora) {
                                config.superAuroraMoonRarePokemonSpawnMultiplier
                            } else {
                                config.auroraMoonRarePokemonSpawnMultiplier
                            }
                            
                            if (isSuperAurora) {
                                spawnMultipliers.add("🌟 Super Aurora Moon: Rare spawns ${multiplier}x more likely, common ${multiplier}x less likely")
                            } else {
                                spawnMultipliers.add("🌟 Aurora Moon: Rare spawns ${multiplier}x more likely, common ${multiplier}x less likely")
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Ignore errors
        }
        
        // Display results
        if (spawnMultipliers.isEmpty()) {
            source.sendFeedback({
                Text.literal("🌕 No active lunar events - normal spawn rates")
                    .formatted(Formatting.GRAY)
            }, false)
        } else {
            source.sendFeedback({
                Text.literal("Active Spawn Multipliers:")
                    .formatted(Formatting.YELLOW)
            }, false)
            
            spawnMultipliers.forEach { multiplier ->
                source.sendFeedback({
                    Text.literal("• $multiplier")
                        .formatted(Formatting.GREEN)
                }, false)
            }
        }
        
        return 1
    }
}
