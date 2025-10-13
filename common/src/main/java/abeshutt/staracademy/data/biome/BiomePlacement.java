package abeshutt.staracademy.data.biome;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public interface BiomePlacement<T> extends BiomePredicate {

    boolean isSubsetOf(T other);

    boolean isSubsetOf(BlockView world, BlockPos pos);

    void fillInto(T other);

    T copy();

}
