package abeshutt.staracademy.card;

import abeshutt.staracademy.data.adapter.basic.TypeSupplierAdapter;
import abeshutt.staracademy.data.serializable.ISerializable;
import com.google.gson.JsonObject;
import net.minecraft.nbt.NbtCompound;

import java.util.Optional;

public abstract class AttributeStyle<T> implements ISerializable<NbtCompound, JsonObject> {

    public static final Adapter ADAPTER = new Adapter();

    public abstract String format(T value);

    /** Stable identifier for network sync / client formatting. */
    public String id() {
        // default to the adapter's registration name if you can get it,
        // else fall back to a sensible constant
        if (this instanceof DecimalPercentageAttributeStyle) return "decimal_percentage";
        return "decimal";
    }

    @Override
    public Optional<JsonObject> writeJson() {
        return Optional.of(new JsonObject());
    }

    @Override
    public void readJson(JsonObject json) {

    }

    public static class Adapter extends TypeSupplierAdapter<AttributeStyle<?>> {
        public Adapter() {
            super("type", false);
            this.register("decimal", DecimalAttributeStyle.class, DecimalAttributeStyle::new);
            this.register("decimal_percentage", DecimalPercentageAttributeStyle.class, DecimalPercentageAttributeStyle::new);
        }
    }

}
