package io.github.alathra.alathranwars.hook.activeupkeep;

import io.github.alathra.activeupkeep.api.ActiveUpkeepAPI;
import io.github.alathra.activeupkeep.core.UpkeepProfile;
import io.github.alathra.alathranwars.AbstractAlathranWars;
import io.github.alathra.alathranwars.AlathranWars;
import io.github.alathra.alathranwars.hook.AbstractHook;
import io.github.alathra.alathranwars.hook.Hook;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.concurrent.ExecutionException;

public class ActiveUpkeepHook extends AbstractHook implements Listener {
    public ActiveUpkeepHook(AlathranWars plugin) {
        super(plugin);
    }

    @Override
    public void onLoad(AbstractAlathranWars plugin) {
        if (!isPluginPresent(Hook.ActiveUpkeep.getPluginName())) {
        }
    }

    @Override
    public void onEnable(AbstractAlathranWars plugin) {
        if (!isPluginEnabled(Hook.ActiveUpkeep.getPluginName())) {
        }
    }

    @Override
    public void onDisable(AbstractAlathranWars plugin) {
        if (!isPluginEnabled(Hook.ActiveUpkeep.getPluginName())) {
        }
    }

    @Override
    public boolean isHookLoaded() {
        return isPluginPresent(Hook.ActiveUpkeep.getPluginName());
    }

    public boolean hasRequiredPlaytime(Player p) {
        try {
            return ActiveUpkeepAPI.getUpkeepProfile(p).get().map(UpkeepProfile::isWarEligible).orElse(false);
        } catch (InterruptedException | ExecutionException e) {
            return false;
        }
    }
}
