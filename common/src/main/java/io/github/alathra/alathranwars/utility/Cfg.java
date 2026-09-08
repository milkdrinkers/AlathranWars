package io.github.alathra.alathranwars.utility;

import io.github.alathra.alathranwars.AbstractAlathranWars;
import io.github.alathra.alathranwars.config.ConfigHandler;
import io.github.alathra.alathranwars.config.PluginConfig;
import org.jetbrains.annotations.NotNull;

/**
 * Convenience class for accessing {@link ConfigHandler#getConfig}
 */
public final class Cfg {
    /**
     * Convenience method for {@link ConfigHandler#getConfig} to get the {@link PluginConfig}
     *
     * @return the config
     */
    @NotNull
    public static PluginConfig get() {
        return AbstractAlathranWars.getInstance().getConfigHandler().getConfig();
    }
}
