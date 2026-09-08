package io.github.alathra.alathranwars.api;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Government;
import com.palmergames.bukkit.towny.object.Resident;
import io.github.alathra.alathranwars.data.mercenary.MercenaryMeta;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

interface MercenaryAPI {
    default boolean isMercenary(Government government) {
        return MercenaryMeta.isMercenary(government);
    }

    default boolean isMercenary(@Nullable Resident resident) {
        return resident != null && resident.getTownOrNull() != null && isMercenary(resident.getTownOrNull());
    }

    default boolean isMercenary(@Nullable Player player) {
        return player != null && isMercenary(TownyAPI.getInstance().getResident(player));
    }

    default void setMercenary(Government government, boolean state) {
        MercenaryMeta.setMercenary(government, state);
    }

    default boolean canToggleMercenary(Government government) {
        return !AlathranWarsAPIImpl.getInstance().hasCooldown(government);
    }
}
