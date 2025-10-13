package abeshutt.staracademy.net;

import abeshutt.staracademy.StarAcademyMod;
import abeshutt.staracademy.card.BoosterPackVisual;
import abeshutt.staracademy.client.BoosterVisualsClientCache;
import abeshutt.staracademy.config.card.CardBoosterPacksConfig;
import abeshutt.staracademy.data.adapter.Adapters;
import abeshutt.staracademy.init.ModConfigs;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.CustomPayload;

import java.util.HashMap;
import java.util.Map;

/**
 * Server-side sync system for booster pack visuals
 */
public class BoosterVisualsSync extends ModPacket<ClientPlayNetworkHandler> {

    public static final Id<BoosterVisualsSync> ID = new Id<>(StarAcademyMod.id("booster_visuals_sync"));

    private Map<String, BoosterPackVisual> visuals;
    private String version;
    private String sha256;

    public BoosterVisualsSync() {
        // Empty constructor for network deserialization
    }

    public BoosterVisualsSync(Map<String, BoosterPackVisual> visuals, String version, String sha256) {
        this.visuals = visuals;
        this.version = version;
        this.sha256 = sha256;
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    @Override
    public void onReceive(ClientPlayNetworkHandler listener) {
        // Capture data on network thread, then execute on client thread
        var map = visuals;
        var v = version;
        var sh = sha256;
        net.minecraft.client.MinecraftClient.getInstance().execute(() ->
            BoosterVisualsClientCache.update(map, v, sh)
        );
    }

    @Override
    public void writeBits(abeshutt.staracademy.data.bit.BitBuffer buffer) {
        var v = version != null ? version : "0";
        var sh = sha256 != null ? sha256 : "";
        var map = (visuals != null) ? visuals : Map.of();

        Adapters.UTF_8.writeBits(v, buffer);
        Adapters.UTF_8.writeBits(sh, buffer);
        Adapters.INT_SEGMENTED_3.writeBits(map.size(), buffer);
        for (var entry : map.entrySet()) {
            String k = (String) entry.getKey();
            BoosterPackVisual b = (BoosterPackVisual) entry.getValue();
            Adapters.UTF_8.writeBits(k, buffer);
            Adapters.UTF_8.writeBits(b.iconId(), buffer);
            Adapters.INT.writeBits(b.tintRgb(), buffer);
            Adapters.BOOLEAN.writeBits(b.foil(), buffer);
        }
    }

    @Override
    public void readBits(abeshutt.staracademy.data.bit.BitBuffer buffer) {
        version = Adapters.UTF_8.readBits(buffer).orElse("");
        sha256 = Adapters.UTF_8.readBits(buffer).orElse("");
        int size = Adapters.INT_SEGMENTED_3.readBits(buffer).orElse(0);

        var tmp = new HashMap<String, BoosterPackVisual>(Math.max(0, size));
        for (int i = 0; i < size; i++) {
            var key = Adapters.UTF_8.readBits(buffer).orElse("");
            var iconId = Adapters.UTF_8.readBits(buffer).orElse("journeysend:icons/booster_default");
            var tint = Adapters.INT.readBits(buffer).orElse(0xFFFFFF);
            var foil = Adapters.BOOLEAN.readBits(buffer).orElse(false);
            tmp.put(key, new BoosterPackVisual(iconId, tint, foil));
        }
        visuals = Map.copyOf(tmp); // immutable snapshot
    }

    /**
     * Build the payload from server-only config
     */
    public static Map<String, BoosterPackVisual> buildSnapshot() {
        CardBoosterPacksConfig cfg = ModConfigs.CARD_BOOSTERS;
        if (cfg == null || cfg.getValues() == null) {
            return Map.of();
        }
        
        Map<String, BoosterPackVisual> result = new HashMap<>();
        cfg.getValues().forEach((id, entry) -> {
            // Ensure non-null key and extract visual data from the booster pack entry
            if (id != null && !id.isEmpty()) {
                // Extract actual visual data from the booster pack entry
                String iconId = entry.getModelBase() != null ? entry.getModelBase().toString() : "journeysend:icons/booster_" + id;
                int tintRgb = entry.getColor(); // Use the actual color from the entry
                boolean foil = false; // Default no foil (could be extended in the future)
                
                result.put(id, new BoosterPackVisual(iconId, tintRgb, foil));
            }
        });
        
        return Map.copyOf(result);
    }

    /**
     * Send visual data to a specific player
     */
    public static void sendTo(net.minecraft.server.network.ServerPlayerEntity player) {
        var cfg = ModConfigs.CARD_BOOSTERS;
        var map = buildSnapshot();
        // Generate version based on config content hash
        var version = "1";
        var sha256 = "boosters@" + map.size(); // Simple hash based on entry count
        
        dev.architectury.networking.NetworkManager.sendToPlayer(player, new BoosterVisualsSync(map, version, sha256));
    }

    /**
     * Initialize server-side events
     */
    public static void initEvents() {
        // Send to players when they join
        dev.architectury.event.events.common.PlayerEvent.PLAYER_JOIN.register(player -> {
            if (player.getServer() != null) {
                sendTo(player);
            }
        });
        
        // Note: Config reload sync can be added later if needed
        // For now, players will get updated visuals when they rejoin
    }
}
