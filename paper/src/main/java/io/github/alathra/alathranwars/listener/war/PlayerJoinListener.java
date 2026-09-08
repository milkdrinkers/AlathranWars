package io.github.alathra.alathranwars.listener.war;

import io.github.alathra.alathranwars.conflict.war.War;
import io.github.alathra.alathranwars.conflict.war.WarController;
import io.github.alathra.alathranwars.conflict.war.side.Side;
import io.github.alathra.alathranwars.hook.NameColorHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;

public class PlayerJoinListener implements Listener {
    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();

        // Handle player login
        for (War war : WarController.getInstance().getWars(p)) {
            Side side = war.getPlayerSide(p);
            if (side == null)
                continue;

            side.login(p);
        }

        NameColorHandler.getInstance().calculatePlayerColors(p);

        if (WarController.getInstance().isInAnyWars(p) && !isEventWar(WarController.getInstance().getWars(p))) {
            p.showTitle(
                Title.title(
                    Component.translatable("alathranwars.war.banner"),
                    Component.translatable("alathranwars.war.in-war-subtitle"),
                    Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(3500), Duration.ofMillis(500))
                )
            );
        }
    }

    /**
     * Check if the only war the player is in is a event war
     *
     * @param wars wars
     * @return boolean
     */
    private boolean isEventWar(Set<War> wars) {
        if (wars.size() != 1)
            return false;

        Optional<War> war = wars.stream().findAny();

        return war.get().isEventWar();
    }
}
