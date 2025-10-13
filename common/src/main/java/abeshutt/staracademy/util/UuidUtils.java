package abeshutt.staracademy.util;

import java.util.UUID;

public class UuidUtils {
    
    /**
     * Safely converts UUID to string to avoid Kotlin UUID conflicts
     */
    public static String toString(UUID uuid) {
        if (uuid == null) return null;
        return String.format("%08x-%04x-%04x-%04x-%012x",
            uuid.getMostSignificantBits() >>> 32,
            (uuid.getMostSignificantBits() >>> 16) & 0xFFFF,
            uuid.getMostSignificantBits() & 0xFFFF,
            uuid.getLeastSignificantBits() >>> 48,
            uuid.getLeastSignificantBits() & 0xFFFFFFFFFFFFL);
    }
}
