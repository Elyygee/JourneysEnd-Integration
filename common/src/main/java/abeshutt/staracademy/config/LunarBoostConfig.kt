package abeshutt.staracademy.config

import com.google.gson.annotations.Expose
import net.fabricmc.loader.api.FabricLoader
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.nio.file.Path

/**
 * Configuration for lunar event boosts
 * This file is automatically created and can be edited by server admins
 */
class LunarBoostConfig : LunarConfigBase() {
    
    // Blue Moon - Shiny Pokemon Boost
    @Expose
    var blueMoonShinyMultiplier: Float = 1.5f
    
    @Expose
    var superBlueMoonShinyMultiplier: Float = 2.0f
    
    // Blood Moon - IV Boost
    @Expose
    var bloodMoonIVsMultiplier: Float = 1.2f
    
    @Expose
    var superBloodMoonIVsMultiplier: Float = 2.0f
    
    // Harvest Moon - EXP Share Boost
    @Expose
    var harvestMoonExpShareMultiplier: Double = 1.2
    
    @Expose
    var superHarvestMoonExpShareMultiplier: Double = 2.0
    
    // Aurora Moon - Rare Pokemon Spawn Boost
    @Expose
    var auroraMoonRarePokemonSpawnMultiplier: Float = 4.8f
    
    @Expose
    var superAuroraMoonRarePokemonSpawnMultiplier: Float = 8.0f
    
    // Lunar Forecast Hologram Settings
    @Expose
    var hologramSwitchTime: Int = 20 // ticks between switches (1 second at 20 TPS)
    
    @Expose
    var forecastDayView: Int = 7 // days ahead to show in forecast
    
    // Configuration file path
    override fun getPath(): String {
        val configDir = FabricLoader.getInstance().configDir.toFile()
        val journeysEndDir = File(configDir, "JourneysEnd")
        if (!journeysEndDir.exists()) {
            journeysEndDir.mkdirs()
        }
        return File(journeysEndDir, "lunar_boosts").absolutePath
    }
    
    override fun reset() {
        // Blue Moon defaults
        blueMoonShinyMultiplier = 1.5f
        superBlueMoonShinyMultiplier = 2.0f
        
        // Blood Moon defaults
        bloodMoonIVsMultiplier = 1.2f
        superBloodMoonIVsMultiplier = 2.0f
        
        // Harvest Moon defaults
        harvestMoonExpShareMultiplier = 1.2
        superHarvestMoonExpShareMultiplier = 2.0
        
        // Aurora Moon defaults
        auroraMoonRarePokemonSpawnMultiplier = 4.8f
        superAuroraMoonRarePokemonSpawnMultiplier = 8.0f
        
        // Hologram defaults
        hologramSwitchTime = 20
        forecastDayView = 7
    }
    
    companion object {
        private var instance: LunarBoostConfig? = null
        
        fun getInstance(): LunarBoostConfig {
            if (instance == null) {
                instance = LunarBoostConfig()
                instance!!.load()
            }
            return instance!!
        }
        
        fun reload() {
            instance = LunarBoostConfig()
            instance!!.load()
        }
    }
}

/**
 * Base class for file-based configuration
 */
abstract class LunarConfigBase {
    abstract fun getPath(): String
    abstract fun reset()
    
    fun load() {
        val configFile = File(getPath() + ".json")
        if (configFile.exists()) {
            try {
                val gson = com.google.gson.GsonBuilder()
                    .setPrettyPrinting()
                    .excludeFieldsWithoutExposeAnnotation()
                    .create()
                
                val reader = FileReader(configFile)
                val config = gson.fromJson(reader, this::class.java)
                reader.close()
                
                // Copy loaded values to this instance
                copyFrom(config)
            } catch (e: Exception) {
                println("Failed to load config file: ${e.message}")
                reset()
                save()
            }
        } else {
            reset()
            save()
        }
    }
    
    fun save() {
        val configFile = File(getPath() + ".json")
        try {
            val gson = com.google.gson.GsonBuilder()
                .setPrettyPrinting()
                .excludeFieldsWithoutExposeAnnotation()
                .create()
            
            val writer = FileWriter(configFile)
            gson.toJson(this, writer)
            writer.close()
        } catch (e: Exception) {
            println("Failed to save config file: ${e.message}")
        }
    }
    
    private fun copyFrom(other: LunarConfigBase) {
        // Use reflection to copy all @Expose fields
        val fields = this::class.java.declaredFields
        val otherFields = other::class.java.declaredFields
        
        for (field in fields) {
            if (field.isAnnotationPresent(Expose::class.java)) {
                field.isAccessible = true
                val otherField = otherFields.find { it.name == field.name }
                if (otherField != null) {
                    otherField.isAccessible = true
                    try {
                        field.set(this, otherField.get(other))
                    } catch (e: Exception) {
                        println("Failed to copy field ${field.name}: ${e.message}")
                    }
                }
            }
        }
    }
}
