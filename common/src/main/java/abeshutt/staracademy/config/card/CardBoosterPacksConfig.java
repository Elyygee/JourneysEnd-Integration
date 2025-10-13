package abeshutt.staracademy.config.card;

import abeshutt.staracademy.StarAcademyMod;
import abeshutt.staracademy.card.BoosterPackEntry;
import abeshutt.staracademy.card.BoosterPackEntry.Pool;
import abeshutt.staracademy.config.ServerOnlyFileConfig;
import abeshutt.staracademy.world.roll.NumberRoll;
import com.google.gson.annotations.Expose;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class CardBoosterPacksConfig extends ServerOnlyFileConfig {

    @Expose private Map<String, BoosterPackEntry> values;

    @Override
    public String getPath() {
        return "card.booster_packs";
    }

    public Map<String, BoosterPackEntry> getValues() {
        return this.values;
    }

    public Optional<BoosterPackEntry> get(String id) {
        if (this.values == null) {
            this.reset(); // Initialize with defaults if null
        }
        return Optional.ofNullable(this.values.get(id));
    }

    @Override
    protected void reset() {
        this.values = new LinkedHashMap<>();
        this.values.put("base", new BoosterPackEntry(
                StarAcademyMod.id("booster_pack/base_base"),
                StarAcademyMod.id("booster_pack/base_ripped"),
                9031664, new Pool("@all", NumberRoll.uniformII(3, 5), 1.0F)));
    }

}
