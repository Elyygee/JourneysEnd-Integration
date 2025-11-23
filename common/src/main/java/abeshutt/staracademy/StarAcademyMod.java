package abeshutt.staracademy;

import abeshutt.staracademy.attribute.Attributes;
import abeshutt.staracademy.event.CommonEvents;
import abeshutt.staracademy.init.ModConfigs;
import abeshutt.staracademy.init.ModItems;
import abeshutt.staracademy.init.ModRegistries;
import com.cobblemon.mod.common.CobblemonItems;
import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.pokemon.ShinyChanceCalculationEvent;
import com.cobblemon.mod.common.pokemon.Pokemon;
import abeshutt.staracademy.block.entity.ShinyPokedollCollectorBlockEntity;
import abeshutt.staracademy.init.ModBlocks;
import net.minecraft.util.math.BlockPos;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.platform.Platform;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public final class StarAcademyMod {

    public static final ThreadLocal<Boolean> FORCE_SPAWNING = ThreadLocal.withInitial(() -> false);
    public static final ThreadLocal<Long> QUEST_ID = ThreadLocal.withInitial(() -> 0L);
    public static RegistryWrapper.WrapperLookup REGISTRIES;

    public static final String ID = "journeysend";
    public static final Logger LOGGER = LogManager.getLogger(ID);

    public static final RegistryKey<World> SAFARI = RegistryKey.of(RegistryKeys.WORLD, StarAcademyMod.id("safari"));
    public static List<Runnable> CLIENT_TICKERS = new ArrayList<>();

    public static void init() {
        LifecycleEvent.SERVER_STARTED.register(instance -> {
            REGISTRIES = instance.getRegistryManager();
        });


        ModRegistries.register();
        Attributes.init();
        
        // Initialize booster visuals sync system
        abeshutt.staracademy.net.BoosterVisualsSync.initEvents();
        
        // Initialize client cache
        abeshutt.staracademy.client.BoosterVisualsClientCache.init();
        
        // Initialize modifier display sync system
        abeshutt.staracademy.net.BoosterModifiersSync.initEvents();
        
        // Initialize modifier display client cache
        abeshutt.staracademy.client.ModifierDisplayClientCache.init();
        
        // Initialize Enhanced Celestials compatibility
        tryRegisterEnhancedCelestialsCompat();
        
        // Register lunar event commands (platform-specific)
        tryRegisterLunarCommands();
        
        // Register shiny boost from Shiny Pokedoll Collectors
        registerShinyPokedollCollectorBoost();

        CommonEvents.POKEMON_SENT_PRE.register(event -> {
            if(event.getLevel().getRegistryKey() == SAFARI) {
                event.cancel();
            }
        });

        CommonEvents.BATTLE_STARTED_PRE.register(event -> {
            if(event.getBattle().getPlayers().stream().anyMatch(player -> player.getWorld().getRegistryKey() == SAFARI)) {
                event.cancel();
            }
        });

        CommonEvents.POKEMON_CATCH_RATE.register(event -> {
            // Prevent non-Safari Balls from catching in Safari dimension
            if(event.getThrower().getWorld().getRegistryKey() == SAFARI) {
                // Check if this is any Safari Ball (original or custom)
                var pokeBallItem = event.getPokeBallEntity().getPokeBall().item();
                boolean isSafariBall = pokeBallItem == CobblemonItems.SAFARI_BALL 
                    || pokeBallItem == ModItems.GREAT_SAFARI_BALL.get() 
                    || pokeBallItem == ModItems.GOLDEN_SAFARI_BALL.get();
                
                if(!isSafariBall) {
                    // Set catch rate to 0 to prevent non-Safari Balls from working
                    event.setCatchRate(0.0F);
                }
            }
            
            // Prevent Safari Balls from working outside Safari dimension
            else {
                var pokeBallItem = event.getPokeBallEntity().getPokeBall().item();
                boolean isSafariBall = pokeBallItem == CobblemonItems.SAFARI_BALL 
                    || pokeBallItem == ModItems.GREAT_SAFARI_BALL.get() 
                    || pokeBallItem == ModItems.GOLDEN_SAFARI_BALL.get();
                
                if(isSafariBall) {
                    // Safari Balls don't work outside the Safari dimension
                    event.setCatchRate(0.0F);
                }
            }
        }, Priority.LOWEST);

        // Sync card configs to clients when they join
        dev.architectury.event.events.common.PlayerEvent.PLAYER_JOIN.register(player -> {
            if(player.getServer() != null) {
                dev.architectury.networking.NetworkManager.sendToPlayer(player, 
                    new abeshutt.staracademy.net.SyncCardConfigsS2CPacket());
            }
        });

        CommonEvents.POKEMON_ENTITY_SPAWN.register(event -> {
            if(FORCE_SPAWNING.get()) {
                return;
            }

            World world = event.getEntity().getEntityWorld();
            WorldBorder border = world.getWorldBorder();
            double dx = event.getEntity().getPos().getX() - border.getCenterX();
            double dz = event.getEntity().getPos().getZ() - border.getCenterZ();
            double distance = Math.sqrt(dx * dx + dz * dz);

            if(distance <= ModConfigs.POKEMON_SPAWN.getSpawnProtectionDistance()) {
                event.cancel();
                return;
            }

            /*
            ModConfigs.POKEMON_SPAWN.getLevel(distance).ifPresent(roll -> {
                event.getEntity().getPokemon().setLevel(roll.get(JavaRandom.ofNanoTime()));
            });*/
        }, Priority.HIGHEST);

        CommonEvents.POKEMON_ENTITY_SPAWN.register(event -> {
            MinecraftServer server = event.getEntity().getWorld().getServer();
            if(server == null) return;
            // Pokemon pokemon = event.getEntity().getPokemon(); // Unused for now

            /*
            if(FORCE_SPAWNING.get()) {
                List<String> prefixes = new ArrayList<>();
                if(pokemon.getShiny()) prefixes.add("Shiny");
                if(pokemon.isLegendary()) prefixes.add("Legendary");

                MutableText message = Text.empty()
                        .append(Text.literal("A ").formatted(Formatting.BOLD))
                        .append(Text.literal(String.join(" ", prefixes)).formatted(Formatting.BOLD))
                        .append(prefixes.isEmpty() ? Text.empty() : Text.literal(" "))
                        .append(event.getEntity().getDisplayName().copy().formatted(Formatting.BOLD))
                        .append(Text.literal(" has been summoned!").formatted(Formatting.BOLD));

                for(ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                    player.sendMessage(message);
                }
            } else {
                List<String> prefixes = new ArrayList<>();
                if(pokemon.getShiny()) prefixes.add("Shiny");
                if(pokemon.isLegendary()) prefixes.add("Legendary");

                if(pokemon.getShiny() || pokemon.isLegendary()) {
                    MutableText message = Text.empty()
                            .append(Text.literal("A ").formatted(Formatting.BOLD))
                            .append(Text.literal(String.join(" ", prefixes)).formatted(Formatting.BOLD))
                            .append(prefixes.isEmpty() ? Text.empty() : Text.literal(" "))
                            .append(event.getEntity().getDisplayName().copy().formatted(Formatting.BOLD))
                            .append(Text.literal(" has spawned near someone!").formatted(Formatting.BOLD));

                    for(ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                        player.sendMessage(message);
                    }
                }
            }*/
        }, Priority.LOWEST);
    }

    public static Identifier id(String path) {
        return Identifier.of(ID, path);
    }

    @Environment(EnvType.CLIENT)
    public static ModelIdentifier mid(Identifier id, String variant) {
        return new ModelIdentifier(id, variant);
    }

    @Environment(EnvType.CLIENT)
    public static ModelIdentifier mid(String name, String variant) {
        return StarAcademyMod.mid(StarAcademyMod.id(name), variant);
    }

    public static Text translatableText(String key, Object... args) {
        return Text.translatable(ID + "." + key, args);
    }

    private static void tryRegisterEnhancedCelestialsCompat() {
        try {
            LOGGER.info("Journey's End: Checking for Enhanced Celestials...");
            if (!Platform.isModLoaded("enhancedcelestials")) {
                LOGGER.info("Journey's End: Enhanced Celestials not found, skipping compatibility");
                return;
            }
            LOGGER.info("Journey's End: Enhanced Celestials found, attempting to register compatibility...");
            // Defer to a compat initializer loaded reflectively to avoid hard dep
            Class<?> clazz = Class.forName("abeshutt.staracademy.compat.enhancedcelestials.EnhancedCelestialsCompat");
            clazz.getDeclaredMethod("init").invoke(null);
            LOGGER.info("Journey's End: Enhanced Celestials compatibility registered successfully");
        } catch (Exception e) {
            LOGGER.warn("Journey's End: Failed to register Enhanced Celestials compatibility: {}", e.getMessage());
        }
    }

    private static void tryRegisterLunarCommands() {
        try {
            LOGGER.info("Journey's End: Attempting to register lunar event commands...");
            // Try Fabric-specific command registration first
            Class<?> fabricCommandClass = Class.forName("abeshutt.staracademy.fabric.FabricLunarEventCommand");
            LOGGER.info("Journey's End: Found FabricLunarEventCommand class");
            
            // Get the INSTANCE field for the Kotlin object
            Object instance = fabricCommandClass.getField("INSTANCE").get(null);
            fabricCommandClass.getDeclaredMethod("register").invoke(instance);
            LOGGER.info("Journey's End: Lunar event commands registered successfully");
        } catch (Exception e) {
            LOGGER.warn("Journey's End: Failed to register lunar event commands: {}", e.getMessage());
            e.printStackTrace();
        }
    }

    private static void registerShinyPokedollCollectorBoost() {
        CobblemonEvents.SHINY_CHANCE_CALCULATION.subscribe(event -> {
            try {
                // Access Kotlin properties using reflection
                java.lang.reflect.Field pokemonField = ShinyChanceCalculationEvent.class.getDeclaredField("pokemon");
                pokemonField.setAccessible(true);
                Pokemon pokemon = (Pokemon) pokemonField.get(event);
                
                // Access entity property from Pokemon (Kotlin property)
                java.lang.reflect.Field entityField = pokemon.getClass().getDeclaredField("entity");
                entityField.setAccessible(true);
                net.minecraft.entity.Entity entity = (net.minecraft.entity.Entity) entityField.get(pokemon);
                
                if (entity == null) {
                    return; // No entity, can't check position
                }
                
                net.minecraft.world.World world = entity.getEntityWorld();
                
                if (world.isClient()) {
                    return; // Only process on server
                }
                
                // Get config values
                float maxMultiplier = ModConfigs.POKEDOLLS.getMaxShinyBoostMultiplier();
                int radius = ModConfigs.POKEDOLLS.getShinyBoostRadius();
                
                if (maxMultiplier <= 1.0f || radius <= 0) {
                    return; // No boost configured
                }
                
                // Find nearby Shiny Pokedoll Collectors using chunk-based iteration for efficiency
                BlockPos entityPos = entity.getBlockPos();
                float totalProgress = 0.0f;
                int collectorCount = 0;
                
                // Calculate chunk bounds
                int minChunkX = (entityPos.getX() - radius) >> 4;
                int maxChunkX = (entityPos.getX() + radius) >> 4;
                int minChunkZ = (entityPos.getZ() - radius) >> 4;
                int maxChunkZ = (entityPos.getZ() + radius) >> 4;
                
                // Iterate through chunks and check block entities
                for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
                    for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                        if (!world.isChunkLoaded(chunkX, chunkZ)) {
                            continue; // Skip unloaded chunks
                        }
                        
                        net.minecraft.world.chunk.WorldChunk chunk = world.getChunk(chunkX, chunkZ);
                        if (chunk == null) {
                            continue;
                        }
                        
                        // Iterate through block entities in this chunk
                        for (java.util.Map.Entry<BlockPos, net.minecraft.block.entity.BlockEntity> entry : chunk.getBlockEntities().entrySet()) {
                            BlockPos pos = entry.getKey();
                            
                            // Check if within radius (using squared distance for efficiency)
                            int dx = pos.getX() - entityPos.getX();
                            int dy = pos.getY() - entityPos.getY();
                            int dz = pos.getZ() - entityPos.getZ();
                            int distSq = dx * dx + dy * dy + dz * dz;
                            
                            if (distSq > radius * radius) {
                                continue; // Outside radius
                            }
                            
                            // Check if this is a Shiny Pokedoll Collector
                            net.minecraft.block.entity.BlockEntity be = entry.getValue();
                            if (be instanceof ShinyPokedollCollectorBlockEntity collector) {
                                // Only count collectors that are lit (active)
                                net.minecraft.block.BlockState state = world.getBlockState(pos);
                                if (state.getBlock() == ModBlocks.SHINY_POKEDOLL_COLLECTOR.get() && 
                                    state.get(abeshutt.staracademy.block.ShinyPokedollCollectorBlock.LIT)) {
                                    totalProgress += collector.getProgress();
                                    collectorCount++;
                                }
                            }
                        }
                    }
                }
                
                // Calculate average progress and apply boost
                if (collectorCount > 0) {
                    float averageProgress = totalProgress / collectorCount;
                    // Calculate multiplier: 1.0 (no boost) to maxMultiplier (full boost)
                    // Linear interpolation: multiplier = 1.0 + (maxMultiplier - 1.0) * progress
                    float multiplier = 1.0f + (maxMultiplier - 1.0f) * averageProgress;
                    
                    // In Cobblemon, chance is the denominator (lower = better odds)
                    // So we divide by the multiplier to make it smaller (better odds)
                    java.lang.reflect.Field chanceField = ShinyChanceCalculationEvent.class.getDeclaredField("chance");
                    chanceField.setAccessible(true);
                    float currentChance = chanceField.getFloat(event);
                    
                    float newChance = currentChance / multiplier;
                    float modifier = currentChance - newChance;
                    event.addModifier(-modifier); // Negative modifier reduces the chance
                    
                    LOGGER.debug("Journey's End: Applied shiny boost from {} collectors (avg progress: {}%, multiplier: {}x)", 
                        collectorCount, String.format("%.2f", averageProgress * 100f), String.format("%.2f", multiplier));
                }
            } catch (Exception e) {
                LOGGER.warn("Journey's End: Failed to apply shiny boost from Pokedoll Collectors: {}", e.getMessage());
            }
        });
    }

}
