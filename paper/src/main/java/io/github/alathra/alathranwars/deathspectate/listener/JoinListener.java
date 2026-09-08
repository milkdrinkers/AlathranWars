package io.github.alathra.alathranwars.deathspectate.listener;

import io.github.alathra.alathranwars.conflict.war.WarController;
import io.github.alathra.alathranwars.deathspectate.DeathUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) { // Persist dead players and their state
        final Player p = e.getPlayer();

        if (!WarController.getInstance().isInActiveWar(p))
            return;

        if (DeathUtil.isSpectating(p))
            DeathUtil.resumeDeathSpectating(p);
    }
}
