package io.github.alathra.alathranwars.listener.war;

import com.palmergames.bukkit.towny.event.NationSpawnEvent;
import com.palmergames.bukkit.towny.event.TownSpawnEvent;
import com.palmergames.bukkit.towny.event.teleport.ResidentSpawnEvent;
import com.palmergames.bukkit.towny.object.Town;
import io.github.alathra.alathranwars.conflict.war.WarController;
import io.github.alathra.alathranwars.utility.Cfg;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

public class PlayerTeleportListener implements Listener {
    @EventHandler
    public void onResidentSpawn(@NotNull ResidentSpawnEvent e) {
        Player p = e.getPlayer();

        if (WarController.getInstance().isInActiveWar(p)) {
            p.showTitle(
                Title.title(
                    Component.translatable("alathranwars.war.banner"),
                    Component.translatable("alathranwars.teleport.war-time"),
                    Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(3500), Duration.ofMillis(500))
                )
            );
            e.setCancelled(true);
            e.setCancelMessage("");
        }
    }

    @EventHandler
    public void onTownSpawn(@NotNull TownSpawnEvent e) {
        Player p = e.getPlayer();

        if (!WarController.getInstance().isInActiveWar(p))
            return;

        if (!isInSpawn(p)) {
            p.showTitle(
                Title.title(
                    Component.translatable("alathranwars.war.banner"),
                    Component.translatable("alathranwars.teleport.war-time"),
                    Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(3500), Duration.ofMillis(500))
                )
            );
            e.setCancelled(true);
            e.setCancelMessage("");
            return;
        }

        Town town = e.getToTown();

        if (WarController.getInstance().isInAnySieges(town)) {
            p.showTitle(
                Title.title(
                    Component.translatable("alathranwars.war.banner"),
                    Component.translatable("alathranwars.teleport.besieged-town"),
                    Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(5500), Duration.ofMillis(500))
                )
            );
            e.setCancelled(true);
            e.setCancelMessage("");
        }
    }

    @EventHandler
    public void onNationSpawn(@NotNull NationSpawnEvent e) {
        Player p = e.getPlayer();

        if (!WarController.getInstance().isInActiveWar(p))
            return;

        if (!isInSpawn(p)) {
            p.showTitle(
                Title.title(
                    Component.translatable("alathranwars.war.banner"),
                    Component.translatable("alathranwars.teleport.war-time"),
                    Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(3500), Duration.ofMillis(500))
                )
            );
            e.setCancelled(true);
            e.setCancelMessage("");
            return;
        }

        Town town = e.getToNation().getCapital();

        if (WarController.getInstance().isInAnySieges(town)) {
            p.showTitle(
                Title.title(
                    Component.translatable("alathranwars.war.banner"),
                    Component.translatable("alathranwars.teleport.besieged-town"),
                    Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(5500), Duration.ofMillis(500))
                )
            );
            e.setCancelled(true);
            e.setCancelMessage("");
        }
    }

    private boolean isInSpawn(Player p) {
        return Cfg.get().battles.teleportWorldWhitelist
            .stream()
            .anyMatch(name -> p.getWorld().getName().equals(name));
    }
}
