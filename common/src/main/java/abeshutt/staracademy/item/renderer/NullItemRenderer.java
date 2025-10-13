package abeshutt.staracademy.item.renderer;

import abeshutt.staracademy.StarAcademyMod;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;

public class NullItemRenderer extends SpecialItemRenderer {

    public static final NullItemRenderer INSTANCE = new NullItemRenderer();

    @Override
    public void render(ItemStack stack, ModelTransformationMode mode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        this.renderModel(StarAcademyMod.mid("error", "inventory"), stack, mode, leftHanded, matrices, vertexConsumers, light, overlay);
    }

}
