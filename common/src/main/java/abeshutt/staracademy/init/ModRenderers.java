package abeshutt.staracademy.init;

import abeshutt.staracademy.block.entity.BetterStructureBlockEntity;
import abeshutt.staracademy.block.entity.HousePokedexBlockEntity;
import abeshutt.staracademy.block.entity.ShinyPokedollCollectorBlockEntity;
import abeshutt.staracademy.block.entity.renderer.BetterStructureBlockEntityRenderer;
import abeshutt.staracademy.block.entity.renderer.HousePokedexBlockEntityRenderer;
import abeshutt.staracademy.block.entity.renderer.ShinyPokedollCollectorBlockEntityRenderer;
import abeshutt.staracademy.entity.renderer.HumanEntityRenderer;
import abeshutt.staracademy.entity.renderer.ShootingStarRenderer;
import abeshutt.staracademy.mixin.ProxyModelPredicateProviderRegistry;
import dev.architectury.event.events.client.ClientLifecycleEvent;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.entity.EntityRenderers;
import net.minecraft.util.Identifier;

import java.util.Map;
import java.util.function.BiConsumer;

public class ModRenderers extends ModRegistries {

    public static class ItemModels extends ModRenderers {
        public static void register() {

            ProxyModelPredicateProviderRegistry.register(ModItems.ACCEPTANCE_LETTER.get(), Identifier.of("open"), (stack, world, entity, seed) -> {
                return stack.get(ModDataComponents.ACCEPTANCE_LETTER_OPEN.get()) ? 1.0F : 0.0F;
            });
        }
    }

    public static class Entities extends ModRenderers {
        public static void register() {
            ClientLifecycleEvent.CLIENT_SETUP.register(minecraft -> {
                EntityRenderers.register(ModEntities.PARTNER_NPC.get(), ctx -> new HumanEntityRenderer<>(ctx, false, e -> e));
                EntityRenderers.register(ModEntities.SAFARI_NPC.get(), ctx -> new HumanEntityRenderer<>(ctx, true, e -> e));
                EntityRenderers.register(ModEntities.NURSE_NPC.get(), ctx -> new HumanEntityRenderer<>(ctx, true, e -> e));
                EntityRenderers.register(ModEntities.CARD_GRADER_NPC.get(), ctx -> new HumanEntityRenderer<>(ctx, true, e -> e));
                EntityRenderers.register(ModEntities.SHOOTING_STAR.get(), ShootingStarRenderer::new);
            });
        }
    }

    public static class BlockEntities extends ModRenderers {
        public static BlockEntityRendererFactory<BetterStructureBlockEntity> STRUCTURE_BLOCK;
        public static BlockEntityRendererFactory<HousePokedexBlockEntity> HOUSE_POKEDEX;
        public static BlockEntityRendererFactory<ShinyPokedollCollectorBlockEntity> SHINY_POKEDEX;

        public static void register(Map<BlockEntityType<?>, BlockEntityRendererFactory<?>> registry) {
            try {
                STRUCTURE_BLOCK = register(registry, ModBlocks.Entities.STRUCTURE_BLOCK.get(), BetterStructureBlockEntityRenderer::new);
                HOUSE_POKEDEX = register(registry, ModBlocks.Entities.HOUSE_POKEDEX.get(), HousePokedexBlockEntityRenderer::new);
                SHINY_POKEDEX = register(registry, ModBlocks.Entities.SHINY_POKEDOLL_COLLECTOR.get(), ShinyPokedollCollectorBlockEntityRenderer::new);
            } catch(Exception e) {
                ClientLifecycleEvent.CLIENT_SETUP.register(minecraft -> {
                    STRUCTURE_BLOCK = register(registry, ModBlocks.Entities.STRUCTURE_BLOCK.get(), BetterStructureBlockEntityRenderer::new);
                    HOUSE_POKEDEX = register(registry, ModBlocks.Entities.HOUSE_POKEDEX.get(), HousePokedexBlockEntityRenderer::new);
                });
            }
        }
    }

    public static class Blocks extends ModRenderers {
        public static void register(BiConsumer<Block, RenderLayer> consumer) {
            registerBlockModelRenderers(consumer);
        }

        public static void registerBlockModelRenderers(BiConsumer<Block, RenderLayer> consumer) {
            consumer.accept(ModBlocks.SHINY_POKEDOLL_COLLECTOR.get(), RenderLayer.getCutout());
        }
    }

    public static <T extends BlockEntity> BlockEntityRendererFactory<T> register(
            Map<BlockEntityType<?>, BlockEntityRendererFactory<?>> registry,
            BlockEntityType<? extends T> type, BlockEntityRendererFactory<T> renderer) {
        registry.put(type, renderer);
        return renderer;
    }

}
