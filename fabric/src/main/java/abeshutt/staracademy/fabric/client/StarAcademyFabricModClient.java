package abeshutt.staracademy.fabric.client;

import abeshutt.staracademy.init.ModItems;
import abeshutt.staracademy.init.ModRenderers;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;

public class StarAcademyFabricModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModRenderers.Blocks.register(BlockRenderLayerMap.INSTANCE::putBlock);
        
        // Register custom renderer for Safari Balls to force 2D in GUI
        var renderer = new PokeBallGui2DRenderer();
        BuiltinItemRendererRegistry.INSTANCE.register(
            ModItems.GREAT_SAFARI_BALL.get(),
            (stack, mode, matrices, vertexConsumers, light, overlay) -> 
                renderer.render(stack, mode, matrices, vertexConsumers, light, overlay)
        );
        BuiltinItemRendererRegistry.INSTANCE.register(
            ModItems.GOLDEN_SAFARI_BALL.get(),
            (stack, mode, matrices, vertexConsumers, light, overlay) -> 
                renderer.render(stack, mode, matrices, vertexConsumers, light, overlay)
        );
    }
}
