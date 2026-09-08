package io.github.alathra.alathranwars.hook.essentials;

import io.github.alathra.alathranwars.AbstractAlathranWars;
import io.github.alathra.alathranwars.AlathranWars;
import io.github.alathra.alathranwars.conflict.war.WarController;
import io.github.alathra.alathranwars.hook.AbstractHook;
import io.github.alathra.alathranwars.hook.Hook;
import net.ess3.api.events.TPARequestEvent;
import net.ess3.api.events.UserRandomTeleportEvent;
import net.ess3.api.events.UserTeleportHomeEvent;
import net.ess3.api.events.UserWarpEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

public class EssentialsXHook extends AbstractHook implements Listener {
    public EssentialsXHook(AlathranWars plugin) {
        super(plugin);
    }

    @Override
    public void onLoad(AbstractAlathranWars plugin) {
        if (!isPluginPresent(Hook.Essentials.getPluginName())) {
        }
    }

    @Override
    public void onEnable(AbstractAlathranWars plugin) {
        if (!isPluginEnabled(Hook.Essentials.getPluginName())) {
        }
    }

    @Override
    public void onDisable(AbstractAlathranWars plugin) {
        if (!isPluginEnabled(Hook.Essentials.getPluginName())) {
        }
    }

    @Override
    public boolean isHookLoaded() {
        return isPluginPresent(Hook.Essentials.getPluginName());
    }

    @SuppressWarnings("unused")
    @EventHandler
    public void onTPACommand(@NotNull TPARequestEvent e) {
        if (!e.getRequester().isPlayer())
            return;

        final boolean isTeleportHere = e.isTeleportHere();

        final Player origin = !isTeleportHere ? e.getRequester().getPlayer() : e.getTarget().getBase(); // The teleported
        final Player target = !isTeleportHere ? e.getTarget().getBase() : e.getRequester().getPlayer(); // The destination

        if (WarController.getInstance().isInActiveWar(origin)) {
            origin.showTitle(
                Title.title(
                    Component.translatable("alathranwars.war.banner"),
                    Component.translatable("alathranwars.teleport.war-time"),
                    Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(3500), Duration.ofMillis(500))
                )
            );
            e.setCancelled(true);
        }
        if (WarController.getInstance().isInActiveWar(target)) {
            target.showTitle(
                Title.title(
                    Component.translatable("alathranwars.war.banner"),
                    Component.translatable("alathranwars.teleport.war-time"),
                    Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(3500), Duration.ofMillis(500))
                )
            );
            e.setCancelled(true);
        }
    }

    @SuppressWarnings("unused")
    @EventHandler
    public void onHomeCommand(@NotNull UserTeleportHomeEvent e) {
        Player p = e.getUser().getBase();

        if (WarController.getInstance().isInActiveWar(p)) {
            p.showTitle(
                Title.title(
                    Component.translatable("alathranwars.war.banner"),
                    Component.translatable("alathranwars.teleport.war-time"),
                    Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(3500), Duration.ofMillis(500))
                )
            );
            e.setCancelled(true);
        }
    }

    @SuppressWarnings("unused")
    @EventHandler
    public void onWarpCommand(@NotNull UserWarpEvent e) {
        Player p = e.getUser().getBase();

        if (WarController.getInstance().isInActiveWar(p)) {
            p.showTitle(
                Title.title(
                    Component.translatable("alathranwars.war.banner"),
                    Component.translatable("alathranwars.teleport.war-time"),
                    Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(3500), Duration.ofMillis(500))
                )
            );
            e.setCancelled(true);
        }
    }

//    @SuppressWarnings("unused")
//    @EventHandler
//    public void onSpawnCommand(@NotNull UserTeleportSpawnEvent e) {
//        Player p = e.getUser().getBase();
//
//        if (WarController.getInstance().isInActiveWar(p)) {
//            p.showTitle(
//                Title.title(
//                    ColorParser.of("<gradient:#D72A09:#B01F03><u><b>War").build(),
//                    ColorParser.of("<gray><i>You cannot teleport during war time!").build(),
//                    Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(3500), Duration.ofMillis(500))
//                )
//            );
//            e.setCancelled(true);
//        }
//    }

    @SuppressWarnings("unused")
    @EventHandler
    public void onRTPCommand(@NotNull UserRandomTeleportEvent e) {
        Player p = e.getUser().getBase();

        if (WarController.getInstance().isInActiveWar(p)) {
            p.showTitle(
                Title.title(
                    Component.translatable("alathranwars.war.banner"),
                    Component.translatable("alathranwars.teleport.war-time"),
                    Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(3500), Duration.ofMillis(500))
                )
            );
            e.setCancelled(true);
        }
    }
}
