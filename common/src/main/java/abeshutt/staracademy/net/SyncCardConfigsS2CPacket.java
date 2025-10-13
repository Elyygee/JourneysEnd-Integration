package abeshutt.staracademy.net;

import abeshutt.staracademy.StarAcademyMod;
import abeshutt.staracademy.init.ModConfigs;
import abeshutt.staracademy.config.card.*;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.CustomPayload;

public class SyncCardConfigsS2CPacket extends ModPacket<ClientPlayNetworkHandler> {

    public static final Id<SyncCardConfigsS2CPacket> ID = new Id<>(StarAcademyMod.id("sync_card_configs_s2c"));

    public SyncCardConfigsS2CPacket() {
        // Empty constructor for network deserialization
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    @Override
    public void onReceive(ClientPlayNetworkHandler listener) {
        // Sync card DATA configs from server to client
        // Visual configs (icons, albums, displays) stay client-side for rendering
        if (ModConfigs.CARD_ENTRIES == null) {
            ModConfigs.CARD_ENTRIES = new CardEntriesConfig();
        }
        if (ModConfigs.CARD_BOOSTERS == null) {
            ModConfigs.CARD_BOOSTERS = new CardBoosterPacksConfig();
        }
        if (ModConfigs.CARD_RARITIES == null) {
            ModConfigs.CARD_RARITIES = new CardRaritiesConfig();
        }
        if (ModConfigs.CARD_MODIFIERS == null) {
            ModConfigs.CARD_MODIFIERS = new CardModifiersConfig();
        }
        if (ModConfigs.CARD_SCALARS == null) {
            ModConfigs.CARD_SCALARS = new CardScalarsConfig();
        }
        // Note: CARD_ICONS, CARD_ALBUMS, CARD_DISPLAYS are client-side only for rendering
    }

    @Override
    public void writeBits(abeshutt.staracademy.data.bit.BitBuffer buffer) {
        // No data to write for now - this is just a trigger packet
    }

    @Override
    public void readBits(abeshutt.staracademy.data.bit.BitBuffer buffer) {
        // No data to read for now - this is just a trigger packet
    }
}
