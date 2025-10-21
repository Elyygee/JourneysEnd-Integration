package abeshutt.staracademy.item;

import abeshutt.staracademy.init.ModWorldData;
import abeshutt.staracademy.world.data.SafariData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.List;

public class SafariTicketGoldenItem extends Item {
    
    public SafariTicketGoldenItem() {
        super(new Settings());
    }
    
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        
        if (user.getWorld().isClient()) {
            return TypedActionResult.pass(stack);
        }
        
        if (user.getServer() == null || world.getServer() == null) {
            return TypedActionResult.pass(stack);
        }
        
        SafariData data = ModWorldData.SAFARI.getGlobal(world.getServer());
        
        // Golden ticket gives random time between 40-60 minutes
        int minTime = 20 * 60 * 40; // 40 minutes
        int maxTime = 20 * 60 * 60; // 60 minutes
        int time = minTime + (int)(Math.random() * (maxTime - minTime + 1));
        
        SafariData.Entry entry = data.getOrCreate(user.getUuid());
        entry.setTimeLeft(entry.getTimeLeft() + time);
        user.sendMessage(getTimeMessage(user, time, true));
        
        if (!user.isCreative()) {
            stack.decrement(1);
        }
        
        return TypedActionResult.success(stack);
    }
    
    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        super.appendTooltip(stack, context, tooltip, type);
        tooltip.add(Text.literal("Adds between 40 and 60 minutes to your Safari timer.").formatted(Formatting.GRAY));
    }
    
    private Text getTimeMessage(PlayerEntity target, long ticks, boolean personal) {
        return Text.empty()
            .append(Text.literal("Added ").formatted(Formatting.GREEN))
            .append(Text.literal(formatTimeString(ticks)).formatted(Formatting.WHITE))
            .append(Text.literal(" to ").formatted(Formatting.GRAY))
            .append(personal ? Text.literal("your").formatted(Formatting.GRAY) : target.getName())
            .append(Text.literal(personal ? " Safari." : "'s Safari timer.").formatted(Formatting.GRAY));
    }
    
    private String formatTimeString(long remainingTicks) {
        long seconds = (remainingTicks / 20) % 60;
        long minutes = ((remainingTicks / 20) / 60) % 60;
        long hours = ((remainingTicks / 20) / 60) / 60;
        return hours > 0
                ? String.format("%02d:%02d:%02d", hours, minutes, seconds)
                : String.format("%02d:%02d", minutes, seconds);
    }
}
