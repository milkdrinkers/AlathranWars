package io.github.alathra.alathranwars.conflict.war.side.spawn;

import io.github.alathra.alathranwars.api.AlathranWarsAPIImpl;
import io.github.alathra.alathranwars.conflict.persistence.Storable;
import io.github.alathra.alathranwars.conflict.war.side.Side;
import io.github.alathra.alathranwars.conflict.war.side.SideImpl;
import io.github.alathra.alathranwars.database.QueryUtils;
import io.github.alathra.alathranwars.database.schema.tables.records.SidesSpawnsRecord;
import io.github.alathra.alathranwars.packet.ParticleCircle;
import io.github.alathra.alathranwars.utility.Cfg;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

public class RallyPointImpl extends SpawnImpl implements RallyPoint, Storable<SidesSpawnsRecord> {
    public static final Duration PLAYER_PROXY_DELETE = Duration.ofSeconds(Cfg.get().respawns.rallies.proxyDelete);
    private final Block rallyBanner;
    private final OfflinePlayer creator;

    public RallyPointImpl(String name, Location location, SpawnType type, Side side, Block rallyBanner, OfflinePlayer creator) {
        super(name, location, type, side, null);
        this.rallyBanner = rallyBanner;
        this.creator = creator;
    }

    public Block getRallyBanner() {
        return rallyBanner;
    }

    @Override
    public OfflinePlayer getCreator() {
        return creator;
    }

    @Override
    public void update() {
        super.update();
        if (getStartProxied().plus(PLAYER_PROXY_DELETE).isBefore(Instant.now()) && isProxied()) {
            delete();
        }

        final Location loc = getLocation().clone().add(0, 0.4, 0);
        final Side side = getSide();
        Objects.requireNonNull(side, "Side for rally point was null while ticking");
        getLocation().getNearbyPlayers(50).forEach(player -> {
            ParticleCircle.sendCircle(player, loc, PLAYER_PROXY_RANGE, 45, AlathranWarsAPIImpl.getInstance().getColor(player, side));
        });
    }

    /**
     * Deletes this rally point
     */
    public void delete() {
        final Chunk chunk = getRallyBanner().getChunk();
        final boolean wasLoaded = chunk.isLoaded();
        if (!wasLoaded)
            chunk.load();

        getRallyBanner().setType(Material.AIR);

        if (!wasLoaded)
            chunk.unload();
    }

    @Override
    public SidesSpawnsRecord deserialize() {
        return new SidesSpawnsRecord(
            QueryUtils.UUIDUtil.toBytes(((SideImpl) getSide()).getUUID()),
            getName(),
            QueryUtils.InstantUtil.toDateTime(getLastProxied()),
            QueryUtils.InstantUtil.toDateTime(getStartProxied()),
            QueryUtils.BooleanUtil.toByte(isProxied()),
            QueryUtils.UUIDUtil.toBytes(getCreator().getUniqueId()),
            QueryUtils.UUIDUtil.toBytes(getLocation().getWorld().getUID()),
            getLocation().getBlockX(),
            getLocation().getBlockY(),
            getLocation().getBlockZ(),
            (double) getLocation().getYaw(),
            (double) getLocation().getPitch(),
            QueryUtils.UUIDUtil.toBytes(getRallyBanner().getWorld().getUID()),
            getRallyBanner().getLocation().getBlockX(),
            getRallyBanner().getLocation().getBlockY(),
            getRallyBanner().getLocation().getBlockZ()
        );
    }
}
