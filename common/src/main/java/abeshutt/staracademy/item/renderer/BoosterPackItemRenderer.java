package abeshutt.staracademy.item.renderer;

import abeshutt.staracademy.StarAcademyMod;
import abeshutt.staracademy.card.BoosterPackEntry;
import abeshutt.staracademy.card.BoosterPackVisual;
import abeshutt.staracademy.client.BoosterVisualsClientCache;
import abeshutt.staracademy.init.ModConfigs;
import abeshutt.staracademy.init.ModDataComponents;
import abeshutt.staracademy.item.BaseBoosterPackItem;
import abeshutt.staracademy.item.BoosterPackItem;
import abeshutt.staracademy.util.ClientScheduler;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class BoosterPackItemRenderer extends SpecialItemRenderer {

    public static final BoosterPackItemRenderer INSTANCE = new BoosterPackItemRenderer();

    @Override
    public void render(ItemStack stack, ModelTransformationMode mode, boolean leftHanded, MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers, int light, int overlay) {
        
        // For BaseBoosterPackItem, use the same logic as the original BoosterPackItem
        if (stack.getItem() instanceof BaseBoosterPackItem) {
            // Get the booster pack entry using the same method as the original
            String boosterId = stack.getOrDefault(ModDataComponents.BOOSTER_PACK.get(), "base");
            boolean hasContainer = stack.contains(DataComponentTypes.CONTAINER);
            
            // Try using the original BoosterPackItem.get() method
            BoosterPackItem.get(stack, true).ifPresentOrElse(entry -> {
                Identifier model = hasContainer ? entry.getModelRipped() : entry.getModelBase();
                ModelIdentifier modelId = StarAcademyMod.mid(model, "inventory");
                this.renderModel(modelId, stack, mode, leftHanded, matrices, vertexConsumers, light, overlay);
            }, () -> {
                ModConfigs.CARD_BOOSTERS.get(boosterId).ifPresentOrElse(entry -> {
                    // Use the model from BoosterPackEntry, switching based on CONTAINER presence
                    Identifier model = hasContainer ? entry.getModelRipped() : entry.getModelBase();
                    ModelIdentifier modelId = StarAcademyMod.mid(model, "inventory");
                    this.renderModel(modelId, stack, mode, leftHanded, matrices, vertexConsumers, light, overlay);
                }, () -> {
                    // Fallback to default model
                    this.renderModel(StarAcademyMod.mid("booster_pack/base_base", "inventory"), stack, mode, leftHanded, matrices, vertexConsumers, light, overlay);
                });
            });
        } else {
            // Original logic for BoosterPackItem
            BoosterPackItem.get(stack, true).ifPresentOrElse(entry -> {
                Identifier model = stack.contains(DataComponentTypes.CONTAINER) ? entry.getModelRipped() : entry.getModelBase();
                this.renderModel(StarAcademyMod.mid(model, "inventory"), stack, mode, leftHanded, matrices, vertexConsumers, light, overlay);
            }, () -> {
                // Use client cache for fallback rendering
                if (BoosterVisualsClientCache.ready()) {
                    // Get a random booster pack from the cache for fallback
                    var visuals = BoosterVisualsClientCache.getAll();
                    if (!visuals.isEmpty()) {
                        var keys = visuals.keySet().toArray();
                        String randomKey = (String) keys[(int)(ClientScheduler.getTick() >> 5) % keys.length];
                        BoosterPackVisual visual = BoosterVisualsClientCache.get(randomKey);
                        
                        // Use the visual data to render
                        Identifier model = StarAcademyMod.id("booster_" + randomKey);
                        this.renderModel(StarAcademyMod.mid(model, "inventory"), stack, mode, leftHanded, matrices, vertexConsumers, light, overlay);
                    }
                } else {
                    // Fallback to default if cache not ready
                    this.renderModel(StarAcademyMod.mid("booster_default", "inventory"), stack, mode, leftHanded, matrices, vertexConsumers, light, overlay);
                }
            });
        }
    }

}
