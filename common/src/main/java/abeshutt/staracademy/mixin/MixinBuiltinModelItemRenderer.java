package abeshutt.staracademy.mixin;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public class MixinBuiltinModelItemRenderer {

    @Inject(method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/render/model/BakedModel;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/BuiltinModelItemRenderer;render(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;II)V"))
    public void render(ItemStack stack, ModelTransformationMode mode, boolean leftHanded, MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers, int light, int overlay, BakedModel model, CallbackInfo ci) {
        // Check if the item implements ISpecialItemModel using reflection
        try {
            Class<?> itemClass = stack.getItem().getClass();
            Class<?> specialItemModelClass = Class.forName("abeshutt.staracademy.util.ISpecialItemModel");
            
            if (specialItemModelClass.isAssignableFrom(itemClass)) {
                // Get the custom renderer and render the item
                Object renderer = itemClass.getMethod("getRenderer").invoke(stack.getItem());
                if (renderer != null) {
                    renderer.getClass().getMethod("render", ItemStack.class, ModelTransformationMode.class, boolean.class, 
                        MatrixStack.class, VertexConsumerProvider.class, int.class, int.class)
                        .invoke(renderer, stack, mode, leftHanded, matrices, vertexConsumers, light, overlay);
                }
            }
        } catch (Exception e) {
            // Silently ignore if reflection fails
        }
    }

}