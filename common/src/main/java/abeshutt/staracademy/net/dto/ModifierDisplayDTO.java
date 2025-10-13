package abeshutt.staracademy.net.dto;

public record ModifierDisplayDTO(
    String id,        // e.g. "shiny_chance"
    String nameKey,   // CardDisplayEntry#getName()
    int colorRgb,     // CardDisplayEntry#getColor()
    String styleId,   // "plain" | "percent" | "xmult" | etc.
    double min,       // formatting hint (optional)
    double max,
    double resolution // formatting hint (optional)
) {
    public static final ModifierDisplayDTO UNKNOWN =
        new ModifierDisplayDTO("unknown","text.academy.card.modifier.unknown",0x00AA00,"plain",0,0,0.001);
}
