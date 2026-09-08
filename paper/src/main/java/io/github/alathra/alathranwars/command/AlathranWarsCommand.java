package io.github.alathra.alathranwars.command;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.executors.CommandArguments;
import io.github.alathra.alathranwars.AlathranWars;
import org.bukkit.command.CommandSender;

import static io.github.alathra.alathranwars.command.CommandHandler.BASE_PERM;

/**
 * Class containing the code for the base command.
 */
class AlathranWarsCommand {
    /**
     * Instantiates and registers a new command.
     */
    protected AlathranWarsCommand() {
        new CommandAPICommand("alathranwars.admin")
            .withHelp("Base command for AlathranWars.", "Base command for AlathranWars.")
            .withPermission(BASE_PERM + ".admin")
            .withSubcommands(
                new TranslationCommand().command(),
                new DumpCommand().command(),
                commandReload()
            )
            .register();
    }

    private CommandAPICommand commandReload() {
        return new CommandAPICommand("reload")
            .withHelp("Reload the configuration files.",
                "Reload config.yml and database.yml. Some settings, such as siege ranges and " +
                    "cooldowns, are read once at startup and need a server restart to take effect.")
            .withPermission(BASE_PERM + ".reload")
            .executes(this::executorReload);
    }

    private void executorReload(CommandSender sender, CommandArguments args) {
        AlathranWars.getInstance().getConfigHandler().reload();
    }
}
