package abeshutt.staracademy.mixin.cobblemon;

import com.cobblemon.mod.common.item.PokeBallItem;
import com.cobblemon.mod.common.pokeball.PokeBall;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Ensures our custom Safari Balls (great / golden) use their 2D icon model in GUI,
 * matching Cobblemon's behaviour for its own Poké Balls.
 */
@Mixin(ItemRenderer.class)
public abstract class MixinSafariBallItemRenderer {

    @Unique
    private boolean journeysend$inSafariBall2DRender = false;

    @Inject(
        method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/render/model/BakedModel;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void journeysend$use2dSafariBallModel(ItemStack stack,
                                                  ModelTransformationMode mode,
                                                  boolean leftHanded,
                                                  MatrixStack matrices,
                                                  VertexConsumerProvider vertexConsumers,
                                                  int light,
                                                  int overlay,
                                                  BakedModel originalModel,
                                                  CallbackInfo ci) {
        // Prevent infinite recursion when we call back into renderItem ourselves
        if (journeysend$inSafariBall2DRender) {
            return;
        }
        // Only care about GUI / fixed (inventory, item frames, etc.)
        if (mode != ModelTransformationMode.GUI && mode != ModelTransformationMode.FIXED) {
            return;
        }

        if (!(stack.getItem() instanceof PokeBallItem pokeBallItem)) {
            return;
        }

        PokeBall pokeBall = pokeBallItem.getPokeBall();
        Identifier name = pokeBall.getName();

        // Restrict to our custom Safari Balls so we don't interfere with base Cobblemon items.
        if (!"journeysend".equals(name.getNamespace())) {
            return;
        }

        String path = name.getPath();
        if (!"great_safari_ball".equals(path) && !"golden_safari_ball".equals(path)) {
            return;
        }

        // Use the configured 2D model identifier from the PokeBall itself.
        Identifier model2d = pokeBall.getModel2d();
        ModelIdentifier modelId = new ModelIdentifier(model2d, "inventory");

        BakedModel replacementModel = MinecraftClient.getInstance()
            .getBakedModelManager()
            .getModel(modelId);

        // Render using the 2D model and stop further processing, but guard against re-entry.
        journeysend$inSafariBall2DRender = true;
        try {
            ((ItemRenderer) (Object) this).renderItem(
                stack, mode, leftHanded, matrices, vertexConsumers, light, overlay, replacementModel
            );
        } finally {
            journeysend$inSafariBall2DRender = false;
        }
        ci.cancel();
    }
}

