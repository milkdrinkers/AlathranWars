package io.github.alathra.alathranwars.command;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIPaperConfig;
import io.github.alathra.alathranwars.AbstractAlathranWars;
import io.github.alathra.alathranwars.AlathranWars;
import io.github.alathra.alathranwars.Reloadable;
import io.github.alathra.alathranwars.command.towny.TownyCommandHandler;

/**
 * A class to handle registration of commands.
 */
public class CommandHandler implements Reloadable {
    public static final String BASE_PERM = "alathranwars.command";
    private final AlathranWars plugin;
    private final TownyCommandHandler townyHandler;

    /**
     * Instantiates the Command handler.
     *
     * @param plugin the plugin
     */
    public CommandHandler(AlathranWars plugin) {
        this.plugin = plugin;
        this.townyHandler = new TownyCommandHandler();
    }

    @Override
    public void onLoad(AbstractAlathranWars plugin) {
        CommandAPI.onLoad(
            new CommandAPIPaperConfig(plugin)
                .silentLogs(true)
        );
        townyHandler.onLoad(plugin);
    }

    @Override
    public void onEnable(AbstractAlathranWars plugin) {
        if (!CommandAPI.isLoaded())
            return;

        CommandAPI.onEnable();

        // Register commands here
        new AlathranWarsCommand();
        new WarCommands();
        new SiegeCommands();
        new AdminCommands();
        AdminCommands.commandRespawn().register();

        townyHandler.onEnable(plugin);
    }

    @Override
    public void onDisable(AbstractAlathranWars plugin) {
        if (!CommandAPI.isLoaded())
            return;

        townyHandler.onDisable(plugin);
        CommandAPI.getRegisteredCommands().forEach(registeredCommand -> CommandAPI.unregister(registeredCommand.namespace() + ':' + registeredCommand.commandName(), true));
        CommandAPI.onDisable();
    }
}