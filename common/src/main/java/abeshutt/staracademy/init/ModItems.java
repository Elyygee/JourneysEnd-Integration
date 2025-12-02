package abeshutt.staracademy.init;

import abeshutt.staracademy.item.*;
import com.cobblemon.mod.common.CobblemonItems;
import com.cobblemon.mod.common.item.PokeBallItem;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

import java.util.Set;
import java.util.function.Supplier;

public class ModItems extends ModRegistries {

    public static RegistrySupplier<Item> DEX_REWARDS;
    public static RegistrySupplier<OutfitItem> OUTFIT;
    public static RegistrySupplier<SafariTicketBaseItem> SAFARI_TICKET_BASE;
    public static RegistrySupplier<SafariTicketGreatItem> SAFARI_TICKET_GREAT;
    public static RegistrySupplier<SafariTicketGoldenItem> SAFARI_TICKET_GOLDEN;
    public static RegistrySupplier<Item> HA_FOSSIL;
    public static RegistrySupplier<Item> MAX_IV_FOSSIL;
    public static RegistrySupplier<Item> SHINY_FOSSIL;
    public static RegistrySupplier<Item> SHINY_INCENSE;
    public static RegistrySupplier<Item> STRONG_SHINY_INCENSE;
    public static RegistrySupplier<Item> UBER_SHINY_INCENSE;
    public static RegistrySupplier<Item> CARD;
    public static RegistrySupplier<Item> BASE_BOOSTER_PACK;
    public static RegistrySupplier<Item> CARD_ALBUM;
    public static RegistrySupplier<Item> LEGENDARY_PLACEHOLDER;
    public static RegistrySupplier<AcceptanceLetterItem> ACCEPTANCE_LETTER;
    public static RegistrySupplier<Item> GREAT_SAFARI_BALL;
    public static RegistrySupplier<Item> GOLDEN_SAFARI_BALL;

    public static void register() {
        DEX_REWARDS = register("rewards", () -> new DexRewardsItem(new Item.Settings().maxCount(1).fireproof()));
        OUTFIT = register("outfit", OutfitItem::new);
        SAFARI_TICKET_BASE = register("safari_ticket_base", SafariTicketBaseItem::new);
        SAFARI_TICKET_GREAT = register("safari_ticket_great", SafariTicketGreatItem::new);
        SAFARI_TICKET_GOLDEN = register("safari_ticket_golden", SafariTicketGoldenItem::new);
        HA_FOSSIL = register("ha_fossil", () -> new Item(new Item.Settings().maxCount(1)));
        MAX_IV_FOSSIL = register("max_iv_fossil", () -> new Item(new Item.Settings().maxCount(1)));
        SHINY_FOSSIL = register("shiny_fossil", () -> new Item(new Item.Settings().maxCount(1)));
        SHINY_INCENSE = register("shiny_incense", () -> new Item(new Item.Settings().maxCount(1)));
        STRONG_SHINY_INCENSE = register("strong_shiny_incense", () -> new Item(new Item.Settings().maxCount(1)));
        UBER_SHINY_INCENSE = register("uber_shiny_incense", () -> new Item(new Item.Settings().maxCount(1)));
        CARD = register("card", CardItem::new);
        BASE_BOOSTER_PACK = register("booster_pack", () -> new BaseBoosterPackItem(new Item.Settings().fireproof().maxCount(1)));
        CARD_ALBUM = register("card_album", CardAlbumItem::new);
        LEGENDARY_PLACEHOLDER = register("legendary_placeholder", () -> new Item(new Item.Settings().maxCount(1)));
        ACCEPTANCE_LETTER = register("acceptance_letter", AcceptanceLetterItem::new);
        // Register Safari Balls as PokeBallItems
        GREAT_SAFARI_BALL = register("great_safari_ball", () -> {
            PokeBallItem item = new PokeBallItem(ModPokeBalls.GREAT_SAFARI_BALL);
            ModPokeBalls.GREAT_SAFARI_BALL.item = item;
            return item;
        });
        GOLDEN_SAFARI_BALL = register("golden_safari_ball", () -> {
            PokeBallItem item = new PokeBallItem(ModPokeBalls.GOLDEN_SAFARI_BALL);
            ModPokeBalls.GOLDEN_SAFARI_BALL.item = item;
            return item;
        });

        // Roasted berries that are not provided natively by CobbleCuisine
        register("roasted_aguav_berry", () -> new FoodItem(Rarity.COMMON, 8, 1.2f, true, false));
        register("roasted_apicot_berry", () -> new FoodItem(Rarity.COMMON, 8, 1.2f, true, false));
        // CobbleCuisine provides roasted_aspear_berry natively
        // register("roasted_aspear_berry", () -> new FoodItem(Rarity.COMMON, 6, 0.8f, true, false));
        // CobbleCuisine provides roasted_babiri_berry natively
        // register("roasted_babiri_berry", () -> new FoodItem(Rarity.COMMON, 6, 0.8f, true, false));
        register("roasted_belue_berry", () -> new FoodItem(Rarity.COMMON, 8, 1.2f, true, false));
        register("roasted_bluk_berry", () -> new FoodItem(Rarity.COMMON, 6, 0.8f, true, false));
        // CobbleCuisine provides charti, cheri, chesto, chilan, chople, coba, colbur natively
        // register("roasted_charti_berry", () -> new FoodItem(Rarity.COMMON, 6, 0.8f, true, false));
        // register("roasted_cheri_berry", () -> new FoodItem(Rarity.COMMON, 6, 0.8f, true, false));
        // register("roasted_chesto_berry", () -> new FoodItem(Rarity.COMMON, 6, 0.8f, true, false));
        // register("roasted_chilan_berry", () -> new FoodItem(Rarity.COMMON, 6, 0.8f, true, false));
        // register("roasted_chople_berry", () -> new FoodItem(Rarity.COMMON, 6, 0.8f, true, false));
        // register("roasted_coba_berry", () -> new FoodItem(Rarity.COMMON, 6, 0.8f, true, false));
        // register("roasted_colbur_berry", () -> new FoodItem(Rarity.COMMON, 6, 0.8f, true, false));
        register("roasted_cornn_berry", () -> new FoodItem(Rarity.COMMON, 8, 1.2f, true, false));
        register("roasted_custap_berry", () -> new FoodItem(Rarity.COMMON, 8, 1.2f, true, false));
        register("roasted_durin_berry", () -> new FoodItem(Rarity.COMMON, 8, 1.2f, true, false));
        // CobbleCuisine provides roasted_enigma_berry natively
        // register("roasted_enigma_berry", () -> new FoodItem(Rarity.COMMON, 8, 1.2f, true, false));
        register("roasted_figy_berry", () -> new FoodItem(Rarity.COMMON, 8, 1.2f, true, false));
        register("roasted_ganlon_berry", () -> new FoodItem(Rarity.COMMON, 8, 1.2f, true, false));
        register("roasted_grepa_berry", () -> new FoodItem(Rarity.COMMON, 8, 1.2f, true, false));
        // CobbleCuisine provides roasted_haban_berry natively
        // register("roasted_haban_berry", () -> new FoodItem(Rarity.COMMON, 6, 0.8f, true, false));
        register("roasted_hondew_berry", () -> new FoodItem(Rarity.COMMON, 8, 1.2f, true, false));
        register("roasted_hopo_berry", () -> new FoodItem(Rarity.COMMON, 8, 1.2f, true, false));
        register("roasted_iapapa_berry", () -> new FoodItem(Rarity.COMMON, 8, 1.2f, true, false));
        register("roasted_jaboca_berry", () -> new FoodItem(Rarity.COMMON, 8, 1.2f, true, false));
        // CobbleCuisine provides roasted_kasib_berry and roasted_kebia_berry natively
        // register("roasted_kasib_berry", () -> new FoodItem(Rarity.COMMON, 6, 0.8f, true, false));
        // register("roasted_kebia_berry", () -> new FoodItem(Rarity.COMMON, 6, 0.8f, true, false));
        register("roasted_kee_berry", () -> new FoodItem(Rarity.COMMON, 8, 1.2f, true, false));
        register("roasted_kelpsy_berry", () -> new FoodItem(Rarity.COMMON, 8, 1.2f, true, false));
        register("roasted_lansat_berry", () -> new FoodItem(Rarity.COMMON, 8, 1.2f, true, false));
        // CobbleCuisine provides roasted_leppa_berry natively
        // register("roasted_leppa_berry", () -> new FoodItem(Rarity.COMMON, 8, 1.2f, true, false));
        register("roasted_liechi_berry", () -> new FoodItem(Rarity.COMMON, 8, 1.2f, true, false));
        // CobbleCuisine provides roasted_lum_berry natively
        // register("roasted_lum_berry", () -> new FoodItem(Rarity.COMMON, 8, 1.2f, true, false));
        register("roasted_mago_berry", () -> new FoodItem(Rarity.COMMON, 8, 1.2f, true, false));
        register("roasted_magost_berry", () -> new FoodItem(Rarity.COMMON, 8, 1.2f, true, false));
        register("roasted_maranga_berry", () -> new FoodItem(Rarity.COMMON, 8, 1.2f, true, false));
        register("roasted_micle_berry", () -> new FoodItem(Rarity.COMMON, 8, 1.2f, true, false));
        register("roasted_nanab_berry", () -> new FoodItem(Rarity.COMMON, 6, 0.8f, true, false));
        register("roasted_nomel_berry", () -> new FoodItem(Rarity.COMMON, 8, 1.2f, true, false));
        // CobbleCuisine provides roasted_occa_berry and roasted_oran_berry natively
        // register("roasted_occa_berry", () -> new FoodItem(Rarity.COMMON, 6, 0.8f, true, false));
        // register("roasted_oran_berry", () -> new FoodItem(Rarity.COMMON, 6, 0.8f, true, false));
        register("roasted_pamtre_berry", () -> new FoodItem(Rarity.COMMON, 8, 1.2f, true, false));
        // CobbleCuisine provides roasted_passho_berry, roasted_payapa_berry, roasted_pecha_berry, roasted_persim_berry natively
        // register("roasted_passho_berry", () -> new FoodItem(Rarity.COMMON, 6, 0.8f, true, false));
        // register("roasted_payapa_berry", () -> new FoodItem(Rarity.COMMON, 6, 0.8f, true, false));
        // register("roasted_pecha_berry", () -> new FoodItem(Rarity.COMMON, 6, 0.8f, true, false));
        // register("roasted_persim_berry", () -> new FoodItem(Rarity.COMMON, 6, 0.8f, true, false));
        register("roasted_petaya_berry", () -> new FoodItem(Rarity.COMMON, 8, 1.2f, true, false));
        register("roasted_pinap_berry", () -> new FoodItem(Rarity.COMMON, 6, 0.8f, true, false));
        register("roasted_pomeg_berry", () -> new FoodItem(Rarity.COMMON, 8, 1.2f, true, false));
        register("roasted_qualot_berry", () -> new FoodItem(Rarity.COMMON, 8, 1.2f, true, false));
        register("roasted_rabuta_berry", () -> new FoodItem(Rarity.COMMON, 8, 1.2f, true, false));
        // CobbleCuisine provides roasted_rawst_berry natively
        // register("roasted_rawst_berry", () -> new FoodItem(Rarity.COMMON, 6, 0.8f, true, false));
        register("roasted_razz_berry", () -> new FoodItem(Rarity.COMMON, 6, 0.8f, true, false));
        // CobbleCuisine provides roasted_rindo_berry and roasted_roseli_berry natively
        // register("roasted_rindo_berry", () -> new FoodItem(Rarity.COMMON, 6, 0.8f, true, false));
        // register("roasted_roseli_berry", () -> new FoodItem(Rarity.COMMON, 8, 1.2f, true, false));
        register("roasted_rowap_berry", () -> new FoodItem(Rarity.COMMON, 8, 1.2f, true, false));
        register("roasted_salac_berry", () -> new FoodItem(Rarity.COMMON, 8, 1.2f, true, false));
        // CobbleCuisine provides roasted_shuca_berry and roasted_sitrus_berry natively
        // register("roasted_shuca_berry", () -> new FoodItem(Rarity.COMMON, 6, 0.8f, true, false));
        // register("roasted_sitrus_berry", () -> new FoodItem(Rarity.COMMON, 8, 1.2f, true, false));
        register("roasted_spelon_berry", () -> new FoodItem(Rarity.COMMON, 8, 1.2f, true, false));
        register("roasted_starf_berry", () -> new FoodItem(Rarity.COMMON, 8, 1.2f, true, false));
        register("roasted_tamato_berry", () -> new FoodItem(Rarity.COMMON, 8, 1.2f, true, false));
        // CobbleCuisine provides roasted_tanga_berry natively
        // register("roasted_tanga_berry", () -> new FoodItem(Rarity.COMMON, 6, 0.8f, true, false));
        register("roasted_touga_berry", () -> new FoodItem(Rarity.COMMON, 8, 1.2f, true, false));
        // CobbleCuisine provides roasted_wacan_berry natively
        // register("roasted_wacan_berry", () -> new FoodItem(Rarity.COMMON, 6, 0.8f, true, false));
        register("roasted_watmel_berry", () -> new FoodItem(Rarity.COMMON, 8, 1.2f, true, false));
        register("roasted_wepear_berry", () -> new FoodItem(Rarity.COMMON, 6, 0.8f, true, false));
        register("roasted_wiki_berry", () -> new FoodItem(Rarity.COMMON, 8, 1.2f, true, false));
        // CobbleCuisine provides roasted_yache_berry natively
        // register("roasted_yache_berry", () -> new FoodItem(Rarity.COMMON, 6, 0.8f, true, false));
        register("smoked_black_apricorn", () -> new FoodItem(Rarity.COMMON, 4, 0.4f, true, false));
        register("smoked_blue_apricorn", () -> new FoodItem(Rarity.COMMON, 4, 0.4f, true, false));
        register("smoked_green_apricorn", () -> new FoodItem(Rarity.COMMON, 4, 0.4f, true, false));
        register("smoked_pink_apricorn", () -> new FoodItem(Rarity.COMMON, 4, 0.4f, true, false));
        register("smoked_red_apricorn", () -> new FoodItem(Rarity.COMMON, 4, 0.4f, true, false));
        register("smoked_white_apricorn", () -> new FoodItem(Rarity.COMMON, 4, 0.4f, true, false));
        register("smoked_yellow_apricorn", () -> new FoodItem(Rarity.COMMON, 4, 0.4f, true, false));
        register("iron_smoked_black_apricorn", () -> new FoodItem(Rarity.RARE, 4, 1.0f, true, true, new FoodComponent.StatusEffectEntry(new StatusEffectInstance(StatusEffects.RESISTANCE, 1200, 0, false, false, true), 0.8f)));
        register("iron_smoked_blue_apricorn", () -> new FoodItem(Rarity.RARE, 4, 1.0f, true, true, new FoodComponent.StatusEffectEntry(new StatusEffectInstance(StatusEffects.WATER_BREATHING, 1200, 0, false, false, true), 0.8f)));
        register("iron_smoked_green_apricorn", () -> new FoodItem(Rarity.RARE, 4, 1.0f, true, true, new FoodComponent.StatusEffectEntry(new StatusEffectInstance(StatusEffects.JUMP_BOOST, 1200, 0, false, false, true), 0.8f)));
        register("iron_smoked_pink_apricorn", () -> new FoodItem(Rarity.RARE, 4, 1.0f, true, true, new FoodComponent.StatusEffectEntry(new StatusEffectInstance(StatusEffects.REGENERATION, 1200, 0, false, false, true), 0.8f)));
        register("iron_smoked_red_apricorn", () -> new FoodItem(Rarity.RARE, 4, 1.0f, true, true, new FoodComponent.StatusEffectEntry(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 1200, 0, false, false, true), 0.8f)));
        register("iron_smoked_white_apricorn", () -> new FoodItem(Rarity.RARE, 4, 1.0f, true, true, new FoodComponent.StatusEffectEntry(new StatusEffectInstance(StatusEffects.SPEED, 1200, 0, false, false, true), 0.8f)));
        register("iron_smoked_yellow_apricorn", () -> new FoodItem(Rarity.RARE, 4, 1.0f, true, true, new FoodComponent.StatusEffectEntry(new StatusEffectInstance(StatusEffects.HASTE, 1200, 0, false, false, true), 0.8f)));
        register("golden_smoked_black_apricorn", () -> new FoodItem(Rarity.UNCOMMON, 4, 1.0f, true, true, new FoodComponent.StatusEffectEntry(new StatusEffectInstance(StatusEffects.RESISTANCE, 2400, 1, false, false, true), 0.8f)));
        register("golden_smoked_blue_apricorn", () -> new FoodItem(Rarity.UNCOMMON, 4, 1.0f, true, true, new FoodComponent.StatusEffectEntry(new StatusEffectInstance(StatusEffects.CONDUIT_POWER, 2400, 0, false, false, true), 0.8f)));
        register("golden_smoked_green_apricorn", () -> new FoodItem(Rarity.UNCOMMON, 4, 1.0f, true, true, new FoodComponent.StatusEffectEntry(new StatusEffectInstance(StatusEffects.JUMP_BOOST, 2400, 1, false, false, true), 0.8f)));
        register("golden_smoked_pink_apricorn", () -> new FoodItem(Rarity.UNCOMMON, 4, 1.0f, true, true, new FoodComponent.StatusEffectEntry(new StatusEffectInstance(StatusEffects.REGENERATION, 2400, 1, false, false, true), 0.8f)));
        register("golden_smoked_red_apricorn", () -> new FoodItem(Rarity.UNCOMMON, 4, 1.0f, true, true, new FoodComponent.StatusEffectEntry(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 2400, 1, false, false, true), 0.8f)));
        register("golden_smoked_white_apricorn", () -> new FoodItem(Rarity.UNCOMMON, 4, 1.0f, true, true, new FoodComponent.StatusEffectEntry(new StatusEffectInstance(StatusEffects.SPEED, 2400, 1, false, false, true), 0.8f)));
        register("golden_smoked_yellow_apricorn", () -> new FoodItem(Rarity.UNCOMMON, 4, 1.0f, true, true, new FoodComponent.StatusEffectEntry(new StatusEffectInstance(StatusEffects.HASTE, 2400, 1, false, false, true), 0.8f)));
        register("lootbag_mythsandlegends", () -> new LootBagItem(0xBC7BFF, new Item.Settings().maxCount(1)));
        register("lootbag_megastone", () -> new LootBagItem(0x80272A, new Item.Settings().maxCount(1)));
        register("lootbag_shinyplushie", () -> new LootBagItem(0xAAFFA0, new Item.Settings().maxCount(1)));
        register("lootbag_plushie", () -> new LootBagItem(0xFFAEFF, new Item.Settings().maxCount(1)));
        register("lootbag_journeysendbiomeblend", () -> new LootBagItem(0x4C694C, new Item.Settings().maxCount(1)));
        register("lootbag_hats", () -> new LootBagItem(0x4F3539, new Item.Settings().maxCount(1)));
    }

    public static <V extends Item> RegistrySupplier<V> register(Identifier id, Supplier<V> item) {
        return register(ITEMS, id, item);
    }

    public static <V extends Item> RegistrySupplier<V> register(String name, Supplier<V> item) {
        return register(ITEMS, name, item);
    }

}

