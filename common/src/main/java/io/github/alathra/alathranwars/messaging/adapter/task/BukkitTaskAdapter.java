package io.github.alathra.alathranwars.messaging.adapter.task;

import io.github.alathra.alathranwars.AbstractAlathranWars;
import org.bukkit.Bukkit;

import java.util.concurrent.TimeUnit;

/**
 * Bukkit platform specific task runner implementation.
 */
public class BukkitTaskAdapter implements TaskAdapter {
    public BukkitTaskAdapter() {
    }

    @Override
    public void init(Runnable runnable, long delay, long interval, TimeUnit timeUnit) {
        Bukkit.getAsyncScheduler().runAtFixedRate(AbstractAlathranWars.getInstance(), (task) -> runnable.run(), delay, interval, timeUnit);
    }

    @Override
    public void cancel() {
        Bukkit.getAsyncScheduler().cancelTasks(AbstractAlathranWars.getInstance());
    }
}
