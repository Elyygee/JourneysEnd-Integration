package abeshutt.staracademy.command;

import abeshutt.staracademy.StarAcademyMod;
import abeshutt.staracademy.block.SafariPortalBlock;
import abeshutt.staracademy.init.ModConfigs;
import abeshutt.staracademy.init.ModWorldData;
import abeshutt.staracademy.world.data.SafariData;
import fr.harmex.cobbledollars.common.utils.extensions.PlayerExtensionKt;
import java.math.BigInteger;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.TimeArgumentType;
import net.minecraft.command.argument.UuidArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Collection;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import com.mojang.brigadier.arguments.FloatArgumentType;

public class SafariCommand extends Command {

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess access, CommandManager.RegistrationEnvironment environment) {
        // /journeysend safari ... (combined OP and player commands)
        dispatcher.register(
            literal(StarAcademyMod.ID)
                .then(literal("safari")
                    .then(literal("pause").requires(source -> source.hasPermissionLevel(4)).executes(this::onPause))
                    .then(literal("unpause").requires(source -> source.hasPermissionLevel(4)).executes(this::onUnpause))
                    .then(literal("restart").requires(source -> source.hasPermissionLevel(4)).executes(this::onRestart))
                    .then(literal("add_time").requires(source -> source.hasPermissionLevel(4))
                        .then(argument("players", EntityArgumentType.players())
                            .then(argument("time", TimeArgumentType.time(Integer.MIN_VALUE))
                                .executes(this::onAddTime))))
                    .then(literal("confirm_unlock")
                        .then(argument("playerUuid", UuidArgumentType.uuid())
                            .executes(this::onConfirmUnlock)))
                    .then(literal("cancel_unlock").executes(this::onCancelUnlock)))
        );

        // Build /je subtree separately to avoid paren hell
        var je = literal("je");

        // /je safari ... (accessible to everyone, subcommands have individual permissions)
        je.then(
            literal("safari")
                .then(literal("pause").requires(source -> source.hasPermissionLevel(4)).executes(this::onPause))
                .then(literal("unpause").requires(source -> source.hasPermissionLevel(4)).executes(this::onUnpause))
                .then(literal("restart").requires(source -> source.hasPermissionLevel(4)).executes(this::onRestart))
                .then(literal("debug").requires(source -> source.hasPermissionLevel(4))
                    .then(literal("portal").executes(this::debugPortal)))
                .then(literal("alpharate").requires(source -> source.hasPermissionLevel(4))
                    .executes(this::getAlphaRate)
                    .then(argument("rate", FloatArgumentType.floatArg(0.0f))
                        .executes(this::setAlphaRate)))
                .then(literal("add_time").requires(source -> source.hasPermissionLevel(4))
                    .then(argument("players", EntityArgumentType.players())
                        .then(argument("time", TimeArgumentType.time(Integer.MIN_VALUE))
                            .executes(this::onAddTime))))
                .then(literal("confirm_unlock")
                    .then(argument("playerUuid", UuidArgumentType.uuid())
                        .executes(this::onConfirmUnlock)))
                .then(literal("cancel_unlock").executes(this::onCancelUnlock))
        );

        // /je lunar ...
        je.then(
            literal("lunar")
                .then(literal("check")
                    .executes(this::checkLunarEvent)
                    .then(argument("player", EntityArgumentType.player())
                        .requires(source -> source.hasPermissionLevel(2))
                        .executes(this::checkLunarEventForPlayer)))
                .then(literal("spawnrates").executes(this::checkSpawnRates))
                .then(literal("reload")
                    .requires(source -> source.hasPermissionLevel(2))
                    .executes(this::reloadConfig))
        );

        dispatcher.register(je);
    }

    private int onAddTime(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "players");
        long time = context.getArgument("time", Integer.class);

        SafariData data = ModWorldData.SAFARI.getGlobal(context.getSource().getServer());

        for(ServerPlayerEntity player : players) {
            SafariData.Entry entry = data.getOrCreate(player.getUuid());
            entry.setTimeLeft(entry.getTimeLeft() + time);
            Text timeMessage = getTimeMessage(player, time, false);
            context.getSource().sendFeedback(() -> timeMessage, true);
            player.sendMessage(getTimeMessage(player, time, true), false);
        }

        return 0;
    }

    private int onRestart(CommandContext<ServerCommandSource> context) {
        SafariData data = ModWorldData.SAFARI.getGlobal(context.getSource().getServer());
        data.onStart(context.getSource().getServer());

        context.getSource().sendFeedback(() -> {
            return Text.empty().append(Text.literal("The Safari has been restarted.").formatted(Formatting.GRAY));
        }, true);
        return 0;
    }

    private int onPause(CommandContext<ServerCommandSource> context) {
        SafariData data = ModWorldData.SAFARI.getGlobal(context.getSource().getServer());

        if(data.setPaused(true)) {
            context.getSource().sendFeedback(() -> {
                return Text.empty().append(Text.literal("The Safari is now ").formatted(Formatting.GRAY))
                        .append(Text.literal("paused").formatted(Formatting.RED)
                        .append(Text.literal(".").formatted(Formatting.GRAY)));
            }, true);
        } else {
            context.getSource().sendFeedback(() -> {
                return Text.empty().append(Text.literal("The Safari is already ").formatted(Formatting.GRAY))
                        .append(Text.literal("paused").formatted(Formatting.RED)
                        .append(Text.literal(".").formatted(Formatting.GRAY)));
            }, true);
        }

        return 0;
    }

    private int onUnpause(CommandContext<ServerCommandSource> context) {
        SafariData data = ModWorldData.SAFARI.getGlobal(context.getSource().getServer());

        if(data.setPaused(false)) {
            context.getSource().sendFeedback(() -> {
                return Text.empty().append(Text.literal("The Safari is now ").formatted(Formatting.GRAY))
                        .append(Text.literal("unpaused").formatted(Formatting.GREEN)
                        .append(Text.literal(".").formatted(Formatting.GRAY)));
            }, true);
        } else {
            context.getSource().sendFeedback(() -> {
                return Text.empty().append(Text.literal("The Safari is already ").formatted(Formatting.GRAY))
                        .append(Text.literal("unpaused").formatted(Formatting.GREEN)
                        .append(Text.literal(".").formatted(Formatting.GRAY)));
            }, true);
        }

        return 0;
    }

    // Lunar command methods - delegate to LunarEventCommand
    private int checkLunarEvent(CommandContext<ServerCommandSource> context) {
        try {
            // Use reflection to call the lunar command method on the Kotlin object instance
            Class<?> lunarCommandClass = Class.forName("abeshutt.staracademy.command.LunarEventCommand");
            java.lang.reflect.Field instanceField = lunarCommandClass.getDeclaredField("INSTANCE");
            instanceField.setAccessible(true);
            Object instance = instanceField.get(null);
            java.lang.reflect.Method method = lunarCommandClass.getDeclaredMethod("checkLunarEvent", CommandContext.class);
            method.setAccessible(true);
            return (Integer) method.invoke(instance, context);
        } catch (Exception e) {
            context.getSource().sendError(Text.literal("Failed to execute lunar command: " + e.getMessage()));
            return 0;
        }
    }

    private int checkLunarEventForPlayer(CommandContext<ServerCommandSource> context) {
        try {
            Class<?> lunarCommandClass = Class.forName("abeshutt.staracademy.command.LunarEventCommand");
            java.lang.reflect.Field instanceField = lunarCommandClass.getDeclaredField("INSTANCE");
            instanceField.setAccessible(true);
            Object instance = instanceField.get(null);
            java.lang.reflect.Method method = lunarCommandClass.getDeclaredMethod("checkLunarEventForPlayer", CommandContext.class);
            method.setAccessible(true);
            return (Integer) method.invoke(instance, context);
        } catch (Exception e) {
            context.getSource().sendError(Text.literal("Failed to execute lunar command: " + e.getMessage()));
            return 0;
        }
    }

    private int checkSpawnRates(CommandContext<ServerCommandSource> context) {
        try {
            Class<?> lunarCommandClass = Class.forName("abeshutt.staracademy.command.LunarEventCommand");
            java.lang.reflect.Field instanceField = lunarCommandClass.getDeclaredField("INSTANCE");
            instanceField.setAccessible(true);
            Object instance = instanceField.get(null);
            java.lang.reflect.Method method = lunarCommandClass.getDeclaredMethod("checkSpawnRates", CommandContext.class);
            method.setAccessible(true);
            return (Integer) method.invoke(instance, context);
        } catch (Exception e) {
            context.getSource().sendError(Text.literal("Failed to execute lunar command: " + e.getMessage()));
            return 0;
        }
    }

    private int reloadConfig(CommandContext<ServerCommandSource> context) {
        try {
            Class<?> lunarCommandClass = Class.forName("abeshutt.staracademy.command.LunarEventCommand");
            java.lang.reflect.Field instanceField = lunarCommandClass.getDeclaredField("INSTANCE");
            instanceField.setAccessible(true);
            Object instance = instanceField.get(null);
            java.lang.reflect.Method method = lunarCommandClass.getDeclaredMethod("reloadConfig", CommandContext.class);
            method.setAccessible(true);
            return (Integer) method.invoke(instance, context);
        } catch (Exception e) {
            context.getSource().sendError(Text.literal("Failed to execute lunar command: " + e.getMessage()));
            return 0;
        }
    }

    private int debugPortal(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player == null) {
            context.getSource().sendError(Text.literal("This command can only be used by players"));
            return 0;
        }

        var world = player.getWorld();
        var dimensionId = world.getRegistryKey().getValue();
        boolean isSafari = SafariPortalBlock.isSafariWorld((net.minecraft.server.world.ServerWorld) world);

        context.getSource().sendFeedback(() -> 
            Text.literal("Portal Debug Info:")
                .formatted(Formatting.GOLD, Formatting.BOLD), false);
        
        context.getSource().sendFeedback(() -> 
            Text.literal("Current Dimension: " + dimensionId)
                .formatted(Formatting.YELLOW), false);
        
        context.getSource().sendFeedback(() -> 
            Text.literal("isSafari: " + isSafari)
                .formatted(isSafari ? Formatting.GREEN : Formatting.RED), false);
        
        context.getSource().sendFeedback(() -> 
            Text.literal("Player Position: " + player.getBlockPos())
                .formatted(Formatting.AQUA), false);

        return 1;
    }

    private int onConfirmUnlock(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        java.util.UUID playerUuid = UuidArgumentType.getUuid(context, "playerUuid");
        ServerCommandSource source = context.getSource();
        
        // Find the player by UUID
        ServerPlayerEntity player = source.getServer().getPlayerManager().getPlayer(playerUuid);
        if (player == null) {
            source.sendError(Text.literal("Player not found"));
            return 0;
        }
        
        SafariData data = ModWorldData.SAFARI.getGlobal(player.getWorld());
        SafariData.Entry entry = data.getOrCreate(player.getUuid());

        // Check if player is OP and bypass unlock requirement
        boolean isOp = player.getServer() != null && player.getServer().getPlayerManager().isOperator(player.getGameProfile());
        
        if(!entry.isUnlocked() && !isOp) {
            BigInteger cost = BigInteger.valueOf(ModConfigs.NPC.getSafariCurrencyCost());
            if(PlayerExtensionKt.canBuy(player, cost)) {
                BigInteger currentBalance = PlayerExtensionKt.getCobbleDollars(player);
                PlayerExtensionKt.setCobbleDollars(player, currentBalance.subtract(cost));
                player.sendMessage(Text.empty().append(Text.translatable("text.journeysend.safari.unlock_complete")
                        .formatted(Formatting.GRAY)));
                entry.setUnlocked(true);
                entry.setPrompted(true);
                data.markDirty();
            } else {
                player.sendMessage(Text.empty()
                        .append(Text.translatable("text.journeysend.safari.unlock_broke", cost.toString()).formatted(Formatting.GRAY)));
            }
        }

        return 0;
    }

    private int onCancelUnlock(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        player.sendMessage(Text.empty()
                .append(Text.translatable("text.journeysend.safari.unlock_cancelled").formatted(Formatting.GRAY)));
        return 0;
    }

    private Text getTimeMessage(ServerPlayerEntity target, long ticks, boolean personal) {
        if(ticks >= 0) {
            return Text.empty()
                .append(Text.literal("Added ").formatted(Formatting.GREEN))
                .append(Text.literal(formatTimeString(ticks)).formatted(Formatting.WHITE))
                .append(Text.literal(" to ").formatted(Formatting.GRAY))
                .append(personal ? Text.literal("your").formatted(Formatting.GRAY) : target.getName())
                .append(Text.literal(personal ? " Safari." : "'s Safari timer.").formatted(Formatting.GRAY));
        } else {
            return Text.empty()
                .append(Text.literal("Removed ").formatted(Formatting.RED))
                .append(Text.literal(formatTimeString(-ticks)).formatted(Formatting.WHITE))
                .append(Text.literal(" from ").formatted(Formatting.GRAY))
                .append(personal ? Text.literal("your").formatted(Formatting.GRAY) : target.getName())
                .append(Text.literal(personal ? " Safari." : "'s Safari timer.").formatted(Formatting.GRAY));
        }
    }

    private String formatTimeString(long remainingTicks) {
        long seconds = (remainingTicks / 20) % 60;
        long minutes = ((remainingTicks / 20) / 60) % 60;
        long hours = ((remainingTicks / 20) / 60) / 60;
        return hours > 0
                ? String.format("%02d:%02d:%02d", hours, minutes, seconds)
                : String.format("%02d:%02d", minutes, seconds);
    }

    private int getAlphaRate(CommandContext<ServerCommandSource> context) {
        float currentRate = ModConfigs.SAFARI.getAlphaSpawnRateMultiplier();
        
        context.getSource().sendFeedback(() -> 
            Text.empty()
                .append(Text.literal("Current Safari Alpha Spawn Rate Multiplier: ")
                    .formatted(Formatting.GRAY))
                .append(Text.literal(String.format("%.2fx", currentRate))
                    .formatted(Formatting.GOLD, Formatting.BOLD))
        , false);
        
        return 1;
    }

    private int setAlphaRate(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        float newRate = FloatArgumentType.getFloat(context, "rate");
        ModConfigs.SAFARI.setAlphaSpawnRateMultiplier(newRate);
        
        try {
            ModConfigs.SAFARI.write();
        } catch (java.io.IOException e) {
            context.getSource().sendError(Text.literal("Failed to save config: " + e.getMessage()));
            return 0;
        }
        
        context.getSource().sendFeedback(() -> 
            Text.empty()
                .append(Text.literal("Safari Alpha Spawn Rate Multiplier set to: ")
                    .formatted(Formatting.GREEN))
                .append(Text.literal(String.format("%.2fx", newRate))
                    .formatted(Formatting.GOLD, Formatting.BOLD))
        , true);
        
        return 1;
    }

}
