package io.github.alathra.alathranwars;

import io.github.alathra.alathranwars.command.CommandHandler;
import io.github.alathra.alathranwars.config.ConfigHandler;
import io.github.alathra.alathranwars.conflict.SaveHandler;
import io.github.alathra.alathranwars.conflict.war.SpawnController;
import io.github.alathra.alathranwars.conflict.war.WarController;
import io.github.alathra.alathranwars.cooldown.CooldownHandler;
import io.github.alathra.alathranwars.database.handler.DatabaseHandler;
import io.github.alathra.alathranwars.deathspectate.DeathHandler;
import io.github.alathra.alathranwars.hook.Hook;
import io.github.alathra.alathranwars.hook.HookManager;
import io.github.alathra.alathranwars.listener.ListenerHandler;
import io.github.alathra.alathranwars.messaging.MessagingHandler;
import io.github.alathra.alathranwars.threadutil.SchedulerHandler;
import io.github.alathra.alathranwars.translation.TranslationHandler;
import io.github.alathra.alathranwars.updatechecker.UpdateHandler;
import io.github.alathra.alathranwars.utility.DB;
import io.github.alathra.alathranwars.utility.Logger;
import io.github.alathra.alathranwars.utility.Messaging;
import io.github.milkdrinkers.colorparser.paper.ColorParser;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import space.arim.morepaperlib.MorePaperLib;

import java.util.List;

public class AlathranWars extends AbstractAlathranWars {
    private static AlathranWars instance;
    private AlathranWarsAPIProvider apiProvider;
    private MorePaperLib paperLib;

    private ConfigHandler configHandler;
    private TranslationHandler translationHandler;
    private DatabaseHandler databaseHandler;
    private MessagingHandler messagingHandler;
    private HookManager hookManager;
    private SaveHandler saveHandler;
    private CommandHandler commandHandler;
    private ListenerHandler listenerHandler;
    private UpdateHandler updateHandler;
    private SchedulerHandler schedulerHandler;
    private CooldownHandler cooldownHandler;
    private DeathHandler deathHandler;

    // Handlers list (defines order of load/enable/disable)
    private List<? extends Reloadable> handlers;

    public static PlainTextComponentSerializer plainTextComponentSerializer = PlainTextComponentSerializer.plainText();

    @Override
    public void onLoad() {
        instance = this;
        apiProvider = new AlathranWarsAPIProvider(this);
        paperLib = new MorePaperLib(this);

        configHandler = new ConfigHandler(this);
        translationHandler = new TranslationHandler(configHandler);
        databaseHandler = DatabaseHandler.builder()
            .withConfigHandler(configHandler)
            .withLogger(getComponentLogger())
            .withMigrate(true)
            .build();
        messagingHandler = MessagingHandler.builder()
            .withLogger(getComponentLogger())
            .withName(getName())
            .build();
        hookManager = new HookManager(this);
        saveHandler = new SaveHandler();
        commandHandler = new CommandHandler(this);
        listenerHandler = new ListenerHandler(this);
        updateHandler = new UpdateHandler(this);
        schedulerHandler = new SchedulerHandler();
        cooldownHandler = new CooldownHandler();
        deathHandler = new DeathHandler();

        handlers = List.of(
            apiProvider,
            configHandler,
            translationHandler,
            databaseHandler,
            messagingHandler,
            hookManager,
            saveHandler,
            commandHandler,
            listenerHandler,
            updateHandler,
            schedulerHandler,
            cooldownHandler,
            deathHandler,
            WarController.getInstance(),
            SpawnController.getInstance()
        );

        DB.init(databaseHandler);
        Messaging.init(messagingHandler);

        for (Reloadable handler : handlers)
            handler.onLoad(instance);
    }

    @Override
    public void onEnable() {
        for (Reloadable handler : handlers)
            handler.onEnable(instance);

        if (!DB.isStarted()) {
            Logger.get().warn(ColorParser.of("<yellow>Database handler failed to start. Database support has been disabled.").build());
            Bukkit.getPluginManager().disablePlugin(this);
        }

        if (!Messaging.isReady() && configHandler.getDatabaseConfig().messaging.enabled) {
            Logger.get().warn(ColorParser.of("<yellow>Messaging handler failed to start. Messaging support has been disabled.").build());
            Bukkit.getPluginManager().disablePlugin(this);
        }

        if (Hook.getVaultHook().isHookLoaded()) {
            Logger.get().info(ColorParser.of("<green>Vault has been found on this server. Vault support enabled.").build());
        } else {
            Logger.get().warn(ColorParser.of("<yellow>Vault is not installed on this server. Vault support has been disabled.").build());
        }

        if (Hook.getPacketEventsHook().isHookLoaded()) {
            Logger.get().info(ColorParser.of("<green>PacketEvents has been found on this server. PacketEvents support enabled.").build());
        } else {
            Logger.get().warn(ColorParser.of("<yellow>PacketEvents is not installed on this server. PacketEvents support has been disabled.").build());
        }

    }

    @Override
    public void onDisable() {
        for (Reloadable handler : handlers.reversed()) // If reverse doesn't work implement a new List with your desired disable order
            handler.onDisable(instance);
    }

    /**
     * Gets plugin instance.
     *
     * @return the plugin instance
     */
    @NotNull
    public static AlathranWars getInstance() {
        return instance;
    }

    /**
     * Gets more paper lib instance.
     *
     * @return the more paper lib instance
     */
    @NotNull
    public MorePaperLib getPaperLib() {
        return paperLib;
    }

    /**
     * Gets config handler.
     *
     * @return the config handler
     */
    @Override
    @NotNull
    public ConfigHandler getConfigHandler() {
        return configHandler;
    }

    /**
     * Gets hook manager.
     *
     * @return the hook manager
     */
    @NotNull
    public HookManager getHookManager() {
        return hookManager;
    }

    /**
     * Gets update handler.
     *
     * @return the update handler
     */
    @NotNull
    public UpdateHandler getUpdateHandler() {
        return updateHandler;
    }

}
