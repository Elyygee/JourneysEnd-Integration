package abeshutt.staracademy.outfit.core;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class OutfitTexture {
    protected int width;
    protected int height;
    protected Identifier model;
    protected ModelIdentifier icon;

    public OutfitTexture(int width, int height, Identifier model, ModelIdentifier icon) {
        this.width = width;
        this.height = height;
        this.model = model;
        this.icon = icon;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public Identifier getModel() {
        return this.model;
    }

    public ModelIdentifier getIcon() {
        return this.icon;
    }
}
