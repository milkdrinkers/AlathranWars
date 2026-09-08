package io.github.alathra.alathranwars.gui.customization;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Describes and item in a gui
 */
public class GuiButton {
    private final GuiPosition position;
    private final Material material;
    private final ItemStack item;

    private GuiButton(GuiPosition position, Material material) {
        this(position, new ItemStack(material));
    }

    private GuiButton(GuiPosition position, ItemStack item) {
        this.position = position;
        this.material = item.getType();
        this.item = item;
    }

    /**
     * Get a new gui item with the following coordinates
     *
     * @param position position of the item
     * @param material the base item material to use
     * @return gui item
     */
    public static GuiButton of(GuiPosition position, Material material) {
        return new GuiButton(position, material);
    }

    /**
     * Get a new gui item with the following coordinates
     *
     * @param position position of the item
     * @param item     the base item to use
     * @return gui item
     */
    public static GuiButton of(GuiPosition position, ItemStack item) {
        return new GuiButton(position, item);
    }

    /**
     * Returns the gui position object for this item
     *
     * @return gui position
     */
    public GuiPosition getPosition() {
        return position;
    }

    /**
     * Returns the material of this item
     *
     * @return material
     */
    public Material getMaterial() {
        return material;
    }

    /**
     * Returns a clone of the item stack
     *
     * @return item stack
     */
    public ItemStack getItem() {
        return item.clone();
    }
}
