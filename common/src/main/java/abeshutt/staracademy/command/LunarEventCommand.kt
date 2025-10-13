// file: abeshutt/staracademy/command/LunarEventCommand.kt
package abeshutt.staracademy.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import dev.corgitaco.enhancedcelestials.EnhancedCelestials
import dev.corgitaco.enhancedcelestials.api.ECLunarEventTags
import dev.corgitaco.enhancedcelestials.lunarevent.EnhancedCelestialsLunarForecastWorldData
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import abeshutt.staracademy.config.LunarBoostConfig

object LunarEventCommand {

    fun register() {
        // Note: Command registration will be handled by the main mod initialization
        // This is a placeholder for compatibility
    }

    private fun registerCommands(dispatcher: CommandDispatcher<ServerCommandSource>) {
        dispatcher.register(
            CommandManager.literal("lunar")
                .then(CommandManager.literal("check")
                    .executes { ctx -> checkLunarEvent(ctx, ctx.source.player) }
                    .then(CommandManager.argument("player", EntityArgumentType.player())
                        .requires { src -> src.hasPermissionLevel(2) }
                        .executes { ctx -> checkLunarEvent(ctx, EntityArgumentType.getPlayer(ctx, "player")) }
                    )
                )
                .then(CommandManager.literal("spawnrates")
                    .executes { ctx -> checkSpawnRates(ctx) }
                )
                .then(CommandManager.literal("reload")
                    .requires { src -> src.hasPermissionLevel(2) }
                    .executes { ctx -> reloadConfig(ctx) }
                )
        )
    }

    /** Shared checker that can target either the sender or a specific player's world. */
    fun checkLunarEvent(context: CommandContext<ServerCommandSource>): Int {
        return checkLunarEvent(context, null)
    }

    fun checkLunarEvent(context: CommandContext<ServerCommandSource>, target: ServerPlayerEntity?): Int {
        val source = context.source
        val player = target ?: source.player
        if (player == null) {
            source.sendFeedback({ Text.literal("❌ This command can only be used by players").formatted(Formatting.RED) }, false)
            return 0
        }

        val data = EnhancedCelestials.lunarForecastWorldData(player.world).orElse(null) as? EnhancedCelestialsLunarForecastWorldData
        if (data == null) {
            source.sendFeedback({ Text.literal("❌ Enhanced Celestials is not active in this world").formatted(Formatting.RED) }, false)
            return 0
        }

        val holder = data.currentLunarEventHolder()
        if (holder == null) {
            source.sendFeedback({ Text.literal("🌕 No active lunar event right now").formatted(Formatting.GRAY) }, false)
            return 1
        }

        val config = LunarBoostConfig.getInstance()

        source.sendFeedback({ Text.literal("🌙 Journey's End - Lunar Event Status").formatted(Formatting.GOLD, Formatting.BOLD) }, false)

        val activeEvents = mutableListOf<String>()
        val activeBoosts = mutableListOf<String>()

        val isSuperMoon = holder.isIn(ECLunarEventTags.SUPER_MOON)

        // Blood
        if (holder.isIn(ECLunarEventTags.BLOOD_MOON)) {
            val mult = if (isSuperMoon) config.superBloodMoonIVsMultiplier else config.bloodMoonIVsMultiplier
            activeEvents += "🩸 ${if (isSuperMoon) "Super Blood Moon" else "Blood Moon"}"
            activeBoosts += "• IV Boost: ${mult}x"
        }
        // Blue (shiny)
        if (holder.isIn(ECLunarEventTags.BLUE_MOON)) {
            val mult = if (isSuperMoon) config.superBlueMoonShinyMultiplier else config.blueMoonShinyMultiplier
            activeEvents += "🌊 ${if (isSuperMoon) "Super Blue Moon" else "Blue Moon"}"
            activeBoosts += "• Shiny Boost: ${mult}x"
        }
        // Harvest (EXP)
        if (holder.isIn(ECLunarEventTags.HARVEST_MOON)) {
            val mult = if (isSuperMoon) config.superHarvestMoonExpShareMultiplier else config.harvestMoonExpShareMultiplier
            activeEvents += "🌾 ${if (isSuperMoon) "Super Harvest Moon" else "Harvest Moon"}"
            activeBoosts += "• EXP Boost: ${mult}x"
        }

        // Aurora / Super Aurora by name key (safe to ignore if EC pack doesn’t define one)
        runCatching {
            val current = data.currentLunarEvent()
            val nameKey = current?.textComponents?.name?.key
            val isAurora = nameKey == "enhancedcelestials.name.aurora_moon"
            val isSuperAurora = nameKey == "enhancedcelestials.name.super_aurora_moon"
            if (isAurora || isSuperAurora) {
                val mult = if (isSuperAurora) config.superAuroraMoonRarePokemonSpawnMultiplier else config.auroraMoonRarePokemonSpawnMultiplier
                activeEvents += if (isSuperAurora) "🌟 Super Aurora Moon" else "🌟 Aurora Moon"
                activeBoosts += "• Rare Spawn Boost: ${mult}x"
            }
        }

        if (isSuperMoon) activeEvents += "✨ Super Moon"

        if (activeEvents.isEmpty()) {
            source.sendFeedback({ Text.literal("🌕 No active lunar events").formatted(Formatting.GRAY) }, false)
        } else {
            source.sendFeedback({ Text.literal("Active Events: ${activeEvents.joinToString(", ")}").formatted(Formatting.YELLOW) }, false)
            if (activeBoosts.isNotEmpty()) {
                source.sendFeedback({ Text.literal("Active Boosts:").formatted(Formatting.GREEN) }, false)
                activeBoosts.forEach { boost ->
                    source.sendFeedback({ Text.literal(boost).formatted(Formatting.GREEN) }, false)
                }
            }
        }

        source.sendFeedback({ Text.literal("📁 Config: /config/JourneysEnd/lunar_boosts.json").formatted(Formatting.DARK_GRAY) }, false)
        return 1
    }

    fun checkLunarEventForPlayer(context: CommandContext<ServerCommandSource>): Int {
        val target = EntityArgumentType.getPlayer(context, "player")
        return checkLunarEvent(context, target)
    }

    fun reloadConfig(context: CommandContext<ServerCommandSource>): Int {
        val source = context.source
        return try {
            LunarBoostConfig.reload()
            source.sendFeedback({ Text.literal("✅ Journey's End lunar config reloaded successfully!").formatted(Formatting.GREEN) }, true)
            1
        } catch (e: Exception) {
            source.sendFeedback({ Text.literal("❌ Failed to reload config: ${e.message}").formatted(Formatting.RED) }, false)
            0
        }
    }

    fun checkSpawnRates(context: CommandContext<ServerCommandSource>): Int {
        val source = context.source
        val player = source.player ?: run {
            source.sendFeedback({ Text.literal("❌ This command can only be used by players").formatted(Formatting.RED) }, false)
            return 0
        }

        if (!FabricLoader.getInstance().isModLoaded("cobblemon")) {
            source.sendFeedback({ Text.literal("❌ Cobblemon is not installed - spawn rates not available").formatted(Formatting.RED) }, false)
            return 0
        }

        val data = EnhancedCelestials.lunarForecastWorldData(player.world).orElse(null) as? EnhancedCelestialsLunarForecastWorldData
        if (data == null) {
            source.sendFeedback({ Text.literal("❌ Enhanced Celestials is not active in this world").formatted(Formatting.RED) }, false)
            return 0
        }

        val holder = data.currentLunarEventHolder()
        if (holder == null) {
            source.sendFeedback({ Text.literal("🌕 No active lunar events - normal spawn rates").formatted(Formatting.GRAY) }, false)
            return 1
        }

        val cfg = LunarBoostConfig.getInstance()

        source.sendFeedback({ Text.literal("🎲 Journey's End - Spawn Rate Analysis").formatted(Formatting.GOLD, Formatting.BOLD) }, false)

        val lines = mutableListOf<String>()
        val isSuperMoon = holder.isIn(ECLunarEventTags.SUPER_MOON)

        if (holder.isIn(ECLunarEventTags.BLOOD_MOON)) {
            val m = if (isSuperMoon) cfg.superBloodMoonIVsMultiplier else cfg.bloodMoonIVsMultiplier
            lines += "🩸 ${if (isSuperMoon) "Super Blood Moon" else "Blood Moon"}: IV spawns ${m}x more likely"
        }

        val isBlueMoon = holder.isIn(ECLunarEventTags.BLUE_MOON)
        if (isBlueMoon) {
            val m = if (isSuperMoon) cfg.superBlueMoonShinyMultiplier else cfg.blueMoonShinyMultiplier
            lines += "🌊 ${if (isSuperMoon) "Super Blue Moon" else "Blue Moon"}: Shiny spawns ${m}x more likely"
        }

        if (holder.isIn(ECLunarEventTags.HARVEST_MOON)) {
            val m = if (isSuperMoon) cfg.superHarvestMoonExpShareMultiplier else cfg.harvestMoonExpShareMultiplier
            lines += "🌾 ${if (isSuperMoon) "Super Harvest Moon" else "Harvest Moon"}: EXP gain ${m}x more"
        }

        runCatching {
            val nameKey = data.currentLunarEvent()?.textComponents?.name?.key
            val isAurora = nameKey == "enhancedcelestials.name.aurora_moon"
            val isSuperAurora = nameKey == "enhancedcelestials.name.super_aurora_moon"
            if (isAurora || isSuperAurora) {
                val m = if (isSuperAurora) cfg.superAuroraMoonRarePokemonSpawnMultiplier else cfg.auroraMoonRarePokemonSpawnMultiplier
                lines += "🌟 ${if (isSuperAurora) "Super Aurora Moon" else "Aurora Moon"}: Rare spawns ${m}x more likely, common ${m}x less likely"
            }
        }

        if (lines.isEmpty()) {
            source.sendFeedback({ Text.literal("🌕 No active lunar events - normal spawn rates").formatted(Formatting.GRAY) }, false)
        } else {
            source.sendFeedback({ Text.literal("Active Spawn Multipliers:").formatted(Formatting.YELLOW) }, false)
            lines.forEach { s -> source.sendFeedback({ Text.literal("• $s").formatted(Formatting.GREEN) }, false) }
        }

        source.sendFeedback({ Text.literal("📊 Current Spawn Rates (with active boosts):").formatted(Formatting.AQUA) }, false)

        runCatching { showLiveSpawnRates(source, player, lines) }
            .onFailure { showEstimatedRates(source, lines) }

        return 1
    }

    private fun showLiveSpawnRates(source: ServerCommandSource, player: ServerPlayerEntity, activeMultipliers: List<String>) {
        source.sendFeedback({ Text.literal("🔍 Loading current values from Cobblemon API:").formatted(Formatting.GREEN) }, false)

        val buckets = com.cobblemon.mod.common.Cobblemon.bestSpawner.config.buckets
        val baseShinyRate = com.cobblemon.mod.common.Cobblemon.config.shinyRate

        source.sendFeedback({ Text.literal("📊 Base Values from Cobblemon API:").formatted(Formatting.AQUA) }, false)

        val bucketWeights = buckets.associateWith { it.weight }.toMutableMap()
        val totalWeight = bucketWeights.values.sum()

        fun prettyCap(s: String) = s.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

        bucketWeights.forEach { (bucket, weight) ->
            val pct = (weight / totalWeight * 100.0)
            val pctStr = if (pct < 1.0) String.format("%.2f", pct) else pct.toInt().toString()
            source.sendFeedback({
                Text.literal("• ${prettyCap(bucket.name)}: weight=${String.format("%.1f", weight)}, ${pctStr}% of spawns")
                    .formatted(Formatting.WHITE)
            }, false)
        }

        source.sendFeedback({ Text.literal("📊 Total Weight: ${String.format("%.1f", totalWeight)}").formatted(Formatting.GRAY) }, false)
        showCurrentShinyRate(source, player, baseShinyRate)

        if (activeMultipliers.isNotEmpty()) {
            source.sendFeedback({ Text.literal("🚀 Active Boosts Applied:").formatted(Formatting.GOLD) }, false)
            activeMultipliers.forEach { m ->
                source.sendFeedback({ Text.literal("  $m").formatted(Formatting.YELLOW) }, false)
            }
        } else {
            source.sendFeedback({ Text.literal("ℹ️ No active lunar event boosts").formatted(Formatting.GRAY) }, false)
        }
    }

    private fun showCurrentShinyRate(source: ServerCommandSource, player: ServerPlayerEntity, baseShinyRate: Float) {
        source.sendFeedback({
            Text.literal("• Shiny Pokemon: ~1/${baseShinyRate.toInt()} base rate (from Cobblemon API)")
                .formatted(Formatting.WHITE)
        }, false)

        val data = EnhancedCelestials.lunarForecastWorldData(player.world).orElse(null) as? EnhancedCelestialsLunarForecastWorldData
        val holder = data?.currentLunarEventHolder() ?: return
        if (holder.isIn(ECLunarEventTags.BLUE_MOON)) {
            val cfg = LunarBoostConfig.getInstance()
            val mult = if (holder.isIn(ECLunarEventTags.SUPER_MOON)) cfg.superBlueMoonShinyMultiplier else cfg.blueMoonShinyMultiplier
            val boosted = baseShinyRate / mult
            source.sendFeedback({
                Text.literal("  └─ With Blue Moon boost (${mult}x): ~1/${boosted.toInt()}")
                    .formatted(Formatting.YELLOW)
            }, false)
        }
    }

    private fun showEstimatedRates(source: ServerCommandSource, activeMultipliers: List<String>) {
        var common = 70.0
        var uncommon = 20.0
        var rare = 8.0
        var ultra = 2.0

        activeMultipliers.forEach { line ->
            if (line.contains("Aurora Moon")) {
                val mult = line.substringAfter(": ").substringBefore("x").toFloatOrNull() ?: 1.0f
                uncommon *= mult
                rare *= mult
                ultra *= mult
            }
        }

        val total = common + uncommon + rare + ultra
        fun norm(v: Double) = (v / total * 100).toInt()

        source.sendFeedback({ Text.literal("• Common Pokemon: ~${norm(common)}% of spawns").formatted(Formatting.WHITE) }, false)
        source.sendFeedback({ Text.literal("• Uncommon Pokemon: ~${norm(uncommon)}% of spawns").formatted(Formatting.WHITE) }, false)
        source.sendFeedback({ Text.literal("• Rare Pokemon: ~${norm(rare)}% of spawns").formatted(Formatting.WHITE) }, false)
        source.sendFeedback({ Text.literal("• Ultra-rare Pokemon: ~${norm(ultra)}% of spawns").formatted(Formatting.WHITE) }, false)

        val baseShinyRate = com.cobblemon.mod.common.Cobblemon.config.shinyRate
        var current = baseShinyRate
        activeMultipliers.forEach { line ->
            if (line.contains("Blue Moon") && line.contains("Shiny")) {
                val mult = line.substringAfter(": ").substringBefore("x").toFloatOrNull() ?: 1.0f
                current += baseShinyRate * (mult - 1.0f)
            }
        }

        val shinyText =
            if (current != baseShinyRate) "• Shiny Pokemon: ~1/${current.toInt()} (boosted from 1/${baseShinyRate.toInt()})"
            else "• Shiny Pokemon: ~1/${current.toInt()} base rate"

        source.sendFeedback({ Text.literal(shinyText).formatted(Formatting.WHITE) }, false)
    }
}
