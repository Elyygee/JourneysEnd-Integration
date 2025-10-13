package abeshutt.staracademy.net;

import abeshutt.staracademy.StarAcademyMod;
import abeshutt.staracademy.card.AttributeUtils;
import abeshutt.staracademy.card.CardModifierEntry;
import abeshutt.staracademy.data.adapter.Adapters;
import abeshutt.staracademy.data.bit.BitBuffer;
import abeshutt.staracademy.init.ModConfigs;
import abeshutt.staracademy.net.dto.ModifierDisplayDTO;
import abeshutt.staracademy.world.roll.UniformNumberRoll;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.CustomPayload;

import java.util.HashMap;
import java.util.Map;

public class BoosterModifiersSync extends ModPacket<ClientPlayNetworkHandler> {

    public static final Id<BoosterModifiersSync> ID =
        new Id<>(StarAcademyMod.id("booster_modifiers_sync"));

    private Map<String, ModifierDisplayDTO> map;
    private String version;
    private String sha256;

    public BoosterModifiersSync() {}
    
    public BoosterModifiersSync(Map<String, ModifierDisplayDTO> map, String version, String sha256) {
        this.map = map; 
        this.version = version; 
        this.sha256 = sha256;
    }

    @Override 
    public Id<? extends CustomPayload> getId() { 
        return ID; 
    }

    @Override 
    public void onReceive(ClientPlayNetworkHandler listener) {
        var client = net.minecraft.client.MinecraftClient.getInstance();
        var m = map; 
        var v = version; 
        var s = sha256;
        client.execute(() -> abeshutt.staracademy.client.ModifierDisplayClientCache.update(m, v, s));
    }

    @Override 
    public void writeBits(BitBuffer buf) {
        var m = (map != null) ? map : Map.of();
        Adapters.UTF_8.writeBits(version != null ? version : "1", buf);
        Adapters.UTF_8.writeBits(sha256 != null ? sha256 : "", buf);
        Adapters.INT_SEGMENTED_3.writeBits(m.size(), buf);
        for (Map.Entry<String, ModifierDisplayDTO> entry : ((Map<String, ModifierDisplayDTO>) m).entrySet()) {
            String id = entry.getKey();
            ModifierDisplayDTO dto = entry.getValue();
            Adapters.UTF_8.writeBits(id, buf);
            Adapters.UTF_8.writeBits(dto.nameKey(), buf);
            Adapters.INT.writeBits(dto.colorRgb(), buf);
            Adapters.UTF_8.writeBits(dto.styleId(), buf);
            Adapters.DOUBLE.writeBits(dto.min(), buf);
            Adapters.DOUBLE.writeBits(dto.max(), buf);
            Adapters.DOUBLE.writeBits(dto.resolution(), buf);
        }
    }

    @Override 
    public void readBits(BitBuffer buf) {
        version = Adapters.UTF_8.readBits(buf).orElse("1");
        sha256  = Adapters.UTF_8.readBits(buf).orElse("");
        int n   = Adapters.INT_SEGMENTED_3.readBits(buf).orElse(0);
        var tmp = new HashMap<String, ModifierDisplayDTO>(n);
        for (int i=0;i<n;i++) {
            var id   = Adapters.UTF_8.readBits(buf).orElse("unknown");
            var key  = Adapters.UTF_8.readBits(buf).orElse("text.academy.card.modifier.unknown");
            int col  = Adapters.INT.readBits(buf).orElse(0x00AA00);
            var sty  = Adapters.UTF_8.readBits(buf).orElse("plain");
            double mn = Adapters.DOUBLE.readBits(buf).orElse(0d);
            double mx = Adapters.DOUBLE.readBits(buf).orElse(0d);
            double res= Adapters.DOUBLE.readBits(buf).orElse(0.001d);
            tmp.put(id, new ModifierDisplayDTO(id, key, col, sty, mn, mx, res));
        }
        map = Map.copyOf(tmp);
    }

    public static Map<String, ModifierDisplayDTO> buildSnapshot() {
        return ModifierDisplaySnapshot.build();
    }

    public static void sendTo(net.minecraft.server.network.ServerPlayerEntity player) {
        var map = buildSnapshot();
        var version = "1";     // or cfg.getSchemaVersion()
        var sha256  = "mods";  // or real hash if you compute it
        dev.architectury.networking.NetworkManager.sendToPlayer(player, new BoosterModifiersSync(map, version, sha256));
    }

    public static void initEvents() {
        dev.architectury.event.events.common.PlayerEvent.PLAYER_JOIN.register(player -> {
            if (player.getServer() != null) sendTo(player);
        });
    }
}
