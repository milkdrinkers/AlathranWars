package io.github.alathra.alathranwars.data.cooldown;

import com.palmergames.bukkit.towny.object.Government;
import com.palmergames.bukkit.towny.object.metadata.CustomDataField;
import io.github.alathra.alathranwars.data.TownyMetaHandler;
import io.github.alathra.alathranwars.data.mercenary.MercenaryMeta;
import io.github.alathra.alathranwars.data.type.InstantDataField;
import io.github.alathra.alathranwars.utility.Cfg;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

public class CooldownMetaGovernment {
    private static final InstantDataField LAST_OFFENSIVE_WAR_COOLDOWN = new InstantDataField(TownyMetaHandler.META_NAMESPACE + "government_last_attacktime");
    private static final InstantDataField LAST_DEFENSIVE_WAR_COOLDOWN = new InstantDataField(TownyMetaHandler.META_NAMESPACE + "government_last_defensetime");

    private final Duration OFFENSIVE_WAR_COOLDOWN_DURATION;
    private final Duration OFFENSIVE_WAR_COOLDOWN_MERCENARY_DURATION;
    private final Duration DEFENSIVE_WAR_COOLDOWN_DURATION;

    private static CooldownMetaGovernment INSTANCE = null;

    private CooldownMetaGovernment() {
        OFFENSIVE_WAR_COOLDOWN_DURATION = Duration.ofDays(Cfg.get().cooldowns.war.government.offensive.defaultDays);
        OFFENSIVE_WAR_COOLDOWN_MERCENARY_DURATION = Duration.ofDays(Cfg.get().cooldowns.war.government.offensive.mercenary);
        DEFENSIVE_WAR_COOLDOWN_DURATION = Duration.ofDays(Cfg.get().cooldowns.war.government.defensive.defaultDays);
    }

    public static CooldownMetaGovernment getInstance() {
        if (INSTANCE == null)
            INSTANCE = new CooldownMetaGovernment();
        return INSTANCE;
    }

    public CooldownType getCooldown(Government government) {
        if (hasAttackCooldown(government)) {
            return CooldownType.OFFENSIVE;
        } else if (hasDefenseCooldown(government)) {
            return CooldownType.DEFENSIVE;
        } else {
            return CooldownType.NONE;
        }
    }

    public boolean hasCooldown(Government government) {
        return switch (getCooldown(government)) {
            case OFFENSIVE, DEFENSIVE -> true;
            case NONE -> false;
        };
    }

    public boolean hasAttackCooldown(Government government) {
        final Instant now = Instant.now();

        final Instant cdStartTime = getOffensiveCooldown(government);
        if (MercenaryMeta.isMercenary(government)) {
            return cdStartTime.plus(OFFENSIVE_WAR_COOLDOWN_MERCENARY_DURATION).isAfter(now);
        } else {
            return cdStartTime.plus(OFFENSIVE_WAR_COOLDOWN_DURATION).isAfter(now);
        }
    }

    public boolean hasDefenseCooldown(Government government) {
        final Instant now = Instant.now();

        final Instant cdStartTime = getDefensiveCooldown(government);
        return cdStartTime.plus(DEFENSIVE_WAR_COOLDOWN_DURATION).isAfter(now);
    }

    public Instant getOffensiveCooldown(Government government) {
        return Optional.ofNullable(government.getMetadata(getLastOffensiveWarCooldown().getKey(), InstantDataField.class)).map(CustomDataField::getValue).orElse(Instant.EPOCH);
    }

    public Instant getDefensiveCooldown(Government government) {
        return Optional.ofNullable(government.getMetadata(getLastDefensiveWarCooldown().getKey(), InstantDataField.class)).map(CustomDataField::getValue).orElse(Instant.EPOCH);
    }

    public Instant getOffensiveCooldownEnd(Government government) {
        if (MercenaryMeta.isMercenary(government)) {
            return getOffensiveCooldown(government).plus(OFFENSIVE_WAR_COOLDOWN_MERCENARY_DURATION);
        } else {
            return getOffensiveCooldown(government).plus(OFFENSIVE_WAR_COOLDOWN_DURATION);
        }
    }

    public Instant getDefensiveCooldownEnd(Government government) {
        if (MercenaryMeta.isMercenary(government)) {
            return getDefensiveCooldown(government).plus(DEFENSIVE_WAR_COOLDOWN_DURATION);
        } else {
            return getDefensiveCooldown(government).plus(DEFENSIVE_WAR_COOLDOWN_DURATION);
        }
    }

    public void setOffensiveCooldown(Government government) {
        if (government.getMetadata(getLastOffensiveWarCooldown().getKey(), InstantDataField.class) != null) {
            Objects.requireNonNull(government.getMetadata(getLastOffensiveWarCooldown().getKey(), InstantDataField.class)).setValue(Instant.now());
            government.save();
        } else {
            government.addMetaData(new InstantDataField(getLastOffensiveWarCooldown().getKey()), true);
        }
    }

    public void setDefensiveCooldown(Government government) {
        if (government.getMetadata(getLastDefensiveWarCooldown().getKey(), InstantDataField.class) != null) {
            Objects.requireNonNull(government.getMetadata(getLastDefensiveWarCooldown().getKey(), InstantDataField.class)).setValue(Instant.now());
            government.save();
        } else {
            government.addMetaData(new InstantDataField(getLastDefensiveWarCooldown().getKey()), true);
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
