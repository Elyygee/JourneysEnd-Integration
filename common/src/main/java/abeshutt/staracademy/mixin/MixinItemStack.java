package abeshutt.staracademy.mixin;

import abeshutt.staracademy.init.ModConfigs;
import abeshutt.staracademy.util.ItemUseLogic;
import abeshutt.staracademy.util.UuidUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(ItemStack.class)
public abstract class MixinItemStack {

    @Shadow public abstract Item getItem();

    // Helper to run logic for all use paths without double-triggering
    private static boolean je$runLogicIfMatch(World world, PlayerEntity user, Hand hand) {
        if (world.isClient || !(user instanceof ServerPlayerEntity sp)) return false;

        ItemStack stack = user.getStackInHand(hand);
        ItemUseLogic logic = ModConfigs.ITEM_LOGIC.getUseLogic(stack).orElse(null);
        if (logic == null) return false;

        for (String cmd : logic.getCommands()) {
            String rendered = cmd
                .replace("${user_uuid}", UuidUtils.toString(user.getUuid()))
                .replace("${user_name}", user.getGameProfile().getName());
            runUseLogic(world, sp, logic, rendered);
        }

        if (logic.isConsumable() && !sp.isCreative()) {
            stack.decrement(1); // consume the stack actually used (respects offhand)
        }

        return true; // Indicate that we handled this item
    }

    // 1) After air use - handle items that execute ItemUseLogic commands
    @Inject(method = "use", at = @At("TAIL"))
    private void je$afterUse(World world, PlayerEntity user, Hand hand,
                             CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        // Run ItemUseLogic if available, but don't cancel the original action
        // This allows custom items (like BoosterPackItem) to execute their own logic first
        ItemStack stack = user.getStackInHand(hand);
        ItemUseLogic logic = ModConfigs.ITEM_LOGIC.getUseLogic(stack).orElse(null);
        if (logic != null && cir.getReturnValue().getResult().isAccepted()) {
            // Only execute commands if the item action was successful
            if (world.isClient || !(user instanceof ServerPlayerEntity sp)) return;
            
            for (String cmd : logic.getCommands()) {
                String rendered = cmd
                    .replace("${user_uuid}", UuidUtils.toString(user.getUuid()))
                    .replace("${user_name}", user.getGameProfile().getName());
                runUseLogic(world, sp, logic, rendered);
            }
            
            // Note: Don't consume the item here as the custom item's use() handles that
        }
    }

    // 2) After block use
    @Inject(method = "useOnBlock", at = @At("TAIL"))
    private void je$afterUseOnBlock(net.minecraft.item.ItemUsageContext ctx,
                                    CallbackInfoReturnable<net.minecraft.util.ActionResult> cir) {
        // If block interaction was accepted, use() will NOT be called => run here.
        if (cir.getReturnValue().isAccepted()) {
            je$runLogicIfMatch(ctx.getWorld(), ctx.getPlayer(), ctx.getHand());
        }
    }

    // 3) After entity use (right-click on entities)
    @Inject(method = "useOnEntity", at = @At("TAIL"))
    private void je$afterUseOnEntity(PlayerEntity user, net.minecraft.entity.LivingEntity target, Hand hand,
                                     CallbackInfoReturnable<net.minecraft.util.ActionResult> cir) {
        if (cir.getReturnValue().isAccepted()) {
            je$runLogicIfMatch(user.getWorld(), user, hand);
        }
    }

    private static void runUseLogic(World world, ServerPlayerEntity player, ItemUseLogic logic, String cmdRaw) {
        if (world.isClient) return;
        String raw = cmdRaw.startsWith("/") ? cmdRaw.substring(1) : cmdRaw;
        MinecraftServer server = ((ServerWorld) world).getServer();

        switch (logic.getContext()) {
            case SERVER -> server.getCommandManager()
                                 .executeWithPrefix(server.getCommandSource(), raw);
            case PLAYER -> server.getCommandManager()
                                 .executeWithPrefix(player.getCommandSource(), raw);
        }
    }

}
