package io.github.alathra.alathranwars.conflict.war;

import com.palmergames.bukkit.towny.object.Town;
import io.github.alathra.alathranwars.data.ControlPoint;
import io.github.alathra.alathranwars.event.battle.SetControlPointEvent;
import io.github.alathra.alathranwars.hook.Hook;
import io.github.alathra.alathranwars.packet.CustomLaser;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WarCapturePointManager implements Listener {
    private final WarImpl war;
    private final Map<Town, CustomLaser> lasers = new ConcurrentHashMap<>();

    public WarCapturePointManager(WarImpl war) {
        this.war = war;
    }

    public void start() {
        if (!Hook.PacketEvents.isLoaded())
            return;

        for (Town town : Stream.concat(war.getAttacker().getTowns().stream(), war.getDefender().getTowns().stream()).collect(Collectors.toUnmodifiableSet())) {
            final Location cp = ControlPoint.get(town);

            lasers.put(town, CustomLaser.of(
                CustomLaser.getLaserFromLocation(cp),
                CustomLaser.getLaserToLocation(cp)
            ));
        }

        for (CustomLaser laser : lasers.values()) {
            laser.spawn();
            Bukkit.getOnlinePlayers().stream()
                .map(OfflinePlayer::getPlayer)
                .filter(Objects::nonNull)
                .forEach(laser::addViewer);
        }
    }

    public void stop() {
        if (!Hook.PacketEvents.isLoaded())
            return;

        for (CustomLaser laser : lasers.values()) {
            laser.despawn();
        }
        lasers.clear();
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onPlayerJoin(PlayerJoinEvent e) {
        if (!Hook.PacketEvents.isLoaded())
            return;

        lasers
            .values()
            .forEach(l -> l.addViewer(e.getPlayer()));
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onPlayerLeave(PlayerQuitEvent e) {
        if (!Hook.PacketEvents.isLoaded())
            return;

        lasers
            .values()
            .forEach(l -> l.removeViewer(e.getPlayer()));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    @SuppressWarnings("unused")
    public void onMoveControlPoint(SetControlPointEvent e) {
        if (!Hook.PacketEvents.isLoaded())
            return;

        final CustomLaser laser = lasers.get(e.getTown());
        if (laser == null)
            return;

        if (e.getOldLocation() == null || e.getNewLocation() == null)
            return;

        if (e.getOldLocation().getWorld() != e.getNewLocation().getWorld())
            return;

        laser.move(
            CustomLaser.getLaserFromLocation(e.getNewLocation()),
            CustomLaser.getLaserToLocation(e.getNewLocation())
        );
    }
}
