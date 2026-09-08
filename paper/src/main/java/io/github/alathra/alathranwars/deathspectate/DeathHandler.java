package io.github.alathra.alathranwars.deathspectate;

import io.github.alathra.alathranwars.AbstractAlathranWars;
import io.github.alathra.alathranwars.Reloadable;
import io.github.alathra.alathranwars.deathspectate.listener.AbuseListener;
import io.github.alathra.alathranwars.deathspectate.listener.DeathListener;
import io.github.alathra.alathranwars.deathspectate.listener.JoinListener;
import io.github.alathra.alathranwars.deathspectate.listener.RespawnListener;
import org.bukkit.Bukkit;

public class DeathHandler implements Reloadable {
    /**
     * On plugin enable.
     */
    @Override
    public void onEnable(AbstractAlathranWars plugin) {
        Bukkit.getPluginManager().registerEvents(new AbuseListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new DeathListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new RespawnListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new JoinListener(), plugin);
    }
}
