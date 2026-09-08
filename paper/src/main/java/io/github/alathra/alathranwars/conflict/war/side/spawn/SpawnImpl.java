package io.github.alathra.alathranwars.conflict.war.side.spawn;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
import io.github.alathra.alathranwars.conflict.IUpdateable;
import io.github.alathra.alathranwars.conflict.war.side.Side;
import io.github.alathra.alathranwars.data.spawn.SpawnTownMeta;
import io.github.alathra.alathranwars.event.spawn.SpawnProxyEvent;
import io.github.alathra.alathranwars.event.spawn.SpawnUnproxyEvent;
import io.github.alathra.alathranwars.utility.Cfg;
import io.github.alathra.alathranwars.utility.SideUtils;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.SoftReference;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents a spawn location.
 */
public class SpawnImpl implements Spawn, IUpdateable {
    public static final int PLAYER_PROXY_COUNT = Cfg.get().respawns.proxy.minPlayers; // Required players to proxy
    public static final int PLAYER_PROXY_RANGE = Cfg.get().respawns.proxy.range; // Range for proxying
    public static final Duration PLAYER_PROXY_REENABLE = Duration.ofSeconds(Cfg.get().respawns.proxy.reenableTime);

    private final String name;
    private final Location location;
    private final SpawnType type;
    private @Nullable SoftReference<Side> side;
    private final @Nullable Town town; // The associated town, only used by Town and Outpost types
    private boolean isProxied = false;
    private Instant lastProxied = Instant.now();
    private Instant startProxied = Instant.now();

    @ApiStatus.Internal
    SpawnImpl(String name, Location location, SpawnType type, @Nullable Town town) {
        this.name = name;
        this.location = location;
        this.type = type;
        this.side = null;
        this.town = town;

        switch (type) {
            case TOWN -> {
                final @Nullable Town t = TownyAPI.getInstance().getTown(location);
                if (t == null)
                    return;

                this.isProxied = SpawnTownMeta.getIsProxied(t);
                final Instant lastProxied = SpawnTownMeta.getLastProxied(t);
                if (lastProxied != null)
                    this.lastProxied = lastProxied;
                final Instant startProxied = SpawnTownMeta.getStartProxied(t);
                if (startProxied != null)
                    this.startProxied = startProxied;
            }
            case OUTPOST -> {
                // TODO Store outposts using towny
            }
        }
    }

    @ApiStatus.Internal
    SpawnImpl(String name, Location location, SpawnType type, Side side, @Nullable Town town) {
        this.name = name;
        this.location = location;
        this.type = type;
        this.side = new SoftReference<>(side);
        this.town = town;

        // TODO Fetch spawn values based on side type
        switch (type) {
            case TOWN -> {
                final @Nullable Town t = TownyAPI.getInstance().getTown(location);
                if (t == null)
                    return;

                this.isProxied = SpawnTownMeta.getIsProxied(t);
                final Instant lastProxied = SpawnTownMeta.getLastProxied(t);
                if (lastProxied != null)
                    this.lastProxied = lastProxied;
                final Instant startProxied = SpawnTownMeta.getStartProxied(t);
                if (startProxied != null)
                    this.startProxied = startProxied;
            }
            case OUTPOST -> {

            }
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public SpawnType getType() {
        return type;
    }

    public void setSide(final Side side) {
        this.side = new SoftReference<>(side);
    }

    @Override
    public Side getSide() {
        if (side == null || side.get() == null)
            return null;
        return side.get();
    }

    public Optional<Town> getTown() {
        return Optional.ofNullable(town);
    }

    /**
     * Check if this spawn is proxied, also checks if some time has passed since it was last proxied
     *
     * @return boolean
     */
    @Override
    public boolean isProxied() {
        final Instant reenableTime = getLastProxied().plus(PLAYER_PROXY_REENABLE);
        return isProxied || reenableTime.isAfter(Instant.now());
    }

    /**
     * Set the spawn as proxied (disabling it)
     *
     * @param proxied boolean
     */
    public void setProxied(boolean proxied) {
        if (!isProxied && proxied) { // Started being proxied
            startProxied = Instant.now();
            Objects.requireNonNull(getSide(), "Side was null when trying to proxy spawn");
            new SpawnProxyEvent(getSide().getWar(), this).callEvent();
        } else if (isProxied && !proxied) { // Stopped being proxied
            Objects.requireNonNull(getSide(), "Side was null when trying to un-proxy spawn");
            new SpawnUnproxyEvent(getSide().getWar(), this).callEvent();
        }

        // update data
        isProxied = proxied;
        if (proxied)
            lastProxied = Instant.now();
    }

    /**
     * Returns the last time this spawn was proxied
     *
     * @return instant
     */
    @Override
    public Instant getLastProxied() {
        return lastProxied;
    }

    /**
     * Returns the time this spawn has been proxied since
     *
     * @return instant
     */
    public Instant getStartProxied() {
        return startProxied;
    }

    /**
     * Runs updates on the spawn calculating whether it should be proxied or not
     */
    @Override
    public void update() {
        setProxied(shouldProxy());
    }

    /**
     * Returns whether this spawn should be proxied or not
     *
     * @return boolean
     */
    private boolean shouldProxy() {
        try {
            final Side oppSide = SideUtils.getOpponent(getSide());


            final long oppsInRange = SideUtils.getPlayersFromSideAtCoord(getLocation(), PLAYER_PROXY_RANGE, oppSide).stream().filter(p -> p.hasLineOfSight(getLocation())).count();

            return oppsInRange >= PLAYER_PROXY_COUNT;
        } catch (IllegalStateException e) {
            return false;
        }
    }

    /**
     * Run the logic for spawning this entity at this spawn
     *
     * @param entity entity
     */
    public void spawn(LivingEntity entity) {
        entity.teleportAsync(getLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SpawnImpl spawn)) return false;
        return equals(spawn);
    }

    // 3x2x3 Rectangle (Spawn location is always 0, 0, 0)
    final static Vector BOUNDING_BOX_MIN = new Vector(-1, 0, -1);
    final static Vector BOUNDING_BOX_MAX = new Vector(1, 1, 1);

    @Override
    public boolean withinBoundingBox(Location location) {
        return withinBoundingBox(this, location);
    }

    public static boolean withinBoundingBox(SpawnImpl spawn, Location location) {
        if (!spawn.getLocation().getWorld().equals(location.getWorld()))
            return false;

        final Vector min = BOUNDING_BOX_MIN;
        final Vector max = BOUNDING_BOX_MAX;

        // Calculate the relative pos of location to the spawn location
        final int relativeX = location.getBlockX() - spawn.getLocation().getBlockX();
        final int relativeY = location.getBlockY() - spawn.getLocation().getBlockY();
        final int relativeZ = location.getBlockZ() - spawn.getLocation().getBlockZ();

        // Check if relative pos' are within bounding box
        return relativeX >= min.getBlockX() && relativeX <= max.getBlockX() &&
            relativeY >= min.getBlockY() && relativeY <= max.getBlockY() &&
            relativeZ >= min.getBlockZ() && relativeZ <= max.getBlockZ();
    }

    public boolean equals(SpawnImpl spawn) {
        return spawn.getLocation().getWorld().equals(getLocation().getWorld()) && spawn.getLocation().equals(getLocation()) && spawn.getSide().equals(getSide()) && spawn.getType().equals(getType()) && spawn.getName().equals(getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getLocation(), getType(), getSide());
    }
}
