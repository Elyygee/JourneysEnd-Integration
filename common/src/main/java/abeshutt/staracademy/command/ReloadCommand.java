package abeshutt.staracademy.command;

import abeshutt.staracademy.StarAcademyMod;
import abeshutt.staracademy.init.ModConfigs;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static net.minecraft.server.command.CommandManager.literal;

public class ReloadCommand extends Command {

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess access, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(literal(StarAcademyMod.ID)
                .then(literal("reload")
                    .requires(source -> source.hasPermissionLevel(4))
                    .executes(this::onReload)));
    }

    private int onReload(CommandContext<ServerCommandSource> context) {
        try {
            ModConfigs.register(false);
            context.getSource().sendFeedback(() -> Text.literal("Reloaded configs successfully.").formatted(Formatting.GRAY), true);
        } catch(Exception e) {
            context.getSource().sendFeedback(() -> Text.literal("Failed to reload configs.").formatted(Formatting.GRAY), true);
            throw e;
        }

        return 0;
    }

}
