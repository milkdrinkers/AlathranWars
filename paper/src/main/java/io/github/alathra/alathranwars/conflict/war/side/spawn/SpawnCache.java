package io.github.alathra.alathranwars.conflict.war.side.spawn;

import com.palmergames.bukkit.towny.object.Town;
import io.github.alathra.alathranwars.AbstractAlathranWars;
import io.github.alathra.alathranwars.Reloadable;
import io.github.alathra.alathranwars.conflict.IUpdateable;
import io.github.alathra.alathranwars.conflict.war.side.Side;
import io.github.alathra.alathranwars.utility.SpawnUtils;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Contains a cached list of all spawns related to a {@link Side}.
 */
public class SpawnCache implements Reloadable, IUpdateable {
    private final Side side;
    private final Set<SpawnImpl> spawns;

    public SpawnCache(Side side) {
        this(side, ConcurrentHashMap.newKeySet());
    }

    public SpawnCache(Side side, Set<RallyPointImpl> rallyPoints) {
        this.side = side;
        this.spawns = ConcurrentHashMap.newKeySet();
        spawns.addAll(rallyPoints);
        getSpawns().forEach(spawn -> {
            spawn.setSide(side); // Ugly hack to apply side for all spawn objects as Side is not available when constructing them before constructing side object
        });
    }

    @Override
    public void onEnable(AbstractAlathranWars plugin) {
        spawns.addAll(SpawnUtils.computeSpawnPoints(side));
    }

    /**
     * Get the {@link Side} associated with this spawn cache
     *
     * @return side
     */
    public Side getSide() {
        return side;
    }

    /**
     * Get a list of all spawns
     *
     * @return list
     */
    public Set<SpawnImpl> getSpawns() {
        return spawns.stream().collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Get a list of all spawns associated with a town (Only outposts and town spawn)
     *
     * @param town town
     * @return list
     */
    public Set<SpawnImpl> getSpawns(Town town) {
        return getSpawns().stream()
            .filter(spawn -> spawn.getTown().isPresent() && spawn.getTown().get().equals(town))
            .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Get a list of all rallies
     * Convenience method for getting a cached list of rallies so the user doesn't have to loop
     *
     * @return list
     */
    public Set<RallyPointImpl> getRallies() {
        return spawns.stream()
            .filter(spawn -> spawn instanceof RallyPointImpl)
            .map(spawn -> (RallyPointImpl) spawn)
            .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Runs update logic for all spawns held by this spawn cache
     */
    @Override
    public void update() {
        getSpawns().forEach(SpawnImpl::update);
    }

    /**
     * Add a spawn
     *
     * @param spawn spawn
     * @return whether successful or not
     */
    public boolean add(SpawnImpl spawn) {
        return spawns.add(spawn);
    }

    /**
     * Remove a spawn
     *
     * @param spawn spawn
     * @return whether successful or not
     */
    public boolean remove(SpawnImpl spawn) {
        return spawns.remove(spawn);
    }

    public int incrementRallyId() {
        return getRallies().size() + 1;
    }
}
