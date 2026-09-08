package io.github.alathra.alathranwars.deathspectate.listener;

import io.github.alathra.alathranwars.deathspectate.DeathConfig;
import io.github.alathra.alathranwars.deathspectate.DeathUtil;
import io.github.alathra.alathranwars.event.battle.BattleDeathEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class DeathListener implements Listener {
    public DeathListener() {
    }

    /**
     * Start death spectating on player death
     *
     * @param e event
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    @SuppressWarnings("unused")
    private void onPlayerDies(BattleDeathEvent e) {
        // Check for citizens npc's
        if (e.getDeathEvent().getEntity().hasMetadata("NPC"))
            return;

        final Player p = e.getDeathEvent().getPlayer();

        if (DeathUtil.isSpectating(p)) {
            e.getDeathEvent().setCancelled(true);
            return;
        }

        if (p.getLastDamageCause() == null)
            return;

        if (!DeathConfig.canSpectate(p, p.getLastDamageCause().getCause()))
            return;

        if (DeathUtil.startDeathSpectating(p, e.getDeathEvent()))
            e.getDeathEvent().setCancelled(true);
    }
}
