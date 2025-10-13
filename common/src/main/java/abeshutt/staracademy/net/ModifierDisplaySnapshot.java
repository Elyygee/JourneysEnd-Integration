package abeshutt.staracademy.net;

import abeshutt.staracademy.card.*;
import abeshutt.staracademy.init.ModConfigs;
import abeshutt.staracademy.net.dto.ModifierDisplayDTO;
import abeshutt.staracademy.world.roll.*;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

final class ModifierDisplaySnapshot {
    private ModifierDisplaySnapshot() {}

    static Map<String, ModifierDisplayDTO> build() {
        var out = new HashMap<String, ModifierDisplayDTO>();
        var modifiers = ModConfigs.CARD_MODIFIERS;           // server-only
        var displays  = ModConfigs.CARD_DISPLAYS;            // server-only
        var root      = ModConfigs.ATTRIBUTE.getRoot();      // server-only

        if (modifiers != null) {
            modifiers.getValues().forEach((id, mod) -> {
                root.path(mod.getPath()).ifPresent(targetPath -> {
                    if (displays != null) {
                        displays.get(targetPath).ifPresent(display -> {
                            String nameKey = display.getName();
                            int color = display.getColor();
                            String styleId = display.getStyle() != null ? display.getStyle().id() : "decimal";

                            // Try to infer min/max/resolution from the Attribute tree
                            var stats = inferNumberHints(mod.getAttribute()); // (min,max,res)
                            out.put(id, new ModifierDisplayDTO(
                                id, nameKey, color, styleId, stats.min, stats.max, stats.resolution
                            ));
                        });
                    }
                });
            });
        }
        return Map.copyOf(out);
    }

    // --- helpers ---

    private record Hints(double min, double max, double resolution) {}
    private static Hints inferNumberHints(abeshutt.staracademy.attribute.again.Attribute<?> attr) {
        // Walk Add/Assign/Node to find first numeric roll (Uniform / Constant / Trapezoid)
        var roll = AttributeUtils.findFirstNumberRoll(attr);
        if (roll == null) return new Hints(0, 0, 0.001);
        if (roll instanceof UniformNumberRoll u) {
            // For uniform rolls, we need to get the constant values from the NumberRoll fields
            double min = getConstantValue(u.minimum);
            double max = getConstantValue(u.maximum);
            double res = getConstantValue(u.resolution);
            return new Hints(min, max, res);
        }
        if (roll instanceof ConstantNumberRoll c) {
            var v = c.value.doubleValue();
            return new Hints(v, v, 0.001);
        }
        if (roll instanceof TrapezoidalNumberRoll t) {
            double min = getConstantValue(t.minimum);
            double max = getConstantValue(t.maximum);
            return new Hints(min, max, 0.001);
        }
        return new Hints(0, 0, 0.001);
    }

    private static double getConstantValue(NumberRoll roll) {
        if (roll instanceof ConstantNumberRoll c) {
            return c.value.doubleValue();
        }
        // For non-constant rolls, we can't easily extract a single value
        // Return 0 as fallback
        return 0.0;
    }
}
