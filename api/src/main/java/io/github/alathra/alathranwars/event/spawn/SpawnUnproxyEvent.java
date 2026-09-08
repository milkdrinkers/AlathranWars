package io.github.alathra.alathranwars.event.spawn;

import io.github.alathra.alathranwars.conflict.war.War;
import io.github.alathra.alathranwars.conflict.war.side.spawn.Spawn;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class SpawnUnproxyEvent extends Event {
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final War war;
    private final Spawn spawn;

    public SpawnUnproxyEvent(War war, Spawn spawn) {
        this.war = war;
        this.spawn = spawn;
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

    public Spawn getSpawn() {
        return spawn;
    }
}
