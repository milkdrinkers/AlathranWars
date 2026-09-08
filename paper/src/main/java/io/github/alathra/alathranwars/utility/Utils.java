package io.github.alathra.alathranwars.utility;

import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.WorldCoord;
import io.github.alathra.alathranwars.conflict.battle.siege.Siege;
import io.github.alathra.alathranwars.conflict.battle.siege.SiegeImpl;
import io.github.alathra.alathranwars.conflict.war.WarController;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public final class Utils {
    private static final ArrayList<Material> weaponList = new ArrayList<>(List.of(
        Material.NETHERITE_SWORD,
        Material.NETHERITE_AXE,
        Material.DIAMOND_SWORD,
        Material.DIAMOND_AXE,
        Material.IRON_SWORD,
        Material.IRON_AXE
    ));
    private static final ArrayList<Material> armorList = new ArrayList<>(List.of(
        Material.NETHERITE_HELMET,
        Material.NETHERITE_CHESTPLATE,
        Material.NETHERITE_LEGGINGS,
        Material.NETHERITE_BOOTS,
        Material.DIAMOND_HELMET,
        Material.DIAMOND_CHESTPLATE,
        Material.DIAMOND_LEGGINGS,
        Material.DIAMOND_BOOTS,
        Material.IRON_HELMET,
        Material.IRON_CHESTPLATE,
        Material.IRON_LEGGINGS,
        Material.IRON_BOOTS
    ));

    /**
     * Damages all gear in a players inventory
     *
     * @param p
     */
    public static void damageAllGear(@NotNull Player p) {
        for (@Nullable ItemStack itemStack : p.getInventory().getContents()) {
            if (itemStack == null)
                continue;

            if (weaponList.contains(itemStack.getType())) {
                setWeaponDurability(itemStack);
            }
        }

        final ItemStack[] armor = p.getInventory().getArmorContents();

        for (@Nullable ItemStack itemStack : armor) {
            if (itemStack == null)
                continue;

            if (armorList.contains(itemStack.getType())) {
                setArmorDurability(itemStack);
            }
        }
    }

    private static void setArmorDurability(@Nullable ItemStack itemStack) {
        if (itemStack == null)
            return;

        ItemMeta meta = itemStack.getItemMeta();

        if (meta == null)
            return;

        if (!(meta instanceof Damageable damageable))
            return;

        switch (itemStack.getType()) {
            case NETHERITE_HELMET -> {
                damageable.setDamage(damageable.getDamage() + 101);
                if (damageable.getDamage() > 400) {
                    damageable.setDamage(406);
                }
            }
            case NETHERITE_CHESTPLATE -> {
                damageable.setDamage(damageable.getDamage() + 148);
                if (damageable.getDamage() > 450) {
                    damageable.setDamage(591);
                }
            }
            case NETHERITE_LEGGINGS -> {
                damageable.setDamage(damageable.getDamage() + 138);
                if (damageable.getDamage() > 420) {
                    damageable.setDamage(554);
                }
            }
            case NETHERITE_BOOTS -> {
                damageable.setDamage(damageable.getDamage() + 120);
                if (damageable.getDamage() > 370) {
                    damageable.setDamage(480);
                }
            }
            case DIAMOND_HELMET -> {
                damageable.setDamage(damageable.getDamage() + 90);
                if (damageable.getDamage() > 280) {
                    damageable.setDamage(362);
                }
            }
            case DIAMOND_CHESTPLATE -> {
                damageable.setDamage(damageable.getDamage() + 132);
                if (damageable.getDamage() > 410) {
                    damageable.setDamage(527);
                }
            }
            case DIAMOND_LEGGINGS -> {
                damageable.setDamage(damageable.getDamage() + 123);
                if (damageable.getDamage() > 380) {
                    damageable.setDamage(494);
                }
            }
            case DIAMOND_BOOTS -> {
                damageable.setDamage(damageable.getDamage() + 107);
                if (damageable.getDamage() > 330) {
                    damageable.setDamage(428);
                }
            }
            case IRON_HELMET -> {
                damageable.setDamage(damageable.getDamage() + 41);
                if (damageable.getDamage() > 125) {
                    damageable.setDamage(164);
                }
            }
            case IRON_CHESTPLATE -> {
                damageable.setDamage(damageable.getDamage() + 60);
                if (damageable.getDamage() > 190) {
                    damageable.setDamage(239);
                }
            }
            case IRON_LEGGINGS -> {
                damageable.setDamage(damageable.getDamage() + 56);
                if (damageable.getDamage() > 180) {
                    damageable.setDamage(224);
                }
            }
            case IRON_BOOTS -> {
                damageable.setDamage(damageable.getDamage() + 48);
                if (damageable.getDamage() > 150) {
                    damageable.setDamage(194);
                }
            }
        }

        itemStack.setItemMeta(damageable);
    }

    private static void setWeaponDurability(@Nullable ItemStack itemStack) {
        if (itemStack == null)
            return;

        ItemMeta meta = itemStack.getItemMeta();

        if (meta == null)
            return;

        if (!(meta instanceof Damageable damageable))
            return;

        switch (itemStack.getType()) {
            case NETHERITE_SWORD, NETHERITE_AXE -> {
                damageable.setDamage(damageable.getDamage() + 507);
                if (damageable.getDamage() > 2000) {
                    damageable.setDamage(2030);
                }
            }
            case DIAMOND_SWORD, DIAMOND_AXE -> {
                damageable.setDamage(damageable.getDamage() + 390);
                if (damageable.getDamage() > 1500) {
                    damageable.setDamage(1560);
                }
            }
            case IRON_SWORD, IRON_AXE, TRIDENT -> {
                damageable.setDamage(damageable.getDamage() + 62);
                if (damageable.getDamage() > 200) {
                    damageable.setDamage(249);
                }
            }
        }

        itemStack.setItemMeta(damageable);
    }

    // TODO Implement usage in siege commands to make siege optional argument

    /**
     * Get the closest siege to the player
     *
     * @param p              player
     * @param checkIfInSiege check whether the players is a participant in the battle
     * @return siege or null
     * @deprecated This will eventually be replaced by a more robust method
     */
    @Deprecated(forRemoval = true, since = "4.0.0")
    public static @Nullable Siege getClosestSiege(Player p, boolean checkIfInSiege) {
        Set<Siege> sieges = checkIfInSiege
            ? WarController.getInstance().getSieges().stream().filter(siege -> siege.isInBattle(p)).collect(Collectors.toSet())
            : WarController.getInstance().getSieges();

        @Nullable Siege siegeResult = null;
        final Location playerLoc = p.getLocation();
        double closestSiege = Double.MAX_VALUE;

        for (Siege siege : sieges) {
            final @Nullable Location location = siege.getControlPoint();

            if (location == null) continue;
            if (!location.getWorld().equals(playerLoc.getWorld())) continue;

            final double distance = location.distanceSquared(playerLoc);
            if (distance < closestSiege) {
                siegeResult = siege;
                closestSiege = distance;
            }
        }

        return siegeResult;
    }

    /**
     * Check if the player is currently within the battle zone
     *
     * @param p     player
     * @param siege battle
     * @return true if within range
     */
    public static boolean isOnSiegeBattlefield(Player p, Siege siege) {
        if (siege == null) return false;

        final Location pLocation = p.getLocation();
        final Location siegeLocation = siege.getControlPoint();

        if (siegeLocation == null) return false;
        if (!pLocation.getWorld().equals(siegeLocation.getWorld())) return false;

        return (pLocation.distanceSquared(siegeLocation) <= SiegeImpl.BATTLEFIELD_RANGE_SQUARED);
    }

    /**
     * @param worldCoord worldCoord
     * @return List of connected chunks
     * @author NinjaMandalorian
     * @deprecated We no longer rely on cells in any capacity
     */
    @Deprecated(forRemoval = true, since = "4.0.0")
    private static @NotNull ArrayList<WorldCoord> getAdjCells(@NotNull WorldCoord worldCoord) {
        @NotNull ArrayList<WorldCoord> worldCoords = new ArrayList<>();

        int[][] XZarray = new int[][]{
            {-1, 0},
            {1, 0},
            {0, -1},
            {0, 1}
        }; // Array that contains relative orthogonal shifts from origin

        for (int[] pair : XZarray) {
            // Constructs new WorldCoord for comparison
            @NotNull WorldCoord tCoord = new WorldCoord(worldCoord.getWorldName(), worldCoord.getX() + pair[0], worldCoord.getZ() + pair[1]);
            if (tCoord.getTownOrNull() != null && tCoord.getTownOrNull() == worldCoord.getTownOrNull()) {
                // If in town, and in same town, adds to return list
                worldCoords.add(tCoord);
            }
        }
        return worldCoords;
    }

    /**
     * Gets all adjacently connected townblocks
     *
     * @param chunkCoord - WorldCoord to check at
     * @return List of WorldCoords
     * @deprecated We no longer rely on cells in any capacity
     */
    @Deprecated(forRemoval = true, since = "4.0.0")
    public static @NotNull ArrayList<WorldCoord> getCluster(WorldCoord chunkCoord) {
        // worldCoords is the returning array, searchList is the to-search list.
        @NotNull ArrayList<WorldCoord> worldCoords = new ArrayList<>();
        @NotNull ArrayList<WorldCoord> searchList = new ArrayList<>(Collections.singletonList(chunkCoord)); // Adds 1st chunk to list

        // Iterates through searchList, to create a full list of every adjacent cell.
        while (!searchList.isEmpty()) {
            WorldCoord toSearch = searchList.get(0); // Gets WorldCoord
            @NotNull ArrayList<WorldCoord> adjCells = getAdjCells(toSearch); // Gets adjacent cells

            for (WorldCoord cell : adjCells) {
                if (worldCoords.contains(cell)) continue; // If in final list, ignore.
                if (searchList.contains(cell)) continue; // If in to-search list, ignore

                // Otherwise, add to search-list.
                searchList.add(cell);
            }

            // Removes from search list and adds to finished list. After checking all adjacent chunks.
            searchList.remove(toSearch);
            worldCoords.add(toSearch);
        }

        return worldCoords; // Returns list
    }

    /**
     * Get the approximate center of the town block
     *
     * @param townBlock town block
     * @return location
     */
    public static Location getTownBlockCenter(TownBlock townBlock) {
        final World townWorld = townBlock.getWorld().getBukkitWorld();
        final Location locUpper = townBlock.getWorldCoord().getUpperMostCornerLocation();
        final Location locLower = townBlock.getWorldCoord().getLowerMostCornerLocation();

        Objects.requireNonNull(townWorld, "townWorld is somehow null!");

        return locLower.toVector().getMidpoint(locUpper.toVector()).toLocation(townWorld);
    }

    /**
     * Just use the methods that exist on {@link Location} object
     *
     * @param location location
     * @param dist     dist
     * @return list
     */
    @Deprecated(forRemoval = true, since = "4.0.0")
    public static List<Player> getPlayersAtCoord(Location location, int dist) {
        final double distSquared = Math.pow(dist, 2);
        return Bukkit.getOnlinePlayers().stream()
            .map(Player::getPlayer)
            .filter(Objects::nonNull)
            .filter(p -> location.getWorld().equals(p.getWorld()))
            .filter(p -> location.distanceSquared(p.getLocation()) < distSquared)
            .toList();
    }

    public static String rgbToHex(Color color) {
        return String.format("#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue());
    }

    public static Color hexToRgb(String hex) {
        return Color.decode(hex);
    }
}
