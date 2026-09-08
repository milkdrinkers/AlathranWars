package io.github.alathra.alathranwars.hook.ports;

import com.palmergames.bukkit.towny.object.Town;
import io.github.alathra.alathranwars.AbstractAlathranWars;
import io.github.alathra.alathranwars.AlathranWars;
import io.github.alathra.alathranwars.hook.AbstractHook;
import io.github.alathra.alathranwars.hook.Hook;
import io.github.alathra.alathraports.api.PortsAPI;
import io.github.alathra.alathraports.core.ports.Port;
import org.bukkit.event.Listener;

public class PortsHook extends AbstractHook implements Listener {
    public PortsHook(AlathranWars plugin) {
        super(plugin);
    }

    @Override
    public void onLoad(AbstractAlathranWars plugin) {
        if (!isPluginPresent(Hook.Ports.getPluginName())) {
        }
    }

    @Override
    public void onEnable(AbstractAlathranWars plugin) {
        if (!isPluginEnabled(Hook.Ports.getPluginName())) {
        }
    }

    @Override
    public void onDisable(AbstractAlathranWars plugin) {
        if (!isPluginEnabled(Hook.Ports.getPluginName())) {
        }
    }

    @Override
    public boolean isHookLoaded() {
        return isPluginPresent(Hook.Ports.getPluginName());
    }

    public void blockade(Town town) {
        try {
            final Port port = PortsAPI.getPortFromTown(town);
            if (port == null)
                return;

            PortsAPI.setBlockaded(port, true);
        } catch (Exception ignored) {
        }
    }

    public void unblockade(Town town) {
        try {
            final Port port = PortsAPI.getPortFromTown(town);
            if (port == null)
                return;

            PortsAPI.setBlockaded(port, false);
        } catch (Exception ignored) {
        }
    }
}
