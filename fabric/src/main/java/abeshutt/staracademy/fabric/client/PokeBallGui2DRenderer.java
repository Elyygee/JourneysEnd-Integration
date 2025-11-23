package abeshutt.staracademy.fabric.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;

public class PokeBallGui2DRenderer {

    public void render(ItemStack stack, ModelTransformationMode mode, MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers, int light, int overlay) {

        var mc = MinecraftClient.getInstance();
        var itemRenderer = mc.getItemRenderer();

        // 1) Inventory/Hotbar/GUI: use your baked 2D model (the JSON you showed)
        if (mode == ModelTransformationMode.GUI) {
            var baked = itemRenderer.getModel(stack, null, null, 0);
            itemRenderer.renderItem(
                stack,
                ModelTransformationMode.GUI,
                false,
                matrices,
                vertexConsumers,
                light,
                overlay,
                baked
            );
            return;
        }

        // 2) Non-GUI (in-hand, ground, frame, head): let vanilla render the baked model
        var baked = itemRenderer.getModel(stack, null, null, 0);
        itemRenderer.renderItem(
            stack,
            mode,
            false,
            matrices,
            vertexConsumers,
            light,
            overlay,
            baked
        );
    }
}
