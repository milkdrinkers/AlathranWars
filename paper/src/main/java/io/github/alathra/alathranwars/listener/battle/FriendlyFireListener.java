package io.github.alathra.alathranwars.listener.battle;

import io.github.alathra.alathranwars.conflict.war.WarController;
import io.github.alathra.alathranwars.utility.SideUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class FriendlyFireListener implements Listener {
    /**
     * Disable friendly fire
     *
     * @param e the e
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageByEntityEvent e) {
        final Entity attacker = e.getDamager();
        final Entity target = e.getEntity();

        if (!(attacker instanceof Player p))
            return;

        if (!(target instanceof Player p2))
            return;

        if (!WarController.getInstance().isInActiveWar(p))
            return;

        if (!WarController.getInstance().isInActiveWar(p2))
            return;

        final boolean sharesSide = SideUtils.getPlayerSides(p).stream()
            .anyMatch(s -> SideUtils.getPlayerSides(p2).contains(s));

        if (sharesSide)
            e.setCancelled(true);
    }
}
