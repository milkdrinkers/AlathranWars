package io.github.alathra.alathranwars;

import io.github.alathra.alathranwars.config.ConfigHandler;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractAlathranWars extends JavaPlugin {
    private static AbstractAlathranWars instance;

    /**
     * Gets plugin instance.
     *
     * @return the plugin instance
     */
    public static AbstractAlathranWars getInstance() {
        return AbstractAlathranWars.instance;
    }

    AbstractAlathranWars() {
        AbstractAlathranWars.instance = this;
    }

    /**
     * Gets config handler.
     *
     * @return the config handler
     */
    public abstract @NotNull ConfigHandler getConfigHandler();
}
