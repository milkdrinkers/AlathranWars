package io.github.alathra.alathranwars.conflict.battle.bossbar;

import io.github.alathra.alathranwars.conflict.battle.BattleManager;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Holds three boss bars for three different audiences.
 * Exposes methods for performantly updating the boss bars and audiences.
 */
public class BossBarManager implements BattleManager {
    private final WrappedBossBar attackerBar;
    private final WrappedBossBar defenderBar;
    private final WrappedBossBar spectatorBar;

    public BossBarManager() {
        attackerBar = new WrappedBossBar(BossBar.bossBar(Component.empty(), 0, BossBar.Color.WHITE, BossBar.Overlay.PROGRESS));
        defenderBar = new WrappedBossBar(BossBar.bossBar(Component.empty(), 0, BossBar.Color.WHITE, BossBar.Overlay.PROGRESS));
        spectatorBar = new WrappedBossBar(BossBar.bossBar(Component.empty(), 0, BossBar.Color.WHITE, BossBar.Overlay.PROGRESS));
    }

    public BossBarManager(BossBar bar1, BossBar bar2, BossBar bar3) {
        attackerBar = new WrappedBossBar(bar1);
        defenderBar = new WrappedBossBar(bar2);
        spectatorBar = new WrappedBossBar(bar3);
    }

    public WrappedBossBar getAttackerBar() {
        return attackerBar;
    }

    public WrappedBossBar getDefenderBar() {
        return defenderBar;
    }

    public WrappedBossBar getSpectatorBar() {
        return spectatorBar;
    }

    public void addAttackerBar(Audience viewer) {
        attackerBar.addViewer(viewer);
    }

    public void addDefenderBar(Audience viewer) {
        defenderBar.addViewer(viewer);
    }

    public void addSpectatorBar(Audience viewer) {
        spectatorBar.addViewer(viewer);
    }

    public void removeAttackerBar(Audience viewer) {
        attackerBar.removeViewer(viewer);
    }

    public void removeDefenderBar(Audience viewer) {
        defenderBar.removeViewer(viewer);
    }

    public void removeSpectatorBar(Audience viewer) {
        spectatorBar.removeViewer(viewer);
    }

    @Override
    public void start() {
        // Set default values
        getAttackerBar().name(Component.empty());
        getAttackerBar().progress(0);
        getAttackerBar().color(BossBar.Color.YELLOW);
        getAttackerBar().overlay(BossBar.Overlay.NOTCHED_10);

        getDefenderBar().name(Component.empty());
        getDefenderBar().progress(0);
        getDefenderBar().color(BossBar.Color.YELLOW);
        getDefenderBar().overlay(BossBar.Overlay.NOTCHED_10);

        getSpectatorBar().name(Component.empty());
        getSpectatorBar().progress(0);
        getSpectatorBar().color(BossBar.Color.YELLOW);
        getSpectatorBar().overlay(BossBar.Overlay.NOTCHED_10);
    }

    @Override
    public void stop() {
        // Remove all viewers
        for (Player p : Bukkit.getOnlinePlayers()) {
            removeAttackerBar(p);
            removeDefenderBar(p);
            removeSpectatorBar(p);
        }
    }
}
