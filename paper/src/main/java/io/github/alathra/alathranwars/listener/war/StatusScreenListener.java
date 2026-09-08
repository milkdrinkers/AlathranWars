package io.github.alathra.alathranwars.listener.war;

import com.palmergames.bukkit.towny.event.statusscreen.NationStatusScreenEvent;
import com.palmergames.bukkit.towny.event.statusscreen.ResidentStatusScreenEvent;
import com.palmergames.bukkit.towny.event.statusscreen.TownStatusScreenEvent;
import io.github.alathra.alathranwars.api.AlathranWarsAPIImpl;
import io.github.alathra.alathranwars.utility.TimeTagResolversTowny;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.translation.Argument;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.time.Duration;

@SuppressWarnings("unused")
public class StatusScreenListener implements Listener {
    @EventHandler
    public void onStatusScreen(ResidentStatusScreenEvent e) {
        final boolean hasAttackCooldown = AlathranWarsAPIImpl.getInstance().hasAttackCooldown(e.getResident());
        final boolean hasDefenseCooldown = AlathranWarsAPIImpl.getInstance().hasDefenseCooldown(e.getResident());
        final Duration attackCooldown = AlathranWarsAPIImpl.getInstance().getAttackCooldown(e.getResident());
        final Duration defenseCooldown = AlathranWarsAPIImpl.getInstance().getDefenseCooldown(e.getResident());

        final Component inactive = Component.translatable("alathranwars.status-screen.cooldown-inactive");

        e.getStatusScreen().addComponentOf("cooldown",
            Component.translatable(
                "alathranwars.status-screen.cooldown",
                Argument.tagResolver(TagResolver.resolver(
                    TagResolver.resolver("cooldown_attack", Tag.selfClosingInserting(
                        hasAttackCooldown ? getCooldownRemaining(attackCooldown) : inactive
                    )),
                    TagResolver.resolver("cooldown_defense", Tag.selfClosingInserting(
                        hasDefenseCooldown ? getCooldownRemaining(defenseCooldown) : inactive
                    ))
                ))
            )
        );
    }

    @EventHandler
    public void onStatusScreen(TownStatusScreenEvent e) {
        final boolean hasAttackCooldown = AlathranWarsAPIImpl.getInstance().hasAttackCooldown(e.getTown());
        final boolean hasDefenseCooldown = AlathranWarsAPIImpl.getInstance().hasDefenseCooldown(e.getTown());
        final Duration attackCooldown = AlathranWarsAPIImpl.getInstance().getAttackCooldown(e.getTown());
        final Duration defenseCooldown = AlathranWarsAPIImpl.getInstance().getDefenseCooldown(e.getTown());

        final Component inactive = Component.translatable("alathranwars.status-screen.cooldown-inactive");

        e.getStatusScreen().addComponentOf("cooldown",
            Component.translatable(
                "alathranwars.status-screen.cooldown",
                Argument.tagResolver(TagResolver.resolver(
                    TagResolver.resolver("cooldown_attack", Tag.selfClosingInserting(
                        hasAttackCooldown ? getCooldownRemaining(attackCooldown) : inactive
                    )),
                    TagResolver.resolver("cooldown_defense", Tag.selfClosingInserting(
                        hasDefenseCooldown ? getCooldownRemaining(defenseCooldown) : inactive
                    ))
                ))
            )
        );

        if (AlathranWarsAPIImpl.getInstance().isMercenary(e.getTown())) {
            e.getStatusScreen().addComponentOf("mercenary",
                Component.translatable("alathranwars.status-screen.mercenary")
            );
        }
    }

    @EventHandler
    public void onStatusScreen(NationStatusScreenEvent e) {
        final boolean hasAttackCooldown = AlathranWarsAPIImpl.getInstance().hasAttackCooldown(e.getNation());
        final boolean hasDefenseCooldown = AlathranWarsAPIImpl.getInstance().hasDefenseCooldown(e.getNation());
        final Duration attackCooldown = AlathranWarsAPIImpl.getInstance().getAttackCooldown(e.getNation());
        final Duration defenseCooldown = AlathranWarsAPIImpl.getInstance().getDefenseCooldown(e.getNation());

        final Component inactive = Component.translatable("alathranwars.status-screen.cooldown-inactive");

        e.getStatusScreen().addComponentOf("cooldown",
            Component.translatable(
                "alathranwars.status-screen.cooldown",
                Argument.tagResolver(TagResolver.resolver(
                    TagResolver.resolver("cooldown_attack", Tag.selfClosingInserting(
                        hasAttackCooldown ? getCooldownRemaining(attackCooldown) : inactive
                    )),
                    TagResolver.resolver("cooldown_defense", Tag.selfClosingInserting(
                        hasDefenseCooldown ? getCooldownRemaining(defenseCooldown) : inactive
                    ))
                ))
            )
        );
    }

    private Component getCooldownRemaining(Duration duration) {
        return Component.translatable(
            "alathranwars.status-screen.cooldown-active-format",
            Argument.tagResolver(TimeTagResolversTowny.tag(duration))
        );
    }
}
