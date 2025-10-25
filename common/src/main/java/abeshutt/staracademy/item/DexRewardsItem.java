package abeshutt.staracademy.item;

import abeshutt.staracademy.util.ClientScheduler;
import abeshutt.staracademy.util.ColorBlender;
import dev.architectury.platform.Platform;
import net.fabricmc.api.EnvType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class DexRewardsItem extends Item {

    public DexRewardsItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        // Return success to allow the ItemUseLogic to execute
        return TypedActionResult.success(stack, world.isClient());
    }

    @Override
    public Text getName(ItemStack stack) {
        double time = 0.0D;

        if(Platform.getEnv() == EnvType.CLIENT) {
            time = ClientScheduler.getTick(MinecraftClient.getInstance().getRenderTickCounter()
                    .getTickDelta(true));
        }

        return styleText(super.getName(stack).getString(), time, 10);
    }

    private static Text styleText(String string, double time, float offset) {
        MutableText text = Text.empty();
        int count = 0;

        for(int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            text = text.append(Text.literal(String.valueOf(c))
                    .setStyle(Style.EMPTY.withColor(getColor(time + count * offset))));
            if(c != ' ') count++;
        }

        return text;
    }

    public static Integer getColor(double time) {
        ColorBlender blender = new ColorBlender(1.5F)
                .add(0xff6b6b, 250.0F)  // Light red
                .add(0xffb3ba, 250.0F); // Light pink
        return blender.getColor(time);
    }
}
