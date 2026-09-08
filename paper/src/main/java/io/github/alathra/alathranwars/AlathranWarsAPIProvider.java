package io.github.alathra.alathranwars;

import io.github.alathra.alathranwars.api.AlathranWarsAPIImpl;

class AlathranWarsAPIProvider extends AlathranWarsAPIImpl implements Reloadable {
    @SuppressWarnings("unused")
    private final AlathranWars instance;

    public AlathranWarsAPIProvider(AlathranWars instance) {
        this.instance = instance;
        setInstance(this);
    }
}
