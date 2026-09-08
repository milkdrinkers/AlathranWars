package io.github.alathra.alathranwars.cooldown.listener;

import io.github.alathra.alathranwars.AbstractAlathranWars;
import io.github.alathra.alathranwars.Reloadable;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;

/**
 * A class to handle registration of event listeners.
 */
@SuppressWarnings("FieldCanBeLocal")
public class ListenerHandler implements Reloadable {
    private final AbstractAlathranWars plugin;
    private final List<Listener> listeners = new ArrayList<>();

    public ListenerHandler(AbstractAlathranWars plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onLoad(AbstractAlathranWars plugin) {
    }

    @Override
    public void onEnable(AbstractAlathranWars plugin) {
        listeners.clear();
        listeners.add(new CooldownListener(plugin));

        for (Listener listener : listeners) {
            plugin.getServer().getPluginManager().registerEvents(listener, plugin);
        }
    }

    @Override
    public void onDisable(AbstractAlathranWars plugin) {
    }
}
