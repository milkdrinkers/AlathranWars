package io.github.alathra.alathranwars.deathspectate.event;

import io.github.alathra.alathranwars.deathspectate.task.SpectateTask;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class DeathSpectatingEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final SpectateTask spectateTask;

    public DeathSpectatingEvent(SpectateTask task) {
        this.spectateTask = task;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public SpectateTask getSpectateTask() {
        return spectateTask;
    }
}
