package io.github.alathra.alathranwars.listener;

import io.github.alathra.alathranwars.AbstractAlathranWars;
import io.github.alathra.alathranwars.AlathranWars;
import io.github.alathra.alathranwars.Reloadable;
import io.github.alathra.alathranwars.listener.battle.FriendlyFireListener;
import io.github.alathra.alathranwars.listener.battle.PlayerDeathListener;
import io.github.alathra.alathranwars.listener.battle.siege.*;
import io.github.alathra.alathranwars.listener.rallypoint.RallyBuildListener;
import io.github.alathra.alathranwars.listener.war.*;

/**
 * A class to handle registration of event listeners.
 */
public class ListenerHandler implements Reloadable {
    private final AlathranWars plugin;

    /**
     * Instantiates a the Listener handler.
     *
     * @param plugin the plugin instance
     */
    public ListenerHandler(AlathranWars plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onEnable(AbstractAlathranWars plugin) {
        // Sieges
        plugin.getServer().getPluginManager().registerEvents(new BlockBreakPlaceListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PlayerDeathListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new ItemUseListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PlayerDamageEntityListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PlayerTriggerSiegeListener(), plugin);

        // Battles
        plugin.getServer().getPluginManager().registerEvents(new SiegeListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new RallyBuildListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new FriendlyFireListener(), plugin);

        // Wars
        plugin.getServer().getPluginManager().registerEvents(new StatusScreenListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new NationListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new TownListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PlayerJoinListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PlayerQuitListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PlayerTeleportListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new WarListener(), plugin);
    }
}