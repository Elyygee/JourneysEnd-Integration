package abeshutt.staracademy.compat.enhancedcelestials

import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.events.pokemon.ExperienceGainedPreEvent
import com.cobblemon.mod.common.api.events.pokemon.ShinyChanceCalculationEvent
import com.cobblemon.mod.common.api.spawning.SpawnBucket
import com.cobblemon.mod.common.api.spawning.context.SpawningContext
import com.cobblemon.mod.common.api.spawning.detail.PokemonSpawnDetail
import com.cobblemon.mod.common.api.spawning.detail.SpawnDetail
import com.cobblemon.mod.common.api.spawning.influence.SpawningInfluence
import com.cobblemon.mod.common.api.spawning.spawner.PlayerSpawnerFactory
import com.cobblemon.mod.common.pokemon.IVs
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.registry.tag.TagKey
import net.minecraft.util.Identifier
import abeshutt.staracademy.config.LunarBoostConfig

object EnhancedCelestialsCompat {
    // Create our own Aurora Moon tag like ACADEMY does
    private val AURORA_MOON_IDENTIFIER = Identifier.of("enhancedcelestials", "aurora_moon")
    
    @JvmStatic
    fun init() {
        println("Journey's End: Starting Enhanced Celestials compatibility initialization...")
        
        // Check if Enhanced Celestials is available
        if (!FabricLoader.getInstance().isModLoaded("enhancedcelestials")) {
            println("Journey's End: Enhanced Celestials not loaded, skipping lunar event compatibility.")
            return
        }
        
        try {
            // Use reflection to get EnhancedCelestialsRegistry.LUNAR_EVENT_KEY
            val enhancedCelestialsRegistryClass = Class.forName("dev.corgitaco.enhancedcelestials.api.EnhancedCelestialsRegistry")
            val lunarEventKeyField = enhancedCelestialsRegistryClass.getField("LUNAR_EVENT_KEY")
            val lunarEventKey = lunarEventKeyField.get(null) as? TagKey<*>
            
            if (lunarEventKey == null) {
                System.err.println("Star Academy: Could not resolve LUNAR_EVENT_KEY, skipping lunar event compatibility.")
                return
            }

            val AURORA_MOON = TagKey.of(lunarEventKey as net.minecraft.registry.RegistryKey<out net.minecraft.registry.Registry<Any>>, AURORA_MOON_IDENTIFIER)

            registerExpBoost(AURORA_MOON)
            registerShinyBoost(AURORA_MOON)
            registerBloodMoonIVBoost(AURORA_MOON)
            registerAuroraMoonSpawnBoost(AURORA_MOON)
            println("Journey's End: Enhanced Celestials compatibility registered (EXP + Shiny + IV + Aurora Moon spawn boosts)")
        } catch (e: Exception) {
            System.err.println("Journey's End: Failed to initialize Enhanced Celestials compatibility due to API access error: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * Harvest Moon - EXP Share Boost
     */
    private fun registerExpBoost(auroraMoonTag: TagKey<*>) {
        CobblemonEvents.EXPERIENCE_GAINED_EVENT_PRE.subscribe { event ->
            val pokemon = event.pokemon
            val entity = pokemon.entity ?: return@subscribe
            val world = entity.world
            
            try {
                val enhancedCelestialsClass = Class.forName("dev.corgitaco.enhancedcelestials.EnhancedCelestials")
                val lunarForecastWorldDataMethod = enhancedCelestialsClass.getMethod("lunarForecastWorldData", net.minecraft.world.World::class.java)
                val lunarDataOptional = lunarForecastWorldDataMethod.invoke(null, world) as java.util.Optional<*>
                
                if (lunarDataOptional.isPresent) {
                    val lunarData = lunarDataOptional.get()
                    val currentLunarEventHolderMethod = lunarData.javaClass.getMethod("currentLunarEventHolder")
                    val holder = currentLunarEventHolderMethod.invoke(lunarData) ?: return@subscribe
                    
                    val eclunarEventTagsClass = Class.forName("dev.corgitaco.enhancedcelestials.api.ECLunarEventTags")
                    val harvestMoonField = eclunarEventTagsClass.getField("HARVEST_MOON")
                    val superMoonField = eclunarEventTagsClass.getField("SUPER_MOON")
                    
                    val harvestMoonTag = harvestMoonField.get(null)
                    val superMoonTag = superMoonField.get(null)
                    
                    val isInMethod = holder.javaClass.getMethod("isIn", Class.forName("net.minecraft.registry.tag.TagKey"))
                    
                    val isHarvestMoon = isInMethod.invoke(holder, harvestMoonTag) as Boolean
                    val isSuperMoon = isInMethod.invoke(holder, superMoonTag) as Boolean
                    
                    if (isHarvestMoon) {
                        val config = LunarBoostConfig.getInstance()
                        val multiplier = if (isSuperMoon) {
                            config.superHarvestMoonExpShareMultiplier
                        } else {
                            config.harvestMoonExpShareMultiplier
                        }
                        val currentExp = event.experience
                        event.experience = (currentExp * multiplier).toInt()
                        println("Journey's End: Applying Harvest Moon EXP boost: ${multiplier}x")
                    }
                }
            } catch (e: Exception) {
                System.err.println("Journey's End: Failed to apply EXP boost due to Enhanced Celestials API access error: ${e.message}")
                e.printStackTrace()
            }
        }
    }
    
    /**
     * Blue Moon - Shiny Pokemon Boost
     */
    private fun registerShinyBoost(auroraMoonTag: TagKey<*>) {
        CobblemonEvents.SHINY_CHANCE_CALCULATION.subscribe { event ->
            val pokemon = event.pokemon
            val entity = pokemon.entity ?: return@subscribe
            val world = entity.world
            
            try {
                val enhancedCelestialsClass = Class.forName("dev.corgitaco.enhancedcelestials.EnhancedCelestials")
                val lunarForecastWorldDataMethod = enhancedCelestialsClass.getMethod("lunarForecastWorldData", net.minecraft.world.World::class.java)
                val lunarDataOptional = lunarForecastWorldDataMethod.invoke(null, world) as java.util.Optional<*>
                
                if (lunarDataOptional.isPresent) {
                    val lunarData = lunarDataOptional.get()
                    val currentLunarEventHolderMethod = lunarData.javaClass.getMethod("currentLunarEventHolder")
                    val holder = currentLunarEventHolderMethod.invoke(lunarData) ?: return@subscribe
                    
                    val eclunarEventTagsClass = Class.forName("dev.corgitaco.enhancedcelestials.api.ECLunarEventTags")
                    val blueMoonField = eclunarEventTagsClass.getField("BLUE_MOON")
                    val superMoonField = eclunarEventTagsClass.getField("SUPER_MOON")
                    
                    val blueMoonTag = blueMoonField.get(null)
                    val superMoonTag = superMoonField.get(null)
                    
                    val isInMethod = holder.javaClass.getMethod("isIn", Class.forName("net.minecraft.registry.tag.TagKey"))
                    
                    val isBlueMoon = isInMethod.invoke(holder, blueMoonTag) as Boolean
                    val isSuperMoon = isInMethod.invoke(holder, superMoonTag) as Boolean
                    
                    println("Journey's End: Shiny calculation - Blue Moon: $isBlueMoon, Super Moon: $isSuperMoon")
                    
                    if (isBlueMoon) {
                        val config = LunarBoostConfig.getInstance()
                        val multiplier = if (isSuperMoon) {
                            config.superBlueMoonShinyMultiplier
                        } else {
                            config.blueMoonShinyMultiplier
                        }
                        println("Journey's End: Applying Blue Moon shiny boost: ${multiplier}x")
                        // Cobblemon's chance is the denominator, so we need to reduce it for better odds
                        // Use the addModifier method to reduce the chance
                        val currentChance = event.chance
                        val newChance = currentChance / multiplier
                        val modifier = currentChance - newChance
                        event.addModifier(-modifier) // Negative modifier reduces the chance
                    }
                }
            } catch (e: Exception) {
                System.err.println("Journey's End: Failed to apply shiny boost due to Enhanced Celestials API access error: ${e.message}")
                e.printStackTrace()
            }
        }
    }
    
    /**
     * Blood Moon - IV Boost
     */
    private fun registerBloodMoonIVBoost(auroraMoonTag: TagKey<*>) {
        PlayerSpawnerFactory.influenceBuilders.add { serverPlayer ->
            object : SpawningInfluence {
                override fun isExpired(): Boolean {
                    try {
                        val enhancedCelestialsClass = Class.forName("dev.corgitaco.enhancedcelestials.EnhancedCelestials")
                        val lunarForecastWorldDataMethod = enhancedCelestialsClass.getMethod("lunarForecastWorldData", net.minecraft.world.World::class.java)
                        val lunarDataOptional = lunarForecastWorldDataMethod.invoke(null, serverPlayer.getWorld()) as java.util.Optional<*>
                        
                        if (lunarDataOptional.isPresent) {
                            val lunarData = lunarDataOptional.get()
                            val currentLunarEventHolderMethod = lunarData.javaClass.getMethod("currentLunarEventHolder")
                            val holder = currentLunarEventHolderMethod.invoke(lunarData) ?: return true
                            
                            val eclunarEventTagsClass = Class.forName("dev.corgitaco.enhancedcelestials.api.ECLunarEventTags")
                            val bloodMoonField = eclunarEventTagsClass.getField("BLOOD_MOON")
                            val bloodMoonTag = bloodMoonField.get(null)
                            
                            val isInMethod = holder.javaClass.getMethod("isIn", Class.forName("net.minecraft.registry.tag.TagKey"))
                            return !(isInMethod.invoke(holder, bloodMoonTag) as Boolean)
                        }
                    } catch (e: Exception) {
                        System.err.println("Journey's End: Failed to check Blood Moon status due to Enhanced Celestials API access error: ${e.message}")
                        e.printStackTrace()
                    }
                    return true // Expired if EC not loaded or error
                }
                
                override fun affectWeight(detail: SpawnDetail, spawningContext: SpawningContext, weight: Float): Float {
                    if (detail is PokemonSpawnDetail) {
                        try {
                            val enhancedCelestialsClass = Class.forName("dev.corgitaco.enhancedcelestials.EnhancedCelestials")
                            val lunarForecastWorldDataMethod = enhancedCelestialsClass.getMethod("lunarForecastWorldData", net.minecraft.world.World::class.java)
                            val lunarDataOptional = lunarForecastWorldDataMethod.invoke(null, spawningContext.world) as java.util.Optional<*>
                            
                            if (lunarDataOptional.isPresent) {
                                val lunarData = lunarDataOptional.get()
                                val currentLunarEventHolderMethod = lunarData.javaClass.getMethod("currentLunarEventHolder")
                                val holder = currentLunarEventHolderMethod.invoke(lunarData) ?: return weight
                                
                                val eclunarEventTagsClass = Class.forName("dev.corgitaco.enhancedcelestials.api.ECLunarEventTags")
                                val bloodMoonField = eclunarEventTagsClass.getField("BLOOD_MOON")
                                val superMoonField = eclunarEventTagsClass.getField("SUPER_MOON")
                                
                                val bloodMoonTag = bloodMoonField.get(null)
                                val superMoonTag = superMoonField.get(null)
                                
                                val isInMethod = holder.javaClass.getMethod("isIn", Class.forName("net.minecraft.registry.tag.TagKey"))
                                
                                val isBloodMoon = isInMethod.invoke(holder, bloodMoonTag) as Boolean
                                val isSuperMoon = isInMethod.invoke(holder, superMoonTag) as Boolean
                                
                                if (isBloodMoon) {
                                    val pokemon = detail.pokemon
                                    val ivs = pokemon.ivs
                                    if (ivs != null && !ivs.acceptableRange.isEmpty()) {
                                        val config = LunarBoostConfig.getInstance()
                                        val multiplier = if (isSuperMoon) {
                                            config.superBloodMoonIVsMultiplier
                                        } else {
                                            config.bloodMoonIVsMultiplier
                                        }
                                        println("Journey's End: Applying Blood Moon IV boost: ${multiplier}x")
                                        return weight * multiplier
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            System.err.println("Journey's End: Failed to apply IV boost due to Enhanced Celestials API access error: ${e.message}")
                            e.printStackTrace()
                        }
                    }
                    return weight
                }
            }
        }
    }
    
    /**
     * Aurora Moon - Rare Pokemon Spawn Boost
     * Treats "super_aurora_moon" as AURORA_MOON ∧ SUPER_MOON combination
     */
    private fun registerAuroraMoonSpawnBoost(@Suppress("UNUSED_PARAMETER") auroraMoonTag: TagKey<*>) {
        println("Journey's End: ===== REGISTERING AURORA MOON SPAWN BOOST =====")

        PlayerSpawnerFactory.influenceBuilders.add { serverPlayer ->
            object : SpawningInfluence {
                override fun isExpired(): Boolean {
                    try {
                        val ec = Class.forName("dev.corgitaco.enhancedcelestials.EnhancedCelestials")
                        val wf = ec.getMethod("lunarForecastWorldData", net.minecraft.world.World::class.java)
                        val dataOpt = wf.invoke(null, serverPlayer.world) as java.util.Optional<*>

                        if (!dataOpt.isPresent) return true

                        val data = dataOpt.get()
                        val holder = data.javaClass.getMethod("currentLunarEventHolder").invoke(data) ?: return true

                        val tags = Class.forName("dev.corgitaco.enhancedcelestials.api.ECLunarEventTags")
                        val AURORA_MOON = tags.getField("AURORA_MOON").get(null)
                        val isIn = holder.javaClass.getMethod("isIn", Class.forName("net.minecraft.registry.tag.TagKey"))

                        val isAurora = isIn.invoke(holder, AURORA_MOON) as Boolean
                        // Active only while Aurora is up
                        return !isAurora
                    } catch (t: Throwable) {
                        System.err.println("Journey's End: Aurora isExpired() check failed: ${t.message}")
                        return true
                    }
                }

                override fun affectBucketWeight(bucket: SpawnBucket, weight: Float): Float {
                    try {
                        val ec = Class.forName("dev.corgitaco.enhancedcelestials.EnhancedCelestials")
                        val wf = ec.getMethod("lunarForecastWorldData", net.minecraft.world.World::class.java)
                        val dataOpt = wf.invoke(null, serverPlayer.world) as java.util.Optional<*>
                        if (!dataOpt.isPresent) return weight

                        val data = dataOpt.get()
                        val holder = data.javaClass.getMethod("currentLunarEventHolder").invoke(data) ?: return weight

                        val tags = Class.forName("dev.corgitaco.enhancedcelestials.api.ECLunarEventTags")
                        val AURORA_MOON = tags.getField("AURORA_MOON").get(null)
                        val SUPER_MOON = tags.getField("SUPER_MOON").get(null)
                        val isIn = holder.javaClass.getMethod("isIn", Class.forName("net.minecraft.registry.tag.TagKey"))

                        val isAurora = isIn.invoke(holder, AURORA_MOON) as Boolean
                        val isSuper = isIn.invoke(holder, SUPER_MOON) as Boolean
                        if (!isAurora) return weight

                        val cfg = LunarBoostConfig.getInstance()
                        val multiplier = if (isSuper) cfg.superAuroraMoonRarePokemonSpawnMultiplier
                                         else cfg.auroraMoonRarePokemonSpawnMultiplier

                        return when (bucket.name) {
                            "common", "uncommon" -> weight / multiplier
                            "rare", "ultra-rare" -> weight * multiplier
                            else -> weight
                        }
                    } catch (t: Throwable) {
                        System.err.println("Journey's End: Aurora affectBucketWeight() failed: ${t.message}")
                        return weight
                    }
                }
            }
        }
    }
}
