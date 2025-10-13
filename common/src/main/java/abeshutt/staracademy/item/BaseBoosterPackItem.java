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
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class BaseBoosterPackItem extends Item implements ISpecialItemModel {

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
        
        if (user instanceof net.minecraft.server.network.ServerPlayerEntity player) {
            // Convert this item to the old booster pack format with NBT and pre-generate cards
            ItemStack newStack = BoosterPackItem.create("base");
            newStack.setCount(stack.getCount());
            
            // Pre-generate the cards so it has all 8 NBT tags immediately
            if (!newStack.contains(net.minecraft.component.DataComponentTypes.CONTAINER)) {
                BoosterPackItem.get(newStack, false).ifPresent(entry -> {
                    List<ItemStack> items = new ArrayList<>();
                    
                    List<CardData> cardDataList = entry.generate(JavaRandom.ofNanoTime());
                    
                    for(CardData data : cardDataList) {
                        ItemStack cardItem = CardItem.of(data);
                        items.add(cardItem);
                    }
                    
                    newStack.set(net.minecraft.component.DataComponentTypes.CONTAINER, 
                        net.minecraft.component.type.ContainerComponent.fromStacks(new ArrayList<>(items)));
                });
            }
            
            return TypedActionResult.success(newStack, false);
        }
        
        return super.use(world, user, hand);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void loadModels(Stream<Identifier> unbakedModels, Consumer<ModelIdentifier> loader) {
        unbakedModels.forEach(id -> {
            if (id.getNamespace().equals(StarAcademyMod.ID) && id.getPath().startsWith("base_booster_pack")) {
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
