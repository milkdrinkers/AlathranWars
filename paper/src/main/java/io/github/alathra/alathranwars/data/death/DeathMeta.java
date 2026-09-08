package io.github.alathra.alathranwars.data.death;

import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.metadata.BooleanDataField;
import com.palmergames.bukkit.towny.utils.MetaDataUtil;
import io.github.alathra.alathranwars.data.TownyMetaHandler;

public class DeathMeta {
    private static final BooleanDataField IS_DEAD = new BooleanDataField(TownyMetaHandler.META_NAMESPACE + "resident_is_dead");

    private static DeathMeta INSTANCE = null;

    private DeathMeta() {
    }

    public static DeathMeta getInstance() {
        if (INSTANCE == null)
            INSTANCE = new DeathMeta();
        return INSTANCE;
    }

    public boolean isDead(Resident resident) {
        if (resident.hasMeta(IS_DEAD.getKey()))
            return MetaDataUtil.getBoolean(resident, IS_DEAD);
        return false;
    }

    public void setDead(Resident resident, boolean state) {
        MetaDataUtil.setBoolean(resident, IS_DEAD, state, true);
    }
}
