package io.github.alathra.alathranwars.api;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Government;
import com.palmergames.bukkit.towny.object.Resident;
import io.github.alathra.alathranwars.data.cooldown.CooldownMetaGovernment;
import io.github.alathra.alathranwars.data.cooldown.CooldownMetaResident;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.time.Instant;

public interface WarCooldownAPI {
    default CooldownMetaGovernment.CooldownType getCooldown(Government government) {
        return CooldownMetaGovernment.getInstance().getCooldown(government);
    }

    default boolean hasCooldown(Government government) {
        return CooldownMetaGovernment.getInstance().hasCooldown(government);
    }

    default boolean hasAttackCooldown(Government government) {
        return CooldownMetaGovernment.getInstance().hasAttackCooldown(government);
    }

    default Duration getAttackCooldown(Government government) {
        return Duration.between(Instant.now(), CooldownMetaGovernment.getInstance().getOffensiveCooldownEnd(government)).abs();
    }

    default void setAttackCooldown(Government government) {
        CooldownMetaGovernment.getInstance().setOffensiveCooldown(government);
    }

    default boolean hasDefenseCooldown(Government government) {
        return CooldownMetaGovernment.getInstance().hasDefenseCooldown(government);
    }

    default Duration getDefenseCooldown(Government government) {
        return Duration.between(Instant.now(), CooldownMetaGovernment.getInstance().getDefensiveCooldownEnd(government)).abs();
    }

    default void setDefenseCooldown(Government government) {
        CooldownMetaGovernment.getInstance().setDefensiveCooldown(government);
    }

    default CooldownMetaResident.CooldownType getCooldown(Player player) {
        final Resident resident = TownyAPI.getInstance().getResident(player);
        if (resident == null)
            return CooldownMetaResident.CooldownType.NONE;
        return getCooldown(resident);
    }

    default CooldownMetaResident.CooldownType getCooldown(Resident resident) {
        return CooldownMetaResident.getInstance().getCooldown(resident);
    }

    default boolean hasCooldown(Player player) {
        final Resident resident = TownyAPI.getInstance().getResident(player);
        if (resident == null)
            return false;
        return hasCooldown(resident);
    }

    default boolean hasCooldown(Resident resident) {
        return CooldownMetaResident.getInstance().hasCooldown(resident);
    }

    default boolean hasAttackCooldown(Player player) {
        final Resident resident = TownyAPI.getInstance().getResident(player);
        if (resident == null)
            return false;
        return hasAttackCooldown(resident);
    }

    default boolean hasAttackCooldown(Resident resident) {
        return CooldownMetaResident.getInstance().hasAttackCooldown(resident);
    }

    default boolean hasDefenseCooldown(Player player) {
        final Resident resident = TownyAPI.getInstance().getResident(player);
        if (resident == null)
            return false;
        return hasDefenseCooldown(resident);
    }

    default boolean hasDefenseCooldown(Resident resident) {
        return CooldownMetaResident.getInstance().hasDefenseCooldown(resident);
    }

    default Duration getAttackCooldown(Player player) {
        final Resident resident = TownyAPI.getInstance().getResident(player);
        if (resident == null || !hasAttackCooldown(resident))
            return Duration.ZERO;
        return getAttackCooldown(resident);
    }

    default Duration getAttackCooldown(Resident resident) {
        return Duration.between(Instant.now(), CooldownMetaResident.getInstance().getOffensiveCooldownEnd(resident)).abs();
    }

    default Duration getDefenseCooldown(Player player) {
        final Resident resident = TownyAPI.getInstance().getResident(player);
        if (resident == null || !hasDefenseCooldown(resident))
            return Duration.ZERO;
        return getDefenseCooldown(resident);
    }

    default Duration getDefenseCooldown(Resident resident) {
        return Duration.between(Instant.now(), CooldownMetaResident.getInstance().getDefensiveCooldownEnd(resident)).abs();
    }

    default void setAttackCooldown(Player player) {
        final Resident resident = TownyAPI.getInstance().getResident(player);
        if (resident == null)
            return;
        setAttackCooldown(resident);
    }

    default void setAttackCooldown(Resident resident) {
        CooldownMetaResident.getInstance().setOffensiveCooldown(resident);
    }

    default void setDefenseCooldown(Player player) {
        final Resident resident = TownyAPI.getInstance().getResident(player);
        if (resident == null)
            return;
        setDefenseCooldown(resident);
    }

    default void setDefenseCooldown(Resident resident) {
        CooldownMetaResident.getInstance().setDefensiveCooldown(resident);
    }
}
