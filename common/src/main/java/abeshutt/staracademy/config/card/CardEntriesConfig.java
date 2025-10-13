package abeshutt.staracademy.config.card;

import abeshutt.staracademy.CardRarity;
import abeshutt.staracademy.attribute.Option;
import abeshutt.staracademy.attribute.again.WeightedList;
import abeshutt.staracademy.card.CardEntry;
import abeshutt.staracademy.card.CardEntry.Entry;
import abeshutt.staracademy.config.ServerOnlyFileConfig;
import abeshutt.staracademy.math.Rational;
import abeshutt.staracademy.world.random.RandomSource;
import com.google.gson.annotations.Expose;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class CardEntriesConfig extends ServerOnlyFileConfig {

    @Expose private Map<String, CardEntry> values;
    @Expose private Map<String, Map<String, Rational>> pools;

    @Override
    public String getPath() {
        return "card.entries";
    }

    public Optional<CardEntry> get(String id) {
        if (this.values == null) {
            this.reset(); // Initialize with defaults if null
        }
        return Optional.ofNullable(this.values.get(id));
    }

    public Option<String> flatten(String id, RandomSource random) {
        if (this.values == null) {
            this.reset(); // Initialize with defaults if null
        }
        System.out.println("[DEBUG] CardEntriesConfig.flatten: Processing id: " + id);
        if(id.startsWith("@")) {
            String poolName = id.substring(1);
            System.out.println("[DEBUG] CardEntriesConfig.flatten: Looking for pool: " + poolName);
            Map<String, Rational> group = this.pools.get(poolName);

            if(group == null) {
                System.out.println("[DEBUG] CardEntriesConfig.flatten: Pool '" + poolName + "' not found. Available pools: " + this.pools.keySet());
                return Option.absent();
            }

            System.out.println("[DEBUG] CardEntriesConfig.flatten: Found pool '" + poolName + "' with " + group.size() + " entries: " + group.keySet());
            WeightedList<String> weighted = WeightedList.of(group);
            return weighted.getRandom(random).mapFlat(s -> this.flatten(s, random));
        }

        boolean hasValue = this.values.containsKey(id);
        System.out.println("[DEBUG] CardEntriesConfig.flatten: Direct lookup for '" + id + "': " + (hasValue ? "FOUND" : "NOT FOUND"));
        return hasValue ? Option.present(id) : Option.absent();
    }

    @Override
    protected void reset() {
        this.values = new LinkedHashMap<>();
        this.pools = new LinkedHashMap<>();

        this.values.put("bulbasaur", new CardEntry("001bulbasaur", "base",
                new Entry("@all", null, 1.0F),
                new Entry("@special", Set.of(CardRarity.LEGENDARY), 1.0F)));

        this.values.put("ivysaur", new CardEntry("002ivysaur", "base",
                new Entry("@all", null, 1.0F),
                new Entry("@special", Set.of(CardRarity.LEGENDARY), 1.0F)));

        this.values.put("venusaur", new CardEntry("003venusaur", "base",
                new Entry("@all", null, 1.0F),
                new Entry("@special", Set.of(CardRarity.LEGENDARY), 1.0F)));

        Map<String, Rational> pool = new LinkedHashMap<>();
        pool.put("bulbasaur", Rational.ONE);
        pool.put("ivysaur", Rational.ONE);
        pool.put("venusaur", Rational.ONE);
        this.pools.put("all", pool);
    }

}
