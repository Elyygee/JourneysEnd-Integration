package abeshutt.staracademy.client;

import abeshutt.staracademy.card.BoosterPackVisual;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;

import java.util.Map;

/**
 * Client-side cache for synced booster pack visual data
 */
public final class BoosterVisualsClientCache {
    private static volatile Map<String, BoosterPackVisual> map = Map.of();
    private static volatile String version = "0";
    private static volatile String sha256 = "";

    /**
     * Update the cache with new data from server
     */
    public static void update(Map<String, BoosterPackVisual> newMap, String newVersion, String newSha256) {
        // Skip redundant updates to avoid churn
        if (java.util.Objects.equals(newSha256, sha256)) {
            return;
        }
        
        map = Map.copyOf(newMap);
        version = newVersion;
        sha256 = newSha256;
    }

    /**
     * Get visual data for a booster pack ID
     */
    public static BoosterPackVisual get(String id) {
        return map.getOrDefault(id, BoosterPackVisual.DEFAULT);
    }

    /**
     * Check if the cache is ready (has data)
     */
    public static boolean ready() {
        return !map.isEmpty();
    }

    /**
     * Get all cached visuals (for iteration)
     */
    public static Map<String, BoosterPackVisual> getAll() {
        return map;
    }

    /**
     * Get current version
     */
    public static String getVersion() {
        return version;
    }

    /**
     * Get current SHA256
     */
    public static String getSha256() {
        return sha256;
    }

    /**
     * Install the client receiver (called during client init)
     */
    public static void installReceiver() {
        if (Platform.getEnvironment() == Env.CLIENT) {
            // Receiver is automatically installed via network registration
            // This method exists for future extensibility
        }
    }

    /**
     * Initialize the client cache (called during client init)
     */
    public static void init() {
        if (Platform.getEnvironment() == Env.CLIENT) {
            installReceiver();
        }
    }
}
