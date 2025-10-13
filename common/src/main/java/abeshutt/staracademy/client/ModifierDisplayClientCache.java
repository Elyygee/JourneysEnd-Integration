package abeshutt.staracademy.client;

import abeshutt.staracademy.net.dto.ModifierDisplayDTO;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;

import java.util.Map;
import java.util.Objects;

/**
 * Client-side cache for modifier display data received from the server.
 * This ensures that tooltip rendering logic on the client always has access to display data,
 * even before the server has fully synced, preventing NullPointerExceptions.
 */
public final class ModifierDisplayClientCache {
    private static volatile Map<String, ModifierDisplayDTO> MAP = Map.of();
    private static volatile String VERSION = "0", SHA = "";

    /**
     * Update the cache with new data from server
     */
    public static void update(Map<String, ModifierDisplayDTO> map, String version, String sha) {
        if (Objects.equals(SHA, sha)) return; // skip identical payload
        MAP = Map.copyOf(map);
        VERSION = version != null ? version : "0";
        SHA = sha != null ? sha : "";
    }

    /**
     * Get display data for a modifier ID
     */
    public static ModifierDisplayDTO get(String id) {
        return MAP.getOrDefault(id, ModifierDisplayDTO.UNKNOWN);
    }

    /**
     * Check if the cache is ready (has data)
     */
    public static boolean ready() { 
        return !MAP.isEmpty(); 
    }

    /**
     * Get current version
     */
    public static String getVersion() {
        return VERSION;
    }

    /**
     * Get current SHA256 hash
     */
    public static String getSha256() {
        return SHA;
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
