package abeshutt.staracademy.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class LootBagItem extends Item {

    private final int color;

    public LootBagItem(int color, Settings settings) {
        super(settings);
        this.color = color;
    }

    @Override
    public Text getName(ItemStack stack) {
        return super.getName(stack).copy().setStyle(Style.EMPTY.withColor(this.color));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        // Let the MixinItemStack handle the logic, just return success to allow consumption
        return TypedActionResult.success(user.getStackInHand(hand));
    }

}
