package io.github.alathra.alathranwars.data;

import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.metadata.LocationDataField;
import com.palmergames.bukkit.towny.utils.MetaDataUtil;
import io.github.alathra.alathranwars.utility.Utils;
import org.bukkit.Location;

public class ControlPoint {
    private final static LocationDataField controlPoint = new LocationDataField(TownyMetaHandler.META_NAMESPACE + "capturepoint");

    public static boolean exists(Town town) {
        return town.hasMeta(controlPoint.getKey());
    }

    public static Location get(Town town) {
        if (exists(town)) {
            return MetaDataUtil.getLocation(town, controlPoint);
        } else {
            final Location location = findFallbackLocation(town);

            set(town, location);

            return location;
        }
    }

    public static void set(Town town, Location location) {
        if (exists(town)) {
            MetaDataUtil.setLocation(town, controlPoint, location, true);
        } else {
            town.addMetaData(new LocationDataField(controlPoint.getKey(), location));
        }
    }

    /**
     * Get a fallback location for this town. Primarily returns the center of the town block exposed to air, or the town spawn, or the world spawn.
     *
     * @param town town
     * @return a guaranteed location
     */
    private static Location findFallbackLocation(Town town) {
        TownBlock townBlock = town.getHomeBlockOrNull();
        Location townSpawnLoc = town.getSpawnOrNull();

        if (townBlock == null) {
            if (townSpawnLoc != null)
                return townSpawnLoc;
            return town.getWorld().getSpawnLocation();
        } else {
            return Utils.getTownBlockCenter(townBlock).toHighestLocation().toCenterLocation();
        }
    }
}
