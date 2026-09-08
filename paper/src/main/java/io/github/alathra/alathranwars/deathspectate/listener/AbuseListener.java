package io.github.alathra.alathranwars.deathspectate.listener;

import com.destroystokyo.paper.event.player.PlayerStartSpectatingEntityEvent;
import com.destroystokyo.paper.event.player.PlayerStopSpectatingEntityEvent;
import io.github.alathra.alathranwars.AlathranWars;
import io.github.alathra.alathranwars.conflict.war.WarController;
import io.github.alathra.alathranwars.deathspectate.DeathConfig;
import io.github.alathra.alathranwars.deathspectate.DeathUtil;
import io.github.alathra.alathranwars.utility.Cfg;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.*;
import org.bukkit.persistence.PersistentDataType;

import static io.github.alathra.alathranwars.deathspectate.DeathUtil.PDC_TELEPORTING;

public class AbuseListener implements Listener {
    private final AlathranWars plugin;

    public AbuseListener() {
        this.plugin = AlathranWars.getInstance();
    }

    /**
     * Prevent other plugins from teleporting a dead player
     *
     * @param e event
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    @SuppressWarnings("unused")
    void onPlayerTryToTeleportWhenDead(PlayerTeleportEvent e) {
        final Player p = e.getPlayer();

        if (!DeathUtil.isSpectating(p))
            return;

        // Only allow us to teleport players while dead
        if (e.getCause().equals(PlayerTeleportEvent.TeleportCause.PLUGIN) &&
            p.getPersistentDataContainer().has(PDC_TELEPORTING, PersistentDataType.BOOLEAN)) {
            p.getPersistentDataContainer().remove(PDC_TELEPORTING);
            return;
        }

        e.setCancelled(true);
    }

    /**
     * Prevent player sneaking while spectating (Fixes issues with ability plugins like MCMMO & AuraSkills, Eco etc)
     *
     * @param e event
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    @SuppressWarnings("unused")
    void onPlayerTryToSneakWhenDead(PlayerToggleSneakEvent e) {
        if (DeathUtil.isSpectating(e.getPlayer()) && e.isSneaking())
            e.setCancelled(true);
    }

    /**
     * Prevent players from running commands while dead
     *
     * @param e event
     */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    @SuppressWarnings("unused")
    void onPlayerTryToRunCommandWhenDead(PlayerCommandPreprocessEvent e) {
        if (DeathUtil.isSpectating(e.getPlayer()) && !e.getPlayer().hasPermission("alathranwars.death.commands")) {
            String cmd = e.getMessage().split(" ")[0]; // Got a more efficient/better way? Let me know/PR it!
            cmd = cmd.substring(1); // Remove slash
            if (!Cfg.get().respawns.whitelistedCommands.contains(cmd)) {
                e.setCancelled(true);
                if (!DeathConfig.getCommandDeniedMessage().isEmpty())
                    e.getPlayer().sendMessage(DeathConfig.getCommandDeniedMessage());
            }
        }
    }

    /**
     * Dead players do not persist, instantly respawn a dead player that leaves
     *
     * @param e event
     */
    @EventHandler(priority = EventPriority.LOW)
    @SuppressWarnings("unused")
    void onPlayerQuitWhileSpectatingOrDead(PlayerQuitEvent e) {
        if (!WarController.getInstance().isInActiveWar(e.getPlayer()))
            DeathUtil.respawnPlayerVanilla(e.getPlayer());
    }

    /**
     * Prevent spectators from taking damage (from the void for example)
     *
     * @param e event
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    @SuppressWarnings("unused")
    void onPlayerTakeDamageWhileSpectating(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player p))
            return;

        if (DeathUtil.isSpectating(p))
            e.setCancelled(true);
    }

    /**
     * Prevent healing while spectating
     *
     * @param e event
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    @SuppressWarnings("unused")
    void onPlayerRegainingHealthWhileSpectating(EntityRegainHealthEvent e) {
        if (!(e.getEntity() instanceof Player p))
            return;

        if (DeathUtil.isSpectating(p))
            e.setCancelled(true);
    }

    /**
     * Prevent triggering animations while dead
     *
     * @param e event
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    @SuppressWarnings("unused")
    private void onPlayerSwingHand(PlayerAnimationEvent e) {
        if (DeathUtil.isSpectating(e.getPlayer()))
            e.setCancelled(true);
    }

    /**
     * Prevent player interactions while dead
     *
     * @param e event
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    @SuppressWarnings("unused")
    private void onPlayerTryToInteractWhenDead(PlayerInteractEvent e) {
        if (DeathUtil.isSpectating(e.getPlayer()))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    @SuppressWarnings("unused")
    private void onPlayerTryToInteractWhenDead(PlayerStartSpectatingEntityEvent e) {
        if (DeathUtil.isSpectating(e.getPlayer()))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    @SuppressWarnings("unused")
    private void onPlayerTryToInteractWhenDead(PlayerStopSpectatingEntityEvent e) {
        if (DeathUtil.isSpectating(e.getPlayer()))
            e.setCancelled(true);
    }

    /**
     * Prevent picking up items while dead
     *
     * @param e event
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    @SuppressWarnings("unused")
    private void onPlayerPickupWhenDead(EntityPickupItemEvent e) {
        if (!(e.getEntity() instanceof Player p))
            return;

        if (DeathUtil.isSpectating(p))
            e.setCancelled(true);
    }
}
