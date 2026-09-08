package io.github.alathra.alathranwars.conflict.battle.bossbar;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.ComponentLike;

import java.util.Set;

public class WrappedBossBar {
    private BossBar bar;
    private boolean hasUpdate = false;

    public WrappedBossBar(BossBar bar) {
        this.bar = bar;
    }

    public void addViewer(Audience viewer) {
        bar.addViewer(viewer);
    }

    public void removeViewer(Audience viewer) {
        bar.removeViewer(viewer);
    }

    public void update() {
        if (!hasUpdate)
            return;

        hasUpdate = false;
    }

    public BossBar getBar() {
        return bar;
    }

    public void setBar(BossBar bar) {
        this.bar = bar;
        hasUpdate = true;
    }

    public void color(BossBar.Color color) {
        bar.color(color);
        hasUpdate = true;
    }

    public void overlay(BossBar.Overlay overlay) {
        bar.overlay(overlay);
        hasUpdate = true;
    }

    public void name(ComponentLike name) {
        bar.name(name);
        hasUpdate = true;
    }

    public void progress(float progress) {
        bar.progress(progress);
        hasUpdate = true;
    }

    public void flags(Set<BossBar.Flag> flags) {
        bar.flags(flags);
        hasUpdate = true;
    }
}
