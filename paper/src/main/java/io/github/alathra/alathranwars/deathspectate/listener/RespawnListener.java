package io.github.alathra.alathranwars.deathspectate.listener;

import io.github.alathra.alathranwars.AlathranWars;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Objects;

import static io.github.alathra.alathranwars.deathspectate.DeathUtil.PDC_AWAITING_RESPAWN;
import static io.github.alathra.alathranwars.deathspectate.DeathUtil.PDC_IS_DEAD;

public class RespawnListener implements Listener {
    private final AlathranWars plugin;

    public RespawnListener() {
        this.plugin = AlathranWars.getInstance();
    }

    /**
     * Handle cleaning up metadata for API
     *
     * @param e event
     */
    @EventHandler(priority = EventPriority.LOWEST)
    @SuppressWarnings("unused")
    void onPlayerRespawns(PlayerRespawnEvent e) {
        final Player p = e.getPlayer();
        final PersistentDataContainer pdc = p.getPersistentDataContainer();

        Objects.requireNonNull(PDC_IS_DEAD, "Is Dead is null");

        // Check if already death spectating or being respawned
        if (!pdc.has(PDC_IS_DEAD, PersistentDataType.BOOLEAN) || pdc.has(PDC_AWAITING_RESPAWN, PersistentDataType.BOOLEAN))
            return;

        // Cleanup metadata
        pdc.remove(PDC_IS_DEAD);
    }
}
