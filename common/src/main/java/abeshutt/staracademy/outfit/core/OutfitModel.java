package abeshutt.staracademy.outfit.core;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.model.PlayerEntityModel;

@Environment(EnvType.CLIENT)
public class OutfitModel extends PlayerEntityModel<AbstractClientPlayerEntity> {

    public OutfitModel(ModelPart root) {
        super(root, false);
    }

}
