package io.github.alathra.alathranwars.event.battle;

import io.github.alathra.alathranwars.conflict.battle.Battle;
import io.github.alathra.alathranwars.conflict.war.War;
import io.github.alathra.alathranwars.enums.battle.BattleType;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PreBattleStartEvent extends BattleStartEvent implements Cancellable {
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private boolean cancelled;

    public PreBattleStartEvent(War war, Battle battle, BattleType battleType) {
        super(war, battle, battleType);
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

    public War getWar() {
        return super.getWar();
    }

    public Battle getBattle() {
        return super.getBattle();
    }

    public BattleType getBattleType() {
        return super.getBattleType();
    }
}
