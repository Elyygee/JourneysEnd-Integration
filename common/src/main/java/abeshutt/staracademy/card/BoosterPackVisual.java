package abeshutt.staracademy.card;

/**
 * Visual data for booster packs that can be safely sent to clients
 */
public record BoosterPackVisual(String iconId, int tintRgb, boolean foil) {
    public static final BoosterPackVisual DEFAULT = new BoosterPackVisual("journeysend:icons/booster_default", 0xFFFFFF, false);
}
