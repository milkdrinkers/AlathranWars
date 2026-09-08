package io.github.alathra.alathranwars.listener.battle.siege;

import io.github.alathra.alathranwars.api.AlathranWarsAPIImpl;
import io.github.alathra.alathranwars.utility.BattleUtils;
import io.github.alathra.alathranwars.utility.Cfg;
import io.github.milkdrinkers.itemutil.ItemUtils;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.UseCooldown;
import net.kyori.adventure.key.Key;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Optional;

@SuppressWarnings("unused")
public class PlayerTriggerSiegeListener implements Listener {
    private List<String> allowedItems = null; // Lazy-loaded list of allowed items

    private List<String> getAllowedItems() {
        if (allowedItems == null)
            allowedItems = Cfg.get().battles.sieges.trigger.items;
        return allowedItems;
    }

    @EventHandler
    public void onTrigger(PlayerInteractEvent e) {
        final Player p = e.getPlayer();

        if (!e.getAction().isRightClick())
            return;

        final ItemStack i = e.getItem();
        if (i == null || i.getType().isAir())
            return;

        final boolean hasValidItem = getAllowedItems().stream()
            .anyMatch(itemId -> ItemUtils.match(i, itemId));

        if (!hasValidItem)
            return;

        // Check if item has a cooldown
        if (i.hasData(DataComponentTypes.USE_COOLDOWN) || i.hasData(DataComponentTypes.INSTRUMENT)) {
            final Optional<Key> cooldownGroup = Optional.ofNullable(i.getData(DataComponentTypes.USE_COOLDOWN)).map(UseCooldown::cooldownGroup);
            final int cooldown = cooldownGroup.map(p::getCooldown).orElseGet(() -> p.getCooldown(i));

            if (cooldown > 0)
                return;
        }

        if (!BattleUtils.isCaptain(p))
            return;

        if (!AlathranWarsAPIImpl.getInstance().hasActiveWarTime(p))
            return;

        BattleUtils.attemptSiegeAtLocation(p);
    }

//    @EventHandler
//    public void onTrigger(RallyPlaceEvent e) {
//        final Player p = e.getRallyPoint().getCreator().getPlayer();
//
//        if (!BattleUtils.isCaptain(p))
//            return;
//
//        if (!AlathranWarsAPIImpl.getInstance().hasActiveWarTime(p))
//            return;
//
//        BattleUtils.attemptSiegeAtLocation(p);
//    }
}
