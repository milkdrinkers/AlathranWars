package io.github.alathra.alathranwars.conflict.war.side.spawn;

import com.palmergames.bukkit.towny.object.Town;
import io.github.alathra.alathranwars.conflict.war.side.Side;
import io.github.alathra.alathranwars.conflict.war.side.SideImpl;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.jetbrains.annotations.Nullable;

public class SpawnBuilder {
    private String name = "";
    private Location location;
    private SpawnType type = SpawnType.TOWN;
    private Side side;
    private Town town;
    private Block block;
    private OfflinePlayer creator;

    public SpawnBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public SpawnBuilder setLocation(Location location) {
        this.location = location;
        return this;
    }

    public SpawnBuilder setType(SpawnType type) {
        this.type = type;
        return this;
    }

    public SpawnBuilder setSide(Side side) {
        this.side = side;
        return this;
    }

    /**
     * Should only be set for Town and Outpost type spawns
     *
     * @param town the associated/owning town
     * @return builder
     */
    public SpawnBuilder setTown(@Nullable Town town) {
        this.town = town;
        return this;
    }

    public SpawnBuilder setBlock(@Nullable Block block) {
        this.block = block;
        return this;
    }

    public SpawnBuilder setCreator(@Nullable OfflinePlayer creator) {
        this.creator = creator;
        return this;
    }

    /**
     * Build a new Spawn
     *
     * @return a new spawn
     * @throws SpawnCreationException exception
     */
    public SpawnImpl build() throws SpawnCreationException {
        if (name == null)
            throw new SpawnCreationException("Missing state name required to create Spawn!");

        if (location == null)
            throw new SpawnCreationException("Missing state location required to create Spawn!");

        if (type == null)
            throw new SpawnCreationException("Missing state type required to create Spawn!");

        if (side == null)
            throw new SpawnCreationException("Missing state side required to create Spawn!");

        if (town != null && type != SpawnType.TOWN && type != SpawnType.OUTPOST)
            throw new SpawnCreationException("Erroneous state town should only be set for Town and Outpost type spawns!");

        if (type == SpawnType.RALLY) {
            if (block == null)
                throw new SpawnCreationException("Missing state block required to create Spawn!");

            if (creator == null)
                throw new SpawnCreationException("Missing state creator required to create Spawn!");

            return new RallyPointImpl(name + ((SideImpl) side).getSpawnManager().incrementRallyId(), location, type, side, block, creator);
        }

        return new SpawnImpl(name, location, type, side, town);
    }
}