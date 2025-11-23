package abeshutt.staracademy.fabric.client;

import abeshutt.staracademy.init.ModRenderers;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;

public class StarAcademyFabricModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModRenderers.Blocks.register(BlockRenderLayerMap.INSTANCE::putBlock);
        
        // Note: Custom Safari Ball inventory icons are handled automatically by Cobblemon's ItemRendererMixin
        // which replaces the 3D model with the 2D icon for GUI/FIXED display contexts.
        // No custom renderer registration needed - the mixin detects PokeBallItem instances and uses getModel2d().
    }
}
