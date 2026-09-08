package io.github.alathra.alathranwars.hook.unlimitednametags;

import io.github.alathra.alathranwars.AbstractAlathranWars;
import io.github.alathra.alathranwars.AlathranWars;
import io.github.alathra.alathranwars.hook.AbstractHook;
import io.github.alathra.alathranwars.hook.Hook;

public class UnlimitedNametagsHook extends AbstractHook {
    public UnlimitedNametagsHook(AlathranWars plugin) {
        super(plugin);
    }

    @Override
    public void onLoad(AbstractAlathranWars plugin) {
        if (!isPluginPresent(Hook.UnlimitedNametags.getPluginName())) {
        }
    }

    @Override
    public void onEnable(AbstractAlathranWars plugin) {
        if (!isPluginEnabled(Hook.UnlimitedNametags.getPluginName())) {
        }
    }

    @Override
    public void onDisable(AbstractAlathranWars plugin) {
        if (!isPluginEnabled(Hook.UnlimitedNametags.getPluginName())) {
        }
    }

    @Override
    public boolean isHookLoaded() {
        return isPluginPresent(Hook.UnlimitedNametags.getPluginName());
    }
}
