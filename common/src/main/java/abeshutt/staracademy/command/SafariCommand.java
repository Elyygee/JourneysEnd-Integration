package abeshutt.staracademy.command;

import abeshutt.staracademy.StarAcademyMod;
import abeshutt.staracademy.block.SafariPortalBlock;
import abeshutt.staracademy.init.ModWorldData;
import abeshutt.staracademy.item.SafariTicketItem;
import abeshutt.staracademy.world.data.SafariData;
// Cobbledollars integration completed - using PlayerExtensionKt for balance operations
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.TimeArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Collection;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class SafariCommand extends Command {

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess access, CommandManager.RegistrationEnvironment environment) {
        // /journeysend safari ...
        dispatcher.register(
            literal(StarAcademyMod.ID)
                .then(literal("safari")
                    .requires(source -> source.hasPermissionLevel(4))
                    .then(literal("pause").executes(this::onPause))
                    .then(literal("unpause").executes(this::onUnpause))
                    .then(literal("restart").executes(this::onRestart))
                    .then(literal("add_time")
                        .then(argument("players", EntityArgumentType.players())
                            .then(argument("time", TimeArgumentType.time(Integer.MIN_VALUE))
                                .executes(this::onAddTime)))))
        );

        // Build /je subtree separately to avoid paren hell
        var je = literal("je");

        // /je safari ...
        je.then(
            literal("safari")
                .requires(source -> source.hasPermissionLevel(4))
                .then(literal("pause").executes(this::onPause))
                .then(literal("unpause").executes(this::onUnpause))
                .then(literal("restart").executes(this::onRestart))
                .then(literal("debug")
                    .then(literal("portal").executes(this::debugPortal)))
                .then(literal("add_time")
                    .then(argument("players", EntityArgumentType.players())
                        .then(argument("time", TimeArgumentType.time(Integer.MIN_VALUE))
                            .executes(this::onAddTime))))
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
            context.getSource().sendFeedback(() -> SafariTicketItem.getTimeMessage(player, time, false), true);
            player.sendMessage(SafariTicketItem.getTimeMessage(player, time, true), false);
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

}
