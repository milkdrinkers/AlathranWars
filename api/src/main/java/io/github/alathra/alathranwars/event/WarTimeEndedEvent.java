package io.github.alathra.alathranwars.event;

import io.github.alathra.alathranwars.conflict.war.War;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Triggered when war time ends for a war.
 */
public class WarTimeEndedEvent extends Event {
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final War war;

    public WarTimeEndedEvent(War war) {
        this.war = war;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    public War getWar() {
        return war;
    }
}
