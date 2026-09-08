package io.github.alathra.alathranwars.event.spawn;

import io.github.alathra.alathranwars.conflict.war.side.spawn.RallyPoint;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class RallyPlaceEvent extends Event {
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final RallyPoint rallyPoint;

    public RallyPlaceEvent(RallyPoint rallyPoint) {
        this.rallyPoint = rallyPoint;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    public RallyPoint getRallyPoint() {
        return rallyPoint;
    }
}
