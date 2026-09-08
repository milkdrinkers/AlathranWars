package io.github.alathra.alathranwars.hook.settlers;

import io.github.alathra.alathranwars.AbstractAlathranWars;
import io.github.alathra.alathranwars.AlathranWars;
import io.github.alathra.alathranwars.hook.AbstractHook;
import io.github.alathra.alathranwars.hook.Hook;

public class SettlersHook extends AbstractHook {
    public SettlersHook(AlathranWars plugin) {
        super(plugin);
    }

    @Override
    public void onLoad(AbstractAlathranWars plugin) {
        if (!isPluginPresent(Hook.Settlers.getPluginName())) {
        }
    }

    @Override
    public void onEnable(AbstractAlathranWars plugin) {
        if (!isPluginEnabled(Hook.Settlers.getPluginName())) {
        }
    }

    @Override
    public void onDisable(AbstractAlathranWars plugin) {
        if (!isPluginEnabled(Hook.Settlers.getPluginName())) {
        }
    }

    @Override
    public boolean isHookLoaded() {
        return isPluginPresent(Hook.Settlers.getPluginName());
    }
}
