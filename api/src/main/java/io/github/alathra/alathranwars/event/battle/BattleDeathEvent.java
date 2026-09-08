package io.github.alathra.alathranwars.event.battle;

import io.github.alathra.alathranwars.conflict.battle.Battle;
import io.github.alathra.alathranwars.conflict.war.War;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BattleDeathEvent extends Event {
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final PlayerDeathEvent deathEvent;
    private final @Nullable War war;
    private final @Nullable Battle battle;

    public BattleDeathEvent(PlayerDeathEvent deathEvent, @Nullable Battle battle) {
        this.deathEvent = deathEvent;
        this.war = battle != null ? battle.getWar() : null;
        this.battle = battle;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    public PlayerDeathEvent getDeathEvent() {
        return deathEvent;
    }

    public @Nullable War getWar() {
        return war;
    }

    public @Nullable Battle getBattle() {
        return battle;
    }
}
