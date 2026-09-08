package io.github.alathra.alathranwars.hook.placeholderapi;

import io.github.alathra.alathranwars.AlathranWars;
import io.github.alathra.alathranwars.api.AlathranWarsAPIImpl;
import io.github.alathra.alathranwars.hook.NameColorHandler;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.clip.placeholderapi.expansion.Relational;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A PlaceholderAPI expansion. Read the docs at <a href="https://wiki.placeholderapi.com/developers/creating-a-placeholderexpansion/">here</a> on how to register your custom placeholders.
 */
public class PAPIExpansion extends PlaceholderExpansion implements Relational {
    private final AlathranWars plugin;
    private final NameColorHandler colorHandler;

    public PAPIExpansion(AlathranWars plugin) {
        this.plugin = plugin;
        this.colorHandler = NameColorHandler.getInstance();
    }

    @Override
    public @NotNull String getIdentifier() {
        return plugin.getPluginMeta().getName().replace(' ', '_').toLowerCase();
    }

    @Override
    public @NotNull String getAuthor() {
        return String.join(", ", plugin.getPluginMeta().getAuthors());
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getPluginMeta().getVersion();
    }

    @Override
    public boolean persist() {
        return true; // This needs to be true, or PlaceholderAPI will unregister the expansion during a plugin reload.
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player p, @NotNull String params) {
        return switch (params) {
            case "player_nametag_prefix" -> {
                if (colorHandler.isPlayerUsingModifiedName(p))
                    yield colorHandler.getPlayerPrefix(p);
                yield "";
            }
            case "player_tab" -> {
                if (colorHandler.isPlayerUsingModifiedName(p))
                    yield colorHandler.getPlayerTabNameColor(p);
                yield "";
            }
            case "player_tablist" -> {
                if (colorHandler.isPlayerUsingModifiedName(p))
                    yield colorHandler.getPlayerTabNameColor(p) + "%essentials_nickname_stripped%";
                yield "%essentials_nickname%";
            }
            case "player_nametag" -> {
                if (colorHandler.isPlayerUsingModifiedName(p))
                    yield colorHandler.getPlayerNameColor(p) + "%essentials_nickname_stripped%";
                yield "%essentials_nickname%";
            }
            case "player_tablist_maquillage" -> {
                if (colorHandler.isPlayerUsingModifiedName(p))
                    yield colorHandler.getPlayerTabNameColor(p) + "%essentials_nickname_stripped%";
                yield "%maquillage_namecolor_essentialsnick%";
            }
            case "player_nametag_maquillage" -> {
                if (colorHandler.isPlayerUsingModifiedName(p))
                    yield colorHandler.getPlayerNameColor(p) + "%essentials_nickname_stripped%";
                yield "%maquillage_namecolor_essentialsnick%";
            }
            default -> null;
        };
    }

    @Override
    public String onPlaceholderRequest(Player p1, Player p2, @NotNull String params) {
        return switch (params) {
            case "player_nametag_prefix" -> AlathranWarsAPIImpl.getInstance().getColorNamePrefix(p1, p2);
            case "player_nametag_suffix" -> AlathranWarsAPIImpl.getInstance().getColorNameSuffix(p1, p2);
            default -> null;
        };
    }
}
