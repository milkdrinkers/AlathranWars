package io.github.alathra.alathranwars.api;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Resident;
import io.github.alathra.alathranwars.data.death.DeathMeta;
import org.bukkit.entity.Player;

public interface DeathAPI { // TODO Replace with integration in custom spectator system
    default boolean isDead(Player player) {
        final Resident resident = TownyAPI.getInstance().getResident(player);
        if (resident == null)
            return false;
        return isDead(resident);
    }

    default boolean isDead(Resident resident) {
        return DeathMeta.getInstance().isDead(resident);
    }

    default void setDead(Player player, boolean state) {
        final Resident resident = TownyAPI.getInstance().getResident(player);
        if (resident == null)
            return;
        setDead(resident, state);
    }

    default void setDead(Resident resident, boolean state) {
        DeathMeta.getInstance().setDead(resident, state);
    }
}
