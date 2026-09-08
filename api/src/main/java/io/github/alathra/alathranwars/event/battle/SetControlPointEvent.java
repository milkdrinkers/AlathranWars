package io.github.alathra.alathranwars.event.battle;

import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SetControlPointEvent extends Event {
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final Town town;
    private final @Nullable Location oldLocation;
    private final Location newLocation;
    private final @Nullable Player player;

    public SetControlPointEvent(Town town, @Nullable Location oldLocation, Location newLocation, @Nullable Player player) {
        this.town = town;
        this.oldLocation = oldLocation;
        this.newLocation = newLocation;
        this.player = player;
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
}
