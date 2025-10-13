package abeshutt.staracademy.outfit.models;

import abeshutt.staracademy.StarAcademyMod;
import abeshutt.staracademy.outfit.core.OutfitPiece;
import abeshutt.staracademy.outfit.core.OutfitTexture;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;

public class Comfy1Outfit {

    public static class Hat extends OutfitPiece {

        public Hat(String id) {
            super(id);
        }

        @Override
        protected void buildMesh(ModelPartData modelPartData) {
            ModelPartData head = modelPartData.addChild("head", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 0.0F, 0.0F));

            ModelPartData cube_r1 = head.addChild("cube_r1", ModelPartBuilder.create().uv(0, 0).cuboid(-5.0F, -2.5F, -5.0F, 10.0F, 3.0F, 10.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, -8.25F, 0.0F, -0.211F, 0.056F, 0.2559F));
        }

        @Override
        protected OutfitTexture buildTexture() {
            return new OutfitTexture(64, 64,
                    StarAcademyMod.id("textures/entity/outfit/comfy1_top.png"),
                    StarAcademyMod.mid("outfit/comfy1_hat", "inventory")
            );
        }

    }

    public static class Shirt extends OutfitPiece {

        public Shirt(String id) {
            super(id);
        }

        @Override
        protected void buildMesh(ModelPartData modelPartData) {
            ModelPartData body = modelPartData.addChild("body", ModelPartBuilder.create().uv(0, 13).cuboid(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new Dilation(0.75F))
                    .uv(16, 29).cuboid(-1.5F, 1.0F, -3.5F, 3.0F, 2.0F, 1.0F, new Dilation(0.0F))
                    .uv(40, 0).cuboid(-1.0F, 3.0F, -3.0F, 2.0F, 7.0F, 0.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 0.0F, 0.0F));

            ModelPartData right_arm = modelPartData.addChild("right_arm", ModelPartBuilder.create().uv(24, 13).cuboid(-4.25F, 1.0F, -3.5F, 5.0F, 2.0F, 7.0F, new Dilation(0.0F))
                    .uv(24, 22).cuboid(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new Dilation(0.74F)), ModelTransform.pivot(-5.0F, 2.0F, 0.0F));

            ModelPartData left_arm = modelPartData.addChild("left_arm", ModelPartBuilder.create().uv(24, 13).mirrored().cuboid(-0.75F, 1.0F, -3.5F, 5.0F, 2.0F, 7.0F, new Dilation(0.0F)).mirrored(false)
                    .uv(0, 29).cuboid(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new Dilation(0.74F)), ModelTransform.pivot(5.0F, 2.0F, 0.0F));
        }

        @Override
        protected OutfitTexture buildTexture() {
            return new OutfitTexture(64, 64,
                    StarAcademyMod.id("textures/entity/outfit/comfy1_top.png"),
                    StarAcademyMod.mid("outfit/comfy1_shirt", "inventory")
            );
        }

    }

    public static class Pants extends OutfitPiece {

        public Pants(String id) {
            super(id);
        }

        @Override
        protected void buildMesh(ModelPartData modelPartData) {
            ModelPartData body = modelPartData.addChild("body", ModelPartBuilder.create().uv(0, 0).cuboid(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new Dilation(0.51F)), ModelTransform.pivot(0.0F, 0.0F, 0.0F));

            ModelPartData right_leg = modelPartData.addChild("right_leg", ModelPartBuilder.create().uv(0, 16).cuboid(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new Dilation(0.5F)), ModelTransform.pivot(-1.9F, 12.0F, 0.0F));

            ModelPartData left_leg = modelPartData.addChild("left_leg", ModelPartBuilder.create().uv(16, 16).cuboid(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new Dilation(0.5F)), ModelTransform.pivot(1.9F, 12.0F, 0.0F));
        }

        @Override
        protected OutfitTexture buildTexture() {
            return new OutfitTexture(32, 32,
                    StarAcademyMod.id("textures/entity/outfit/comfy1_bottom.png"),
                    StarAcademyMod.mid("outfit/comfy1_pants", "inventory")
            );
        }

    }

    public static class Shoes extends OutfitPiece {

        public Shoes(String id) {
            super(id);
        }

        @Override
        protected void buildMesh(ModelPartData modelPartData) {
            ModelPartData right_leg = modelPartData.addChild("right_leg", ModelPartBuilder.create().uv(16, 38).cuboid(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new Dilation(0.75F)), ModelTransform.pivot(-1.9F, 12.0F, 0.0F));

            ModelPartData left_leg = modelPartData.addChild("left_leg", ModelPartBuilder.create().uv(32, 38).cuboid(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new Dilation(0.75F)), ModelTransform.pivot(1.9F, 12.0F, 0.0F));
        }

        @Override
        protected OutfitTexture buildTexture() {
            return new OutfitTexture(64, 64,
                    StarAcademyMod.id("textures/entity/outfit/comfy1_top.png"),
                    StarAcademyMod.mid("outfit/comfy1_shoes", "inventory")
            );
        }

    }

}
