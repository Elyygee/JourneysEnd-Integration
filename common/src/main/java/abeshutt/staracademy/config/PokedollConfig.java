package abeshutt.staracademy.config;

import com.google.gson.annotations.Expose;
import net.minecraft.util.Identifier;

import java.util.HashSet;
import java.util.Set;

public class PokedollConfig extends FileConfig {


    @Expose
    private Set<Identifier> blocksToDetect = new HashSet<>();

    @Expose
    private int radius = 16;

    @Expose
    private int numberOfPokedolls = 5;

    @Expose
    private int checkInterval = 20 * 3; // In ticks.

    @Expose
    private float maxShinyBoostMultiplier = 2.0f; // Maximum shiny boost multiplier at 100% progress (e.g., 2.0 = 2x shiny rate)

    @Expose
    private int shinyBoostRadius = 64; // Radius to check for collectors when calculating shiny boost


    @Override
    public String getPath() {
        return "pokedoll";
    }

    @Override
    protected void reset() {
        blocksToDetect.clear();
        radius = 16;
        numberOfPokedolls = 5;
        checkInterval = 20 * 3;
        maxShinyBoostMultiplier = 2.0f;
        shinyBoostRadius = 64;
    }

    public Set<Identifier> getBlocksToDetect() {
        return blocksToDetect;
    }

    public int getRadius() {
        return radius;
    }

    public int getNumberOfPokedolls() {
        return numberOfPokedolls;
    }

    public int getCheckInterval() {
        return checkInterval;
    }

    public float getMaxShinyBoostMultiplier() {
        return maxShinyBoostMultiplier;
    }

    public int getShinyBoostRadius() {
        return shinyBoostRadius;
    }
}
