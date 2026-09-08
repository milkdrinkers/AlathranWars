package io.github.alathra.alathranwars.config;

import io.github.alathra.alathranwars.AbstractAlathranWars;
import io.github.alathra.alathranwars.Reloadable;
import io.github.alathra.alathranwars.config.loading.ConfigLoader;
import io.github.alathra.alathranwars.config.typeserializer.StringListSerializer;
import io.github.alathra.alathranwars.config.typeserializer.StringObjectMapSerializer;
import org.slf4j.Logger;

import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.Path;

/**
 * A class that generates/loads {@literal &} provides access to a configuration file.
 */
@Singleton
public class ConfigHandler implements Reloadable {
    private final Path configDir;
    private final Logger logger;

    private PluginConfig cfg;
    private DatabaseConfig databaseCfg;

    /**
     * Instantiates a new Config handler.
     *
     * @param plugin the plugin instance
     */
    public ConfigHandler(AbstractAlathranWars plugin) {
        this.configDir = plugin.getDataFolder().toPath();
        this.logger = plugin.getComponentLogger();
    }

    public ConfigHandler(AbstractAlathranWars plugin, Path configDir, Logger logger) {
        this.configDir = configDir;
        this.logger = logger;
    }

    @Override
    public void onLoad(AbstractAlathranWars plugin) {
        try {
            cfg = pluginConfigLoader().buildOrThrow(PluginConfig.class);
            databaseCfg = databaseConfigLoader().buildOrThrow(DatabaseConfig.class);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load configuration: " + e.getMessage(), e);
        }
    }

    @Override
    public void onEnable(AbstractAlathranWars plugin) {
    }

    @Override
    public void onDisable(AbstractAlathranWars plugin) {
    }

    /**
     * Gets main config object.
     *
     * @return the config object
     */
    public PluginConfig getConfig() {
        return cfg;
    }

    /**
     * Gets database config object.
     *
     * @return the config object
     */
    public DatabaseConfig getDatabaseConfig() {
        return databaseCfg;
    }

    /**
     * Reloads both configuration files.
     */
    public void reload() {
        final PluginConfig newCfg = pluginConfigLoader().build(PluginConfig.class);
        if (newCfg != null)
            cfg = newCfg;

        final DatabaseConfig newDatabaseCfg = databaseConfigLoader().build(DatabaseConfig.class);
        if (newDatabaseCfg != null)
            databaseCfg = newDatabaseCfg;
    }

    private ConfigLoader pluginConfigLoader() {
        return new ConfigLoader()
            .withLogger(logger)
            .withDirectory()
            .withPath(configDir.resolve("config.yml"))
            .withHeader("");
    }

    private ConfigLoader databaseConfigLoader() {
        return new ConfigLoader()
            .withLogger(logger)
            .withDirectory()
            .withPath(configDir.resolve("database.yml"))
            .withHeader("")
            .withSerializer(b -> {
                b.registerExact(StringListSerializer.TYPE_TOKEN, StringListSerializer.INSTANCE)
                    .registerExact(StringObjectMapSerializer.TYPE_TOKEN, StringObjectMapSerializer.INSTANCE);
            });
    }
}
