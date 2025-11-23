package abeshutt.staracademy.block.entity.renderer;

import abeshutt.staracademy.block.entity.ShinyPokedollCollectorBlockEntity;
import abeshutt.staracademy.client.RenderUtil;
import abeshutt.staracademy.init.ModConfigs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;

import java.text.DecimalFormat;

public class ShinyPokedollCollectorBlockEntityRenderer implements BlockEntityRenderer<ShinyPokedollCollectorBlockEntity> {

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.#");
    private static final DecimalFormat MULTIPLIER_FORMAT = new DecimalFormat("#.##");

    public ShinyPokedollCollectorBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
    }

    @Override
    public void render(ShinyPokedollCollectorBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        RenderUtil.renderHologram(matrices, vertexConsumers, LightmapTextureManager.MAX_LIGHT_COORDINATE, 82.0F / 255.0F, 57.0F / 255F, 141F / 255F);
        
        // Get config values
        float maxMultiplier = ModConfigs.POKEDOLLS != null ? ModConfigs.POKEDOLLS.getMaxShinyBoostMultiplier() : 2.0f;
        int radius = ModConfigs.POKEDOLLS != null ? ModConfigs.POKEDOLLS.getShinyBoostRadius() : 64;
        
        // Calculate current multiplier based on progress
        float progress = entity.getProgress();
        float currentMultiplier = 1.0f + (maxMultiplier - 1.0f) * progress;
        
        // Render multiple lines of text
        matrices.push();
        matrices.translate(0.5, 2.6, 0.5);
        matrices.scale(0.025F, -0.025F, 0.025F);
        MinecraftClient client = MinecraftClient.getInstance();
        float cameraYaw = client.getEntityRenderDispatcher().camera.getYaw();
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-cameraYaw));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180));

        Matrix4f matrix4f = matrices.peek().getPositionMatrix();
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        
        // Line 1: Collection percentage
        Text collectionText = Text.literal(DECIMAL_FORMAT.format(progress * 100F) + "%");
        float collectionWidth = (float) (-textRenderer.getWidth(collectionText) / 2);
        textRenderer.draw(collectionText, collectionWidth, 0, ColorHelper.Argb.getArgb(255, 255, 255, 255), false, matrix4f, vertexConsumers, TextRenderer.TextLayerType.NORMAL, ColorHelper.Argb.getArgb(0, 0, 0, 0), LightmapTextureManager.MAX_LIGHT_COORDINATE);
        
        // Line 2: Shiny boost multiplier
        Text multiplierText = Text.literal("Shiny: " + MULTIPLIER_FORMAT.format(currentMultiplier) + "x");
        float multiplierWidth = (float) (-textRenderer.getWidth(multiplierText) / 2);
        textRenderer.draw(multiplierText, multiplierWidth, textRenderer.fontHeight + 1, ColorHelper.Argb.getArgb(255, 255, 215, 0), false, matrix4f, vertexConsumers, TextRenderer.TextLayerType.NORMAL, ColorHelper.Argb.getArgb(0, 0, 0, 0), LightmapTextureManager.MAX_LIGHT_COORDINATE);
        
        // Line 3: Radius
        Text radiusText = Text.literal("Radius: " + radius + " blocks");
        float radiusWidth = (float) (-textRenderer.getWidth(radiusText) / 2);
        textRenderer.draw(radiusText, radiusWidth, (textRenderer.fontHeight + 1) * 2, ColorHelper.Argb.getArgb(200, 200, 200, 200), false, matrix4f, vertexConsumers, TextRenderer.TextLayerType.NORMAL, ColorHelper.Argb.getArgb(0, 0, 0, 0), LightmapTextureManager.MAX_LIGHT_COORDINATE);
        
        matrices.pop();
    }
}
