package io.github.alathra.alathranwars.event.battle;

import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PreSetControlPointEvent extends Event implements Cancellable {
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private boolean cancelled;
    private final Town town;
    private final @Nullable Location oldLocation;
    private Location newLocation;
    private final @Nullable Player player;

    public PreSetControlPointEvent(Town town, @Nullable Location oldLocation, Location newLocation, @Nullable Player player) {
        this.town = town;
        this.oldLocation = oldLocation;
        this.newLocation = newLocation;
        this.player = player;
    }

    /**
     * Gets the cancellation state of this event. A cancelled event will not
     * be executed in the server, but will still pass to other plugins
     *
     * @return true if this event is cancelled
     */
    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    /**
     * Sets the cancellation state of this event. A cancelled event will not
     * be executed in the server, but will still pass to other plugins.
     *
     * @param cancelled true if you wish to cancel this event
     */
    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    public Town getTown() {
        return town;
    }

    public @Nullable Location getOldLocation() {
        return oldLocation;
    }

    public Location getNewLocation() {
        return newLocation;
    }

    public @Nullable Player getPlayer() {
        return player;
    }

    public void setNewLocation(Location newLocation) {
        this.newLocation = newLocation;
    }
}
