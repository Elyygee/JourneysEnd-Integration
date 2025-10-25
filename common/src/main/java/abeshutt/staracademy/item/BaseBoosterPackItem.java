package abeshutt.staracademy.item;

import abeshutt.staracademy.StarAcademyMod;
import abeshutt.staracademy.card.BoosterPackEntry;
import abeshutt.staracademy.card.CardData;
import abeshutt.staracademy.init.ModConfigs;
import abeshutt.staracademy.item.renderer.BoosterPackItemRenderer;
import abeshutt.staracademy.item.renderer.SpecialItemRenderer;
import abeshutt.staracademy.util.ISpecialItemModel;
import abeshutt.staracademy.world.random.JavaRandom;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class BaseBoosterPackItem extends Item implements ISpecialItemModel {

    private static final Logger LOGGER = LoggerFactory.getLogger("JourneysEnd-BaseBoosterPack");

    public BaseBoosterPackItem(Settings settings) {
        super(settings);
    }

    @Override
    public Text getName(ItemStack stack) {
        Text text = super.getName(stack);
        
        // Get the color from the "base" booster pack entry
        if (ModConfigs.CARD_BOOSTERS != null) {
            Integer color = ModConfigs.CARD_BOOSTERS.get("base")
                    .map(BoosterPackEntry::getColor)
                    .orElse(null);
            
            if (color != null) {
                text = text.copy().setStyle(Style.EMPTY.withColor(color));
            }
        }
        
        return text;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        user.setCurrentHand(hand);


        // Server-side: Generate cards if not already generated
        if (!world.isClient() && !stack.contains(DataComponentTypes.CONTAINER)) {
            
            // Set the booster pack ID to "base" so BoosterPackItem.get() can find it
            stack.set(abeshutt.staracademy.init.ModDataComponents.BOOSTER_PACK.get(), "base");

            java.util.Optional<BoosterPackEntry> entryOpt = BoosterPackItem.get(stack, false);
            if (entryOpt.isPresent()) {
                BoosterPackEntry entry = entryOpt.get();
                List<ItemStack> items = new ArrayList<>();

                List<CardData> cardDataList = entry.generate(JavaRandom.ofNanoTime());

                for(CardData data : cardDataList) {
                    ItemStack cardItem = CardItem.of(data);
                    items.add(cardItem);
                }

                stack.set(DataComponentTypes.CONTAINER,
                    net.minecraft.component.type.ContainerComponent.fromStacks(new ArrayList<>(items)));
            }
        }

        // Client-side: Always open GUI
        if (world.isClient()) {
            this.openScreen();
        }

        // Return success to keep the item and sync CONTAINER to client
        return TypedActionResult.success(stack, false);
    }

    @Environment(EnvType.CLIENT)
    public void openScreen() {
        // Import Screen and BoosterPackScreen here to avoid loading client classes on server
        try {
            net.minecraft.client.MinecraftClient client = net.minecraft.client.MinecraftClient.getInstance();
            abeshutt.staracademy.screen.BoosterPackScreen screen = new abeshutt.staracademy.screen.BoosterPackScreen();
            client.setScreen(screen);
        } catch (Exception e) {
            LOGGER.error("[BaseBoosterPack] Failed to open screen", e);
        }
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void loadModels(Stream<Identifier> unbakedModels, Consumer<ModelIdentifier> loader) {
        // Load the booster pack models
        loader.accept(StarAcademyMod.mid("booster_pack/base_base", "inventory"));
        loader.accept(StarAcademyMod.mid("booster_pack/base_ripped", "inventory"));
        
        // Also load any other models that match
        unbakedModels.forEach(id -> {
            if (id.getNamespace().equals(StarAcademyMod.ID) && 
                (id.getPath().startsWith("base_booster_pack") || id.getPath().startsWith("booster_pack/base_"))) {
                loader.accept(StarAcademyMod.mid(id, "inventory"));
            }
        });
    }

    @Override
    public SpecialItemRenderer getRenderer() {
        return BoosterPackItemRenderer.INSTANCE;
    }
}
