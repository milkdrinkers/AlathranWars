package io.github.alathra.alathranwars.api;

import com.github.retrooper.packetevents.protocol.color.Color;
import com.palmergames.bukkit.towny.object.Government;
import io.github.alathra.alathranwars.conflict.war.side.Side;
import io.github.alathra.alathranwars.utility.Cfg;
import io.github.alathra.alathranwars.utility.SideUtils;
import io.github.alathra.alathranwars.utility.Utils;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.entity.Player;

public interface ColorAPI {
    // TODO Current implementation is very inefficient as it recalculates
    //  the relationships on every method invocation, in the future we want
    //  to back the implementation with a cache that is either:
    //  1) Invalidated on side changes
    //  2) Or a time based cache where entries expire after X minutes
    //
    //  One has precise results but is more complex to implement, the other is quick and dirty

    enum ColorType {
        FRIENDLY,
        NEUTRAL,
        HOSTILE
    }

    default BossBar.Color getColorBossbar(ColorType color) {
        return switch (color) {
            case FRIENDLY -> BossBar.Color.BLUE;
            case NEUTRAL -> BossBar.Color.YELLOW;
            case HOSTILE -> BossBar.Color.RED;
        };
    }

    default String getColorNamePrefix(ColorType color) {
        return switch (color) {
            case FRIENDLY -> Cfg.get().colors.nametags.prefix.friendly;
            case NEUTRAL -> Cfg.get().colors.nametags.prefix.neutral;
            case HOSTILE -> Cfg.get().colors.nametags.prefix.hostile;
        };
    }

    default String getColorNameSuffix(ColorType color) {
        return switch (color) {
            case FRIENDLY -> Cfg.get().colors.nametags.suffix.friendly;
            case NEUTRAL -> Cfg.get().colors.nametags.suffix.neutral;
            case HOSTILE -> Cfg.get().colors.nametags.suffix.hostile;
        };
    }

    /**
     * Get the prefix string representing the relationship between two players
     *
     * @param origin The player from whose perspective the color is determined
     * @param target The player whose relationship to the origin is being evaluated
     * @return A minimessage string
     */
    default String getColorNamePrefix(Player origin, Player target) {
        boolean isHostile = false;
        boolean isAlly = false;
        for (Side side : SideUtils.getPlayerSides(origin)) {
            final Side opponentSide = SideUtils.getOpponent(side);

            if (opponentSide.isOnSide(target) && !side.isSurrendered(target)) {
                isHostile = true;
                break;
            }

            if (side.isOnSide(target)) {
                isAlly = true;
            }
        }

        if (isHostile) {
            return getColorNamePrefix(ColorType.HOSTILE);
        } else if (isAlly) {
            return getColorNamePrefix(ColorType.FRIENDLY);
        } else {
            return getColorNamePrefix(ColorType.NEUTRAL);
        }
    }

    /**
     * Get the suffix string representing the relationship between two players
     *
     * @param origin The player from whose perspective the color is determined
     * @param target The player whose relationship to the origin is being evaluated
     * @return A minimessage string
     */
    default String getColorNameSuffix(Player origin, Player target) {
        boolean isHostile = false;
        boolean isAlly = false;
        for (Side side : SideUtils.getPlayerSides(origin)) {
            final Side opponentSide = SideUtils.getOpponent(side);

            if (opponentSide.isOnSide(target) && !side.isSurrendered(target)) {
                isHostile = true;
                break;
            }

            if (side.isOnSide(target)) {
                isAlly = true;
            }
        }

        if (isHostile) {
            return getColorNameSuffix(ColorType.HOSTILE);
        } else if (isAlly) {
            return getColorNameSuffix(ColorType.FRIENDLY);
        } else {
            return getColorNameSuffix(ColorType.NEUTRAL);
        }
    }

    default Color getColorParticle(ColorType particleColor) {
        final String color = switch (particleColor) {
            case FRIENDLY -> Cfg.get().colors.particles.friendly;
            case NEUTRAL -> Cfg.get().colors.particles.neutral;
            case HOSTILE -> Cfg.get().colors.particles.hostile;
        };

        final java.awt.Color c = Utils.hexToRgb(color);
        return new Color(c.getRed(), c.getGreen(), c.getBlue());
    }

    /**
     * Get the color representing the relationship between two players
     *
     * @param origin The player from whose perspective the color is determined
     * @param target The player whose relationship to the origin is being evaluated
     * @return A Color object representing the relationship:
     * - Red (254, 63, 63) for hostile
     * - Blue (63, 63, 254) for ally
     * - Yellow (254, 254, 63) for neutral
     */
    default Color getColor(Player origin, Player target) {
        boolean isHostile = false;
        boolean isAlly = false;
        for (Side side : SideUtils.getPlayerSides(origin)) {
            final Side opponentSide = SideUtils.getOpponent(side);

            if (opponentSide.isOnSide(target) && !side.isSurrendered(target)) {
                isHostile = true;
                break;
            }

            if (side.isOnSide(target)) {
                isAlly = true;
            }
        }

        if (isHostile) {
            return getColorParticle(ColorType.HOSTILE);
        } else if (isAlly) {
            return getColorParticle(ColorType.FRIENDLY);
        } else {
            return getColorParticle(ColorType.NEUTRAL);
        }
    }

    /**
     * Get the color representing the relationship between a player and a government
     *
     * @param origin The player from whose perspective the color is determined
     * @param target The government whose relationship to the origin is being evaluated
     * @return A Color object representing the relationship:
     * - Red (254, 63, 63) for hostile
     * - Blue (63, 63, 254) for ally
     * - Yellow (254, 254, 63) for neutral
     */
    default Color getColor(Player origin, Government target) {
        boolean isHostile = false;
        boolean isAlly = false;
        for (Side side : SideUtils.getPlayerSides(origin)) {
            final Side opponentSide = SideUtils.getOpponent(side);

            if (opponentSide.isOnSide(target) && !side.isSurrendered(target)) {
                isHostile = true;
                break;
            }

            if (side.isOnSide(target)) {
                isAlly = true;
            }
        }

        if (isHostile) {
            return getColorParticle(ColorType.HOSTILE);
        } else if (isAlly) {
            return getColorParticle(ColorType.FRIENDLY);
        } else {
            return getColorParticle(ColorType.NEUTRAL);
        }
    }

    /**
     * Get the color representing the relationship between a player and a side
     *
     * @param origin The player from whose perspective the color is determined
     * @param target The side whose relationship to the origin is being evaluated
     * @return A Color object representing the relationship:
     * - Red (254, 63, 63) for hostile
     * - Blue (63, 63, 254) for ally
     * - Yellow (254, 254, 63) for neutral
     */
    default Color getColor(Player origin, Side target) {
        boolean isHostile = false;
        boolean isAlly = false;
        for (Side side : SideUtils.getPlayerSides(origin)) {
            final Side opponentSide = SideUtils.getOpponent(side);

            if (opponentSide.equals(target)) {
                isHostile = true;
                break;
            }

            if (side.equals(target)) {
                isAlly = true;
            }
        }

        if (isHostile) {
            return getColorParticle(ColorType.HOSTILE);
        } else if (isAlly) {
            return getColorParticle(ColorType.FRIENDLY);
        } else {
            return getColorParticle(ColorType.NEUTRAL);
        }
    }
}
