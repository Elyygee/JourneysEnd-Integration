package abeshutt.staracademy.outfit.models;

import abeshutt.staracademy.StarAcademyMod;
import abeshutt.staracademy.outfit.core.OutfitPiece;
import abeshutt.staracademy.outfit.core.OutfitTexture;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;

public class SweaterOutfit {

    public static class Top extends OutfitPiece {

        public Top(String id) {
            super(id);
        }

        @Override
        protected void buildMesh(ModelPartData modelPartData) {
            ModelPartData body = modelPartData.addChild("body", ModelPartBuilder.create().uv(0, 0).cuboid(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new Dilation(0.75F))
                    .uv(0, 16).cuboid(-4.0F, 0.0F, -3.0F, 8.0F, 4.0F, 6.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 0.0F, 0.0F));

            ModelPartData right_arm = modelPartData.addChild("right_arm", ModelPartBuilder.create().uv(24, 0).cuboid(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new Dilation(0.74F))
                    .uv(32, 32).cuboid(-4.0F, 6.0F, -3.0F, 5.0F, 2.0F, 6.0F, new Dilation(0.0F))
                    .uv(32, 32).cuboid(9.0F, 6.0F, -3.0F, 5.0F, 2.0F, 6.0F, new Dilation(0.0F)), ModelTransform.pivot(-5.0F, 2.0F, 0.0F));

            ModelPartData left_arm = modelPartData.addChild("left_arm", ModelPartBuilder.create().uv(0, 26).cuboid(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new Dilation(0.74F)), ModelTransform.pivot(5.0F, 2.0F, 0.0F));
        }

        @Override
        protected OutfitTexture buildTexture() {
            return new OutfitTexture(64, 64,
                    StarAcademyMod.id("textures/entity/outfit/sweater_top.png"),
                    StarAcademyMod.mid("outfit/sweater_top", "inventory")
            );
        }

    }

    public static class Boots extends OutfitPiece {

        public Boots(String id) {
            super(id);
        }

        @Override
        protected void buildMesh(ModelPartData modelPartData) {
            ModelPartData right_leg = modelPartData.addChild("right_leg", ModelPartBuilder.create().uv(16, 26).cuboid(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new Dilation(0.75F)), ModelTransform.pivot(-1.9F, 12.0F, 0.0F));

            ModelPartData left_leg = modelPartData.addChild("left_leg", ModelPartBuilder.create().uv(32, 16).cuboid(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new Dilation(0.75F)), ModelTransform.pivot(1.9F, 12.0F, 0.0F));
        }

        @Override
        protected OutfitTexture buildTexture() {
            return new OutfitTexture(64, 64,
                    StarAcademyMod.id("textures/entity/outfit/sweater_top.png"),
                    StarAcademyMod.mid("outfit/sweater_boots", "inventory")
            );
        }

    }

}
