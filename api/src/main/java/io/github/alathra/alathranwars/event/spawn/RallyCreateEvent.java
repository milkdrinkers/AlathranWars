package io.github.alathra.alathranwars.event.spawn;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class RallyCreateEvent extends Event { // TODO Fire
    private static final HandlerList HANDLER_LIST = new HandlerList();

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
