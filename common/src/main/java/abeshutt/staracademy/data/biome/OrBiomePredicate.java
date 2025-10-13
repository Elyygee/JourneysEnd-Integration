package abeshutt.staracademy.data.biome;

import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.Biome;

import java.util.Arrays;

public class OrBiomePredicate implements BiomePredicate {

    private final BiomePredicate[] children;

    public OrBiomePredicate(BiomePredicate... children) {
        this.children = children;
    }

    public BiomePredicate[] getChildren() {
        return this.children;
    }

    @Override
    public boolean test(RegistryEntry<Biome> biome) {
        for(BiomePredicate child : this.children) {
            if(child.test(biome)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public String toString() {
        return Arrays.toString(this.children);
    }

}
