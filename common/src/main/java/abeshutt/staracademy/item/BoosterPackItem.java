package abeshutt.staracademy.item;

import abeshutt.staracademy.StarAcademyMod;
import abeshutt.staracademy.card.BoosterPackEntry;
import abeshutt.staracademy.card.CardData;
import abeshutt.staracademy.init.ModConfigs;
import abeshutt.staracademy.init.ModDataComponents;
import abeshutt.staracademy.init.ModItems;
import abeshutt.staracademy.item.renderer.BoosterPackItemRenderer;
import abeshutt.staracademy.item.renderer.SpecialItemRenderer;
import abeshutt.staracademy.screen.BoosterPackScreen;
import abeshutt.staracademy.util.ISpecialItemModel;
import abeshutt.staracademy.world.random.JavaRandom;
import dev.architectury.platform.Platform;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class BoosterPackItem extends Item implements ISpecialItemModel {

    public BoosterPackItem() {
        super(new Settings().fireproof().maxCount(1));
    }

    public static Optional<BoosterPackEntry> get(ItemStack stack, boolean client) {
        String boosterId = stack.getOrDefault(ModDataComponents.BOOSTER_PACK.get(), null);
        System.out.println("[DEBUG] BoosterPackItem.get: Booster ID from stack: " + boosterId);
        
        if(boosterId == null) {
            System.out.println("[DEBUG] BoosterPackItem.get: No booster ID found in stack");
            return Optional.empty();
        }
        
        Optional<BoosterPackEntry> entry = ModConfigs.CARD_BOOSTERS.get(boosterId);
        System.out.println("[DEBUG] BoosterPackItem.get: Found booster entry: " + (entry.isPresent() ? "YES" : "NO"));
        return entry;
    }

    public static void set(ItemStack stack, String id) {
        stack.set(ModDataComponents.BOOSTER_PACK.get(), id);
    }

    public static ItemStack create(String id) {
        ItemStack stack = new ItemStack(ModItems.BOOSTER_PACK.get());
        stack.set(ModDataComponents.BOOSTER_PACK.get(), id);
        return stack;
    }

    @Override
    public Text getName(ItemStack stack) {
        Text text = super.getName(stack);

        Integer color = BoosterPackItem.get(stack, Platform.getEnv() == EnvType.CLIENT)
                .map(BoosterPackEntry::getColor)
                .orElse(null);

        if(color != null) {
            text = text.copy().setStyle(Style.EMPTY.withColor(color));
        }

        return text;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        user.setCurrentHand(hand);

        System.out.println("[DEBUG] BoosterPackItem.use: world.isClient() = " + world.isClient());
        System.out.println("[DEBUG] BoosterPackItem.use: stack.contains(CONTAINER) = " + stack.contains(DataComponentTypes.CONTAINER));

        if(!world.isClient() && !stack.contains(DataComponentTypes.CONTAINER)) {
            System.out.println("[DEBUG] BoosterPackItem.use: Generating cards on server side");
            BoosterPackItem.get(stack, false).ifPresent(entry -> {
                System.out.println("[DEBUG] BoosterPackItem.use: Found booster pack entry: " + entry);
                List<ItemStack> items = new ArrayList<>();

                List<CardData> cardDataList = entry.generate(JavaRandom.ofNanoTime());
                System.out.println("[DEBUG] BoosterPackItem.use: Generated " + cardDataList.size() + " card data entries");

                for(CardData data : cardDataList) {
                    ItemStack cardItem = CardItem.of(data);
                    System.out.println("[DEBUG] BoosterPackItem.use: Created card item: " + cardItem);
                    items.add(cardItem);
                }

                System.out.println("[DEBUG] BoosterPackItem.use: Total items to add to container: " + items.size());
                stack.set(DataComponentTypes.CONTAINER, ContainerComponent.fromStacks(new ArrayList<>(items)));
            });
            if (!BoosterPackItem.get(stack, false).isPresent()) {
                System.out.println("[DEBUG] BoosterPackItem.use: No booster pack entry found for stack");
            }
        }

        if(world.isClient()) {
            this.openScreen();
        }

        return TypedActionResult.consume(stack);
    }

    @Environment(EnvType.CLIENT)
    public void openScreen() {
        MinecraftClient.getInstance().setScreen(new BoosterPackScreen());
    }

    @Override
    public String getTranslationKey(ItemStack stack) {
        String id = stack.getOrDefault(ModDataComponents.BOOSTER_PACK.get(), null);
        return super.getTranslationKey(stack) + (id == null ? "" : "." + id);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void loadModels(Stream<Identifier> unbakedModels, Consumer<ModelIdentifier> loader) {
        unbakedModels.forEach(id -> {
            if(id.getNamespace().equals(StarAcademyMod.ID) && id.getPath().startsWith("booster_pack")) {
                loader.accept(StarAcademyMod.mid(id, "inventory"));
            }
        });
    }

    @Override
    @Environment(EnvType.CLIENT)
    public SpecialItemRenderer getRenderer() {
        return BoosterPackItemRenderer.INSTANCE;
    }

}
