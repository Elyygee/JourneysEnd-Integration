package abeshutt.staracademy.command;

import abeshutt.staracademy.StarAcademyMod;
import abeshutt.staracademy.init.ModConfigs;
import abeshutt.staracademy.init.ModWorldData;
import abeshutt.staracademy.world.data.CardGradingData;
import fr.harmex.cobbledollars.common.utils.extensions.PlayerExtensionKt;
import java.math.BigInteger;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.UuidArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static net.minecraft.server.command.CommandManager.literal;
import static net.minecraft.server.command.CommandManager.argument;

public class GraderCommand extends Command {

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess access, CommandManager.RegistrationEnvironment environment) {
        // /journeysend grader confirm_unlock and cancel_unlock (player commands)
        dispatcher.register(
            literal(StarAcademyMod.ID)
                .then(literal("grader")
                    .then(literal("confirm_unlock")
                        .then(argument("playerUuid", UuidArgumentType.uuid())
                            .executes(this::onConfirmUnlock)))
                    .then(literal("cancel_unlock").executes(this::onCancelUnlock)))
        );

        // Build /je subtree separately to avoid paren hell
        var je = literal("je");

        // /je grader confirm_unlock and cancel_unlock (player commands)
        je.then(
            literal("grader")
                .then(literal("confirm_unlock")
                    .then(argument("playerUuid", UuidArgumentType.uuid())
                        .executes(this::onConfirmUnlock)))
                .then(literal("cancel_unlock").executes(this::onCancelUnlock))
        );

        // Register the /je command
        dispatcher.register(je);
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
        
        CardGradingData data = ModWorldData.CARD_GRADING.getGlobal(player.getWorld());
        
        // Check if player has a card to grade
        if (data.has(player.getUuid())) {
            BigInteger cost = BigInteger.valueOf(ModConfigs.NPC.getGradingCurrencyCost());
            if(PlayerExtensionKt.canBuy(player, cost)) {
                BigInteger currentBalance = PlayerExtensionKt.getCobbleDollars(player);
                PlayerExtensionKt.setCobbleDollars(player, currentBalance.subtract(cost));
                player.sendMessage(Text.empty().append(Text.translatable("text.journeysend.grader.unlock_complete")
                        .formatted(Formatting.GRAY)));
                // The card grading process continues as normal
            } else {
                player.sendMessage(Text.empty()
                        .append(Text.translatable("text.journeysend.grader.unlock_broke", cost.toString()).formatted(Formatting.GRAY)));
            }
        } else {
            player.sendMessage(Text.empty()
                    .append(Text.translatable("text.journeysend.grader.no_card").formatted(Formatting.GRAY)));
        }

        return 0;
    }

    private int onCancelUnlock(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        player.sendMessage(Text.empty()
                .append(Text.translatable("text.journeysend.grader.unlock_cancelled").formatted(Formatting.GRAY)));
        return 0;
    }
}
