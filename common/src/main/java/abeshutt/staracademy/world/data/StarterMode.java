package abeshutt.staracademy.world.data;

import net.minecraft.util.StringIdentifiable;

public enum StarterMode implements StringIdentifiable {

    DEFAULT, RAFFLE_ENABLED, RAFFLE_PAUSED;

    public static final com.mojang.serialization.Codec<StarterMode> CODEC = StringIdentifiable.createCodec(StarterMode::values);

    @Override
    public String asString() {
        return this.name();
    }

}
