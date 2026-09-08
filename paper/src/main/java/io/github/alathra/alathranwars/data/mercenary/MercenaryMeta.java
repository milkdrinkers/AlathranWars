package io.github.alathra.alathranwars.data.mercenary;

import com.palmergames.bukkit.towny.object.Government;
import com.palmergames.bukkit.towny.object.metadata.BooleanDataField;
import com.palmergames.bukkit.towny.utils.MetaDataUtil;
import io.github.alathra.alathranwars.AlathranWars;
import io.github.alathra.alathranwars.data.TownyMetaHandler;

public class MercenaryMeta {
    private final AlathranWars plugin;
    private static final BooleanDataField IS_MERCENARY = new BooleanDataField(TownyMetaHandler.META_NAMESPACE + "is_mercenary", false);

    MercenaryMeta(AlathranWars plugin) {
        this.plugin = plugin;
    }

    public static boolean isMercenary(Government government) {
        return government.hasMeta(IS_MERCENARY.getKey()) && MetaDataUtil.getBoolean(government, IS_MERCENARY);
    }

    public static void setMercenary(Government government, boolean enabled) {
        if (government.hasMeta(IS_MERCENARY.getKey())) {
            MetaDataUtil.setBoolean(government, IS_MERCENARY, enabled, true);
        } else {
            government.addMetaData(new BooleanDataField(IS_MERCENARY.getKey(), enabled), true);
        }
    }
}
