package io.github.alathra.alathranwars.data.cooldown;

import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.metadata.CustomDataField;
import io.github.alathra.alathranwars.api.AlathranWarsAPIImpl;
import io.github.alathra.alathranwars.data.TownyMetaHandler;
import io.github.alathra.alathranwars.data.type.InstantDataField;
import io.github.alathra.alathranwars.utility.Cfg;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

public class CooldownMetaResident {
    private static final InstantDataField LAST_OFFENSIVE_WAR_COOLDOWN = new InstantDataField(TownyMetaHandler.META_NAMESPACE + "resident_last_attacktime");
    private static final InstantDataField LAST_DEFENSIVE_WAR_COOLDOWN = new InstantDataField(TownyMetaHandler.META_NAMESPACE + "resident_last_defensetime");

    private final Duration OFFENSIVE_WAR_COOLDOWN_DURATION;
    private final Duration OFFENSIVE_WAR_COOLDOWN_MERCENARY_DURATION;
    private final Duration DEFENSIVE_WAR_COOLDOWN_DURATION;

    private static CooldownMetaResident INSTANCE = null;

    private CooldownMetaResident() {
        OFFENSIVE_WAR_COOLDOWN_DURATION = Duration.ofDays(Cfg.get().cooldowns.war.resident.offensive.defaultDays);
        OFFENSIVE_WAR_COOLDOWN_MERCENARY_DURATION = Duration.ofDays(Cfg.get().cooldowns.war.resident.offensive.mercenary);
        DEFENSIVE_WAR_COOLDOWN_DURATION = Duration.ofDays(Cfg.get().cooldowns.war.resident.defensive.defaultDays);
    }

    public static CooldownMetaResident getInstance() {
        if (INSTANCE == null)
            INSTANCE = new CooldownMetaResident();
        return INSTANCE;
    }

    public CooldownType getCooldown(Resident resident) {
        if (hasAttackCooldown(resident)) {
            return CooldownType.OFFENSIVE;
        } else if (hasDefenseCooldown(resident)) {
            return CooldownType.DEFENSIVE;
        } else {
            return CooldownType.NONE;
        }
    }

    public boolean hasCooldown(Resident resident) {
        return switch (getCooldown(resident)) {
            case OFFENSIVE, DEFENSIVE -> true;
            case NONE -> false;
        };
    }

    public boolean hasAttackCooldown(Resident resident) {
        final Instant now = Instant.now();

        final Instant cdStartTime = getOffensiveCooldown(resident);
        if (AlathranWarsAPIImpl.getInstance().isMercenary(resident)) {
            return cdStartTime.plus(OFFENSIVE_WAR_COOLDOWN_MERCENARY_DURATION).isAfter(now);
        } else {
            return cdStartTime.plus(OFFENSIVE_WAR_COOLDOWN_DURATION).isAfter(now);
        }
    }

    public boolean hasDefenseCooldown(Resident resident) {
        final Instant now = Instant.now();

        final Instant cdStartTime = getDefensiveCooldown(resident);
        return cdStartTime.plus(DEFENSIVE_WAR_COOLDOWN_DURATION).isAfter(now);
    }

    public Instant getOffensiveCooldown(Resident resident) {
        return Optional.ofNullable(resident.getMetadata(getLastOffensiveWarCooldown().getKey(), InstantDataField.class)).map(CustomDataField::getValue).orElse(Instant.EPOCH);
    }

    public Instant getDefensiveCooldown(Resident resident) {
        return Optional.ofNullable(resident.getMetadata(getLastDefensiveWarCooldown().getKey(), InstantDataField.class)).map(CustomDataField::getValue).orElse(Instant.EPOCH);
    }

    public Instant getOffensiveCooldownEnd(Resident resident) {
        if (AlathranWarsAPIImpl.getInstance().isMercenary(resident)) {
            return getOffensiveCooldown(resident).plus(OFFENSIVE_WAR_COOLDOWN_MERCENARY_DURATION);
        } else {
            return getOffensiveCooldown(resident).plus(OFFENSIVE_WAR_COOLDOWN_DURATION);
        }
    }

    public Instant getDefensiveCooldownEnd(Resident resident) {
        if (AlathranWarsAPIImpl.getInstance().isMercenary(resident)) {
            return getDefensiveCooldown(resident).plus(DEFENSIVE_WAR_COOLDOWN_DURATION);
        } else {
            return getDefensiveCooldown(resident).plus(DEFENSIVE_WAR_COOLDOWN_DURATION);
        }
    }

    public void setOffensiveCooldown(Resident resident) {
        if (resident.getMetadata(getLastOffensiveWarCooldown().getKey(), InstantDataField.class) != null) {
            Objects.requireNonNull(resident.getMetadata(getLastOffensiveWarCooldown().getKey(), InstantDataField.class)).setValue(Instant.now());
            resident.save();
        } else {
            resident.addMetaData(new InstantDataField(getLastOffensiveWarCooldown().getKey()), true);
        }
    }

    public void setDefensiveCooldown(Resident resident) {
        if (resident.getMetadata(getLastDefensiveWarCooldown().getKey(), InstantDataField.class) != null) {
            Objects.requireNonNull(resident.getMetadata(getLastDefensiveWarCooldown().getKey(), InstantDataField.class)).setValue(Instant.now());
            resident.save();
        } else {
            resident.addMetaData(new InstantDataField(getLastDefensiveWarCooldown().getKey()), true);
        }
    }

    public InstantDataField getLastOffensiveWarCooldown() {
        return LAST_OFFENSIVE_WAR_COOLDOWN;
    }

    public InstantDataField getLastDefensiveWarCooldown() {
        return LAST_DEFENSIVE_WAR_COOLDOWN;
    }

    public enum CooldownType {
        OFFENSIVE,
        DEFENSIVE,
        NONE
    }
}
