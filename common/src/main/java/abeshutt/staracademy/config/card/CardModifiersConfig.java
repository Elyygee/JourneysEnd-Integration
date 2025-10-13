package abeshutt.staracademy.config.card;

import abeshutt.staracademy.attribute.Option;
import abeshutt.staracademy.attribute.again.*;
import abeshutt.staracademy.card.CardModifierEntry;
import abeshutt.staracademy.config.ServerOnlyFileConfig;
import abeshutt.staracademy.math.Rational;
import abeshutt.staracademy.world.random.RandomSource;
import abeshutt.staracademy.world.roll.NumberRoll;
import com.google.gson.annotations.Expose;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static abeshutt.staracademy.attribute.again.type.AttributeTypes.number;
import static abeshutt.staracademy.attribute.path.AttributePath.absolute;

public class CardModifiersConfig extends ServerOnlyFileConfig {

    @Expose private Map<String, CardModifierEntry> values;
    @Expose private Map<String, Map<String, Rational>> pools;

    @Override
    public String getPath() {
        return "card.modifiers";
    }

    public Optional<CardModifierEntry> get(String id) {
        if (this.values == null) {
            this.reset(); // Initialize with defaults if null
        }
        return Optional.ofNullable(this.values.get(id));
    }

    public Map<String, CardModifierEntry> getValues() {
        if (this.values == null) {
            this.reset(); // Initialize with defaults if null
        }
        return this.values;
    }

    public Option<String> flatten(String id, RandomSource random) {
        if (this.values == null) {
            this.reset(); // Initialize with defaults if null
        }
        if(id.startsWith("@")) {
            Map<String, Rational> group = this.pools.get(id.substring(1));

            if(group == null) {
                return Option.absent();
            }

            WeightedList<String> weighted = WeightedList.of(group);
            return weighted.getRandom(random).mapFlat(s -> this.flatten(s, random));
        }

        return this.values.containsKey(id) ? Option.present(id) : Option.absent();
    }

    @Override
    protected void reset() {
        this.values = new LinkedHashMap<>();
        this.pools = new LinkedHashMap<>();

        this.values.put("shiny_chance", new CardModifierEntry(
                absolute("shiny_chance", "increased"), 1,
                new AddAttribute<>(number(), NodeAttribute.of(number())
                            .add(null, 0, AssignAttribute.of(
                                    NumberRoll.uniformResolutionII(
                                            Rational.of(1, 100),
                                            Rational.of(5, 100),
                                            Rational.of(1, 100))))
                            .add(null, 0, CardScalarAttribute.of("base")))));

        Map<String, Rational> all = new LinkedHashMap<>();
        Map<String, Rational> special = new LinkedHashMap<>();
        all.put("shiny_chance", Rational.ONE);
        special.put("shiny_chance", Rational.ONE);
        this.pools.put("all", all);
        this.pools.put("special", special);
    }

}
