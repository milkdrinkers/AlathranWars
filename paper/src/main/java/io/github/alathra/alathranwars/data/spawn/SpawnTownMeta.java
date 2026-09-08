package io.github.alathra.alathranwars.data.spawn;

import com.palmergames.bukkit.towny.object.Government;
import com.palmergames.bukkit.towny.object.metadata.BooleanDataField;
import com.palmergames.bukkit.towny.object.metadata.CustomDataField;
import io.github.alathra.alathranwars.data.TownyMetaHandler;
import io.github.alathra.alathranwars.data.type.InstantDataField;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

public class SpawnTownMeta {
    private static final BooleanDataField IS_PROXIED = new BooleanDataField(TownyMetaHandler.META_NAMESPACE + "spawnpoint_is_proxied", false);
    private static final InstantDataField LAST_PROXIED = new InstantDataField(TownyMetaHandler.META_NAMESPACE + "spawnpoint_last_proxied");
    private static final InstantDataField START_PROXIED = new InstantDataField(TownyMetaHandler.META_NAMESPACE + "spawnpoint_start_proxied");

    public static boolean getIsProxied(Government government) {
        return Optional.ofNullable(government.getMetadata(IS_PROXIED.getKey(), BooleanDataField.class)).map(CustomDataField::getValue).orElse(false);
    }

    @Nullable
    public static Instant getLastProxied(Government government) {
        return Optional.ofNullable(government.getMetadata(LAST_PROXIED.getKey(), InstantDataField.class)).map(CustomDataField::getValue).orElse(null);
    }

    @Nullable
    public static Instant getStartProxied(Government government) {
        return Optional.ofNullable(government.getMetadata(START_PROXIED.getKey(), InstantDataField.class)).map(CustomDataField::getValue).orElse(null);
    }

    public void setIsProxied(Government government, boolean isProxied) {
        if (government.hasMeta(IS_PROXIED.getKey())) {
            Objects.requireNonNull(government.getMetadata(IS_PROXIED.getKey(), BooleanDataField.class)).setValue(isProxied);
        } else {
            government.addMetaData(new BooleanDataField(IS_PROXIED.getKey(), isProxied), true);
        }
    }

    public void setLastProxied(Government government) {
        if (government.hasMeta(LAST_PROXIED.getKey())) {
            Objects.requireNonNull(government.getMetadata(LAST_PROXIED.getKey(), InstantDataField.class)).setValue(Instant.now());
        } else {
            government.addMetaData(new InstantDataField(LAST_PROXIED.getKey()), true);
        }
    }

    public void setStartProxied(Government government) {
        if (government.hasMeta(START_PROXIED.getKey())) {
            Objects.requireNonNull(government.getMetadata(START_PROXIED.getKey(), InstantDataField.class)).setValue(Instant.now());
        } else {
            government.addMetaData(new InstantDataField(START_PROXIED.getKey()), true);
        }
    }

    public static void remove(Government government) { // TODO Implement removal of spawn data
        government.removeMetaData(IS_PROXIED.getKey());
        government.removeMetaData(LAST_PROXIED.getKey());
        government.removeMetaData(START_PROXIED.getKey());
    }
}
