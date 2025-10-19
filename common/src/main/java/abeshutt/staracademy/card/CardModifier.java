package abeshutt.staracademy.card;

import abeshutt.staracademy.attribute.Option;
import abeshutt.staracademy.attribute.again.*;
import abeshutt.staracademy.client.ModifierDisplayClientCache;
import abeshutt.staracademy.data.adapter.Adapters;
import abeshutt.staracademy.data.serializable.ISerializable;
import abeshutt.staracademy.init.ModConfigs;
import abeshutt.staracademy.net.dto.ModifierDisplayDTO;
import abeshutt.staracademy.item.CardAlbumItem;
import abeshutt.staracademy.math.Rational;
import abeshutt.staracademy.world.roll.ConstantNumberRoll;
import abeshutt.staracademy.world.roll.NumberRoll;
import abeshutt.staracademy.world.roll.TrapezoidalNumberRoll;
import abeshutt.staracademy.world.roll.UniformNumberRoll;
import com.google.gson.JsonObject;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static abeshutt.staracademy.attribute.again.type.AttributeTypes.any;

public class CardModifier implements ISerializable<NbtCompound, JsonObject> {

    private String source;
    private Attribute<?> attribute;

    public CardModifier() {

    }

    public CardModifier(String source, Attribute<?> attribute) {
        this.source = source;
        this.attribute = attribute;
    }

    public Attribute<?> getAttribute() {
        return this.attribute;
    }

    public void attach(Attribute<?> root) {
        ModConfigs.CARD_MODIFIERS.get(this.source).ifPresent(mod -> {
            root.path(mod.getPath()).ifPresent(target -> {
                if(target instanceof NodeAttribute node) {
                    node.add(CardAlbumItem.REFERENCE, mod.getOrder(), this.attribute);
                }
            });
        });
    }

    public void setGrade(int grade) {
        this.attribute.iterate(child -> {
            if(child instanceof CardScalarAttribute<?> scalar) {
                scalar.setGrade(grade);
            }
        });
    }

    public void appendTooltip(ItemStack stack, int grade, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
        MutableText text = Text.empty();

        if(this.attribute instanceof AddAttribute<?> given) {
            this.setGrade(0);
            Object rawValue = given.getValue().get(Option.absent(),
                    new AttributeContext(null)).orElse(null);
            this.setGrade(grade);
            Object gradedValue = given.getValue().get(Option.absent(),
                    new AttributeContext(null)).orElse(null);

            // ---- synced client display metadata (no server configs) ----
            boolean usedSyncedConfig = false;

            if (gradedValue != null) {
                ModifierDisplayDTO dto = ModifierDisplayClientCache.get(this.source); // returns UNKNOWN if missing

                if (dto != null && !dto.equals(ModifierDisplayDTO.UNKNOWN)) {
                    text.append(Text.literal("+"));

                    if (Screen.hasShiftDown()) {
                        // Show current value and known range if min/max provided
                        String cur = formatValue(gradedValue, dto);
                        if (dto.min() == dto.max()) {
                            text.append(Text.literal(cur));
                        } else {
                            text.append(Text.literal(cur))
                                .append(Text.literal(" [" + formatDouble(dto.min(), dto) + "-" + formatDouble(dto.max(), dto) + "]"))
                                .formatted(Formatting.GRAY);
                        }
                    } else {
                        text.append(Text.literal(formatValue(gradedValue, dto)));
                    }

                    text.append(Text.literal(" "))
                        .append(Text.translatable(dto.nameKey()))
                        .setStyle(text.getStyle().withColor(dto.colorRgb()));

                    usedSyncedConfig = true;
                }
            }

            // ---- fallback if no synced display entry available ----
            if (!usedSyncedConfig && gradedValue != null) {
                text.append(Text.literal("+")).append(Text.literal(formatFallback(gradedValue)))
                    .append(Text.literal(" "))
                    .append(Text.translatable(this.inferDisplayName(this.source)))
                    .setStyle(text.getStyle().withColor(0x00AA00)); // green fallback
            }
        }

        tooltip.add(text);
    }
    
    private String inferDisplayName(String source) {
        // Try to infer a readable name from the source ID based on the config paths
        if (source.contains("block_interaction_range")) {
            return "text.journeysend.card.modifier.block_reach";
        } else if (source.contains("movement_speed")) {
            return "text.journeysend.card.modifier.movement_speed";
        } else if (source.contains("oxygen_bonus")) {
            return "text.journeysend.card.modifier.oxygen_bonus";
        } else if (source.contains("armor")) {
            return "text.journeysend.card.modifier.armor";
        } else if (source.contains("jump_strength")) {
            return "text.journeysend.card.modifier.jump_strength";
        } else if (source.contains("block_break_speed")) {
            return "text.journeysend.card.modifier.block_break_speed";
        } else if (source.contains("shiny_chance")) {
            return "text.journeysend.card.modifier.shiny_chance";
        } else if (source.contains("poison")) {
            return "text.journeysend.card.modifier.poison_influence";
        } else if (source.contains("fire")) {
            return "text.journeysend.card.modifier.fire_influence";
        } else if (source.contains("water")) {
            return "text.journeysend.card.modifier.water_influence";
        } else if (source.contains("electric")) {
            return "text.journeysend.card.modifier.electric_influence";
        } else if (source.contains("grass")) {
            return "text.journeysend.card.modifier.grass_influence";
        } else if (source.contains("damage")) {
            return "text.journeysend.card.modifier.damage";
        } else if (source.contains("speed")) {
            return "text.journeysend.card.modifier.speed";
        } else if (source.contains("health")) {
            return "text.journeysend.card.modifier.health";
        } else if (source.contains("attack")) {
            return "text.journeysend.card.modifier.attack";
        } else if (source.contains("defense")) {
            return "text.journeysend.card.modifier.defense";
        } else if (source.contains("spawn")) {
            return "text.journeysend.card.modifier.spawn_influence";
        } else {
            return "text.journeysend.card.modifier.unknown";
        }
    }

    private static String formatValue(Object value, abeshutt.staracademy.net.dto.ModifierDisplayDTO dto) {
        if (!(value instanceof Number n)) return String.valueOf(value);
        double d = n.doubleValue();
        return switch (dto.styleId()) {
            case "decimal_percentage" -> String.format(java.util.Locale.ROOT, "%.1f%%", d * 100.0);
            case "decimal" -> (Math.rint(d) == d)
                    ? Integer.toString((int) d)
                    : String.format(java.util.Locale.ROOT, "%.3f", d);
            default -> String.format(java.util.Locale.ROOT, "%.3f", d);
        };
    }

    private static String formatDouble(double d, abeshutt.staracademy.net.dto.ModifierDisplayDTO dto) {
        return switch (dto.styleId()) {
            case "decimal_percentage" -> String.format(java.util.Locale.ROOT, "%.1f%%", d * 100.0);
            case "decimal" -> (Math.rint(d) == d)
                    ? Integer.toString((int) d)
                    : String.format(java.util.Locale.ROOT, "%.3f", d);
            default -> String.format(java.util.Locale.ROOT, "%.3f", d);
        };
    }

    private static String formatFallback(Object value) {
        if (value instanceof Number n) {
            double d = n.doubleValue();
            return (Math.rint(d) == d) ? Integer.toString((int)d) : String.format(java.util.Locale.ROOT, "%.2f", d);
        }
        return String.valueOf(value);
    }

    public Text getPartText(Attribute<?> attribute, Object value, CardDisplayEntry display, boolean advanced) {
        if(attribute instanceof NumberConstantAttribute constant) {
            Rational min = this.getMin(constant.getConfig());
            Rational max = this.getMax(constant.getConfig());

            if(!advanced || min.equals(max)) {
                if(value.equals(min)) {
                    return Text.literal(display.getStyle().format(value)).setStyle(Style.EMPTY.withColor(display.getColor()));
                } else {
                    return Text.empty()
                            .append(Text.literal(display.getStyle().format(value)).setStyle(Style.EMPTY.withColor(display.getColor())))
                            .append(Text.literal(" [" + display.getStyle().format(min) + "]")).formatted(Formatting.GRAY);
                }
            } else {
                return Text.empty()
                        .append(Text.literal(display.getStyle().format(value)).setStyle(Style.EMPTY.withColor(display.getColor())))
                        .append(Text.literal(" [" + display.getStyle().format(min) + "-" + display.getStyle().format(max) + "]")).formatted(Formatting.GRAY);
            }
        } else if(attribute instanceof AssignAttribute<?> assign) {
            return this.getPartText(assign.getValue(), value, display, advanced);
        } else if(attribute instanceof AddAttribute<?> add) {
            return Text.empty()
                    .append(Text.literal("+ ")).setStyle(Style.EMPTY.withColor(display.getColor()))
                    .append(this.getPartText(add.getValue(), value, display, advanced));
        } else if(attribute instanceof MultiplyAttribute<?> multiply) {
            return Text.empty()
                    .append(Text.literal("× ")).setStyle(Style.EMPTY.withColor(display.getColor()))
                    .append(this.getPartText(multiply.getValue(), value, display, advanced));
        } else if(attribute instanceof CardScalarAttribute<?> scalar) {
            Rational value2 = scalar.getScalar().orElse(null);
            return Text.literal("× " + (value2 == null ? "1" : display.getStyle().format(value2))).setStyle(Style.EMPTY.withColor(display.getColor()));
        }

        throw new UnsupportedOperationException();
    }

    private Rational getMin(NumberRoll roll) {
        return switch(roll) {
            case ConstantNumberRoll r -> r.value;
            case UniformNumberRoll r -> this.getMin(r.minimum);
            case TrapezoidalNumberRoll r -> this.getMin(r.minimum);
            default -> throw new IllegalStateException("Unexpected value: " + roll);
        };
    }

    private Rational getMax(NumberRoll roll) {
        return switch(roll) {
            case ConstantNumberRoll r -> r.value;
            case UniformNumberRoll r -> this.getMin(r.maximum);
            case TrapezoidalNumberRoll r -> this.getMin(r.maximum);
            default -> throw new IllegalStateException("Unexpected value: " + roll);
        };
    }

    @Override
    public Optional<NbtCompound> writeNbt() {
        return Optional.of(new NbtCompound()).map(nbt -> {
            Adapters.UTF_8.writeNbt(this.source).ifPresent(tag -> nbt.put("source", tag));
            Adapters.ATTRIBUTE.writeNbt(this.attribute, any()).ifPresent(tag -> nbt.put("attribute", tag));
            return nbt;
        });
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        this.source = Adapters.UTF_8.readNbt(nbt.get("source")).orElseThrow();
        this.attribute = Adapters.ATTRIBUTE.readNbt(nbt.get("attribute"), any()).orElseThrow();
    }


}
