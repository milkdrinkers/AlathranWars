package io.github.alathra.alathranwars.gui.customization;

/**
 * Describes the position of an item in a gui
 */
public class GuiPosition {
    private final int x;
    private final int y;

    private GuiPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Get the horizontal position
     *
     * @return int
     * @implNote Positions start at 1
     */
    public int x() {
        return x;
    }

    /**
     * Get the horizontal position,
     * Internally calls {@link #x()}
     *
     * @return int
     * @implNote Positions start at 1
     */
    public int horizontal() {
        return x();
    }

    /**
     * Get the column,
     * Internally calls {@link #x()}
     *
     * @return int
     * @implNote Columns start at 1
     */
    public int col() {
        return x();
    }

    /**
     * Get the vertical position
     *
     * @return int
     * @implNote Positions start at 1
     */
    public int y() {
        return y;
    }

    /**
     * Get the vertical position,
     * Internally calls {@link #y()}
     *
     * @return int
     * @implNote Positions start at 1
     */
    public int vertical() {
        return y();
    }

    /**
     * Get the row,
     * Internally calls {@link #y()}
     *
     * @return int
     * @implNote Rows start at 1
     */
    public int row() {
        return y();
    }

    /**
     * Get a new gui position with the following coordinates
     *
     * @param x horizontal position
     * @param y vertical position
     * @return gui position
     * @implNote Positions start at 1
     */
    public static GuiPosition of(int x, int y) {
        checkBoundsX(x);
        checkBoundsY(y);
        return new GuiPosition(x, y);
    }

    private static void checkBoundsX(final int pos) throws GuiConfigurationException {
        if (pos < 1)
            throw new GuiConfigurationException("The position is less than 1 but no gui's support that width!");

        if (pos > 9)
            throw new GuiConfigurationException("The position is greater than 9 but no gui's support that width!");
    }

    private static void checkBoundsY(final int pos) throws GuiConfigurationException {
        if (pos < 1)
            throw new GuiConfigurationException("The position is less than 1 but no gui's support that height!");

        if (pos > 7)
            throw new GuiConfigurationException("The position is greater than 7 but no gui's support that height!");
    }
}
