package abeshutt.staracademy.attribute.again;

import abeshutt.staracademy.attribute.Option;
import abeshutt.staracademy.math.Rational;
import abeshutt.staracademy.util.AttributeHolder;
import com.cobblemon.mod.common.api.spawning.SpawnBucket;
import com.cobblemon.mod.common.api.spawning.position.SpawnablePosition;
import com.cobblemon.mod.common.api.spawning.position.calculators.SpawnablePositionCalculator;
import com.cobblemon.mod.common.api.spawning.detail.SpawnAction;
import com.cobblemon.mod.common.api.spawning.detail.SpawnDetail;
import com.cobblemon.mod.common.api.spawning.influence.SpawningInfluence;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

import static abeshutt.staracademy.attribute.Attributes.ofBucketWeight;
import static abeshutt.staracademy.attribute.Attributes.ofLabelWeight;

public class AttributePlayerInfluence implements SpawningInfluence {

    private final ServerPlayerEntity player;

    public AttributePlayerInfluence(ServerPlayerEntity player) {
        this.player = player;
    }

    @Override
    public boolean isExpired() {
        return false;
    }

    @Override
    public boolean affectSpawnable(@NotNull SpawnDetail spawnDetail, @NotNull SpawnablePosition spawnablePosition) {
        return true;
    }

    @Override
    public float affectWeight(SpawnDetail detail, SpawnablePosition spawnablePosition, float weight) {
        for(String label : detail.getLabels()) {
            float value = weight;

            weight = AttributeHolder.getRoot(this.player).path(ofLabelWeight(label))
                .map(attribute -> {
                    Option<Rational> result = attribute.get(Option.present(Rational.of(value)), AttributeContext.random());
                    return result.isPresent() ? result.get().floatValue() : value;
                }).orElse(weight);
        }

        return weight;
    }

    @Override
    public void affectAction(SpawnAction<?> action) {

    }

    @Override
    public void affectSpawn(SpawnAction<?> action, Entity entity) {

    }

    @Override
    public void affectBucketWeights(java.util.Map<SpawnBucket, Float> bucketWeights) {
        for (SpawnBucket bucket : bucketWeights.keySet()) {
            float weight = bucketWeights.get(bucket);
            float newWeight = AttributeHolder.getRoot(this.player).path(ofBucketWeight(bucket.getName())).map(attribute -> {
                Option<Rational> result = attribute.get(Option.present(Rational.of(weight)), AttributeContext.random());
                return result.isPresent() ? result.get().floatValue() : weight;
            }).orElse(weight);
            bucketWeights.put(bucket, newWeight);
        }
    }

    // Note: The interface signature uses net.minecraft.server.level.ServerLevel and net.minecraft.core.BlockPos
    // but common module uses intermediary mappings (ServerWorld/BlockPos). 
    // Since we can't access the new package structure in common module, we'll use a bridge approach.
    // The fabric module will handle the proper remapping.
    public boolean isAllowedPosition(net.minecraft.server.world.ServerWorld world, net.minecraft.util.math.BlockPos pos, SpawnablePositionCalculator<?, ?> calculator) {
        return true;
    }

}
