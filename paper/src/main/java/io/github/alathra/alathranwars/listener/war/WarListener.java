package io.github.alathra.alathranwars.listener.war;

import io.github.alathra.alathranwars.event.WarTimeStartedEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.translation.Argument;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.time.Duration;

public class WarListener implements Listener {
    @EventHandler
    public void onWarTimeStart(WarTimeStartedEvent e) {
        final Audience audience = Audience.audience(e.getWar().getPlayersOnlineAll());
        final Title warTitle = Title.title(
            Component.translatable("alathranwars.war.banner"),
            Component.translatable(
                "alathranwars.war.war-time-started.subtitle",
                Argument.string("name", e.getWar().getLabel())),
            Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(3500), Duration.ofMillis(500))
        );
        final Sound warSound = Sound.sound(Key.key("entity.wither.spawn"), Sound.Source.VOICE, 0.5f, 1.0F);
        audience.showTitle(warTitle);
        audience.playSound(warSound);

        Audience.audience(Audience.audience(Bukkit.getServer().getOnlinePlayers().stream().filter(p -> !e.getWar().getPlayersOnline().contains(p)).toList()), Bukkit.getServer().getConsoleSender())
            .sendMessage(Component.translatable(
                "alathranwars.war.war-time-started.broadcast",
                Argument.component("prefix", Component.translatable("alathranwars.prefix")),
                Argument.string("name", e.getWar().getLabel())));
    }

    //@EventHandler
    //public void onWarTimeEnd(WarTimeEndedEvent e) {
    // TODO Small notification about end of war time, battles will play out
    //}
}
