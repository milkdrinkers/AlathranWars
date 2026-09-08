package io.github.alathra.alathranwars.listener.war;

import io.github.alathra.alathranwars.conflict.war.War;
import io.github.alathra.alathranwars.conflict.war.WarController;
import io.github.alathra.alathranwars.conflict.war.side.Side;
import io.github.alathra.alathranwars.hook.NameColorHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {
    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();

        // Handle player logout
        for (War war : WarController.getInstance().getWars(p)) {
            Side side = war.getPlayerSide(p);
            if (side == null)
                continue;

            side.logout(p);
        }

        NameColorHandler.getInstance().removePlayer(p);
    }
}
