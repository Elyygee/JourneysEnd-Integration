package abeshutt.staracademy.mixin;

import abeshutt.staracademy.util.ISpecialItemModel;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Mixin(ModelLoader.class)
public abstract class MixinModelLoader {

    @Shadow @Final private Map<Identifier, JsonUnbakedModel> jsonUnbakedModels;

    @Shadow protected abstract void loadItemModel(ModelIdentifier id);

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", ordinal = 0, shift = At.Shift.AFTER))
    public void init(BlockColors blockColors, Profiler profiler, Map<Identifier, JsonUnbakedModel> jsonUnbakedModels,
                     Map<Identifier, List<ModelLoader.SpriteGetter>> blockStates, CallbackInfo ci) {
        for(Item item : Registries.ITEM) {
            if(item instanceof ISpecialItemModel loader) {
                Stream<Identifier> filteredUnbakedModels = this.jsonUnbakedModels.keySet().stream()
                        .filter(id -> {
                            String path = id.getPath();
                            return path.startsWith("models/item/") && path.endsWith(".json");
                        })
                        .map(id -> id.withPath(id.getPath().substring(
                                "models/item/".length(), id.getPath().length() - ".json".length())));

                loader.loadModels(filteredUnbakedModels, this::loadItemModel);
            }
        }

        // Ensure Cobblemon-style 2D models for our custom Safari Balls are loaded.
        // These models live under assets/cobblemon/models/item/* but there is no vanilla
        // item with ID cobblemon:great_safari_ball / golden_safari_ball, so they won't
        // be picked up automatically. We explicitly schedule them here so that
        // ModelIdentifier("cobblemon:great_safari_ball#inventory") and
        // ModelIdentifier("cobblemon:golden_safari_ball#inventory") resolve to
        // non-missing models in our SafariBall 2D renderer mixin.
        this.loadItemModel(new ModelIdentifier(new Identifier("cobblemon", "great_safari_ball"), "inventory"));
        this.loadItemModel(new ModelIdentifier(new Identifier("cobblemon", "golden_safari_ball"), "inventory"));
    }

}
