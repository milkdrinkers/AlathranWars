package io.github.alathra.alathranwars.gui;

import dev.triumphteam.gui.builder.item.PaperItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import io.github.alathra.alathranwars.api.AlathranWarsAPIImpl;
import io.github.alathra.alathranwars.conflict.war.side.spawn.Spawn;
import io.github.alathra.alathranwars.deathspectate.DeathUtil;
import io.github.alathra.alathranwars.gui.customization.GuiButton;
import io.github.alathra.alathranwars.gui.customization.GuiPosition;
import io.github.alathra.alathranwars.utility.SpawnUtils;
import io.github.milkdrinkers.colorparser.paper.ColorParser;
import io.github.milkdrinkers.threadutil.Scheduler;
import io.github.milkdrinkers.wordweaver.Translation;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class SpawnSelectMenu {
    public static void open(Player p) {
        final PaginatedGui gui = Gui.paginated()
            .title(translate("alathranwars.gui.spawn-menu.title", p))
            .rows(6)
            .disableItemPlace()
            .disableItemSwap()
            .disableItemDrop()
            .disableItemTake()
            .disableOtherActions()
            .create();

        gui.setCloseGuiAction(e -> onClose(gui, e));

        populateDectoration(gui);
        populate(gui, p);

        gui.open(p);
    }

    public static void reload(PaginatedGui gui, Player p) {
        populate(gui, p);
    }

    public static void onClose(PaginatedGui gui, InventoryCloseEvent e) {
        if (e.getPlayer() instanceof Player p) {
            switch (e.getReason()) {
                case PLUGIN, DEATH, PLAYER, OPEN_NEW, CANT_USE, TELEPORT, UNKNOWN -> {
                    if (
                        p.isDead() || AlathranWarsAPIImpl.getInstance().isDead(p) || DeathUtil.isSpectating(p)
                    ) {
                        Scheduler.delay(1).sync(() -> gui.open(p)).execute();
                    }
                }
            }
        }
    }

    public static void populate(PaginatedGui gui, Player p) {
        gui.clearPageItems();

        // List populate
        PopulateContent.populate(gui, p);

        // Add populate
        PopulateButtons.populate(gui, p);

        gui.update();
    }

    public static void populateDectoration(PaginatedGui gui) {
        // Add border background
        GuiItem borderItem = PaperItemBuilder.from(Material.GRAY_STAINED_GLASS_PANE)
            .name(Component.empty())
            .asGuiItem();

        gui.getFiller().fillBorder(borderItem);
    }

    private final static class PopulateButtons {
        private static final GuiButton nextButton = GuiButton.of(GuiPosition.of(6, 6), Material.ARROW);
        private static final GuiButton prevButton = GuiButton.of(GuiPosition.of(4, 6), Material.ARROW);
        private static final GuiButton infoButton = GuiButton.of(GuiPosition.of(5, 6), Material.PAPER);

        private static final Sound SOUND_CLICK = Sound.sound(Key.key("ui.button.click"), Sound.Source.AMBIENT, 1.0f, 1.0f);
        private static final Sound SOUND_CLICK_FAIL = Sound.sound(Key.key("minecraft:block.end_portal_frame.fill"), Sound.Source.AMBIENT, 0.5f, 1.0F);

        /**
         * Add all buttons to the gui
         *
         * @param gui gui
         * @param p   player
         */
        public static void populate(PaginatedGui gui, Player p) {
            nextButton(gui, p);
            previousButton(gui, p);
            infoButton(gui, p);
        }

        /**
         * Add button to gui
         *
         * @param gui gui
         * @param p   player
         */
        private static void nextButton(PaginatedGui gui, Player p) {
            final ItemStack nextPage = nextButton.getItem();
            nextPage.editMeta(meta -> meta.customName(translate("alathranwars.gui.spawn-menu.next-page", p).decoration(TextDecoration.ITALIC, false)));
            gui.setItem(nextButton.getPosition().row(), nextButton.getPosition().col(), PaperItemBuilder.from(nextPage).asGuiItem(event -> {
                if (gui.next())
                    p.playSound(SOUND_CLICK);
                else
                    p.playSound(SOUND_CLICK_FAIL);
            }));
        }

        /**
         * Add button to gui
         *
         * @param gui gui
         * @param p   player
         */
        private static void previousButton(PaginatedGui gui, Player p) {
            ItemStack prevPage = prevButton.getItem();
            prevPage.editMeta(meta -> meta.customName(translate("alathranwars.gui.spawn-menu.previous-page", p).decoration(TextDecoration.ITALIC, false)));
            gui.setItem(prevButton.getPosition().row(), prevButton.getPosition().col(), PaperItemBuilder.from(prevPage).asGuiItem(event -> {
                if (gui.previous())
                    p.playSound(SOUND_CLICK);
                else
                    p.playSound(SOUND_CLICK_FAIL);
            }));
        }

        /**
         * Add button to gui
         *
         * @param gui gui
         * @param p   player
         */
        private static void infoButton(PaginatedGui gui, Player p) {
            final ItemStack info = infoButton.getItem();
            info.editMeta(meta -> {
                meta.customName(translate("alathranwars.gui.spawn-menu.info", p));
                meta.lore(translateList("alathranwars.gui.spawn-menu.info-lore", p));
            });
            gui.setItem(infoButton.getPosition().row(), infoButton.getPosition().col(), PaperItemBuilder.from(info).asGuiItem(event -> {
                p.playSound(SOUND_CLICK);
                SpawnSelectMenu.reload(gui, p);
            }));
        }
    }

    private final static class PopulateContent {
        private static final ItemStack rallyButton = new ItemStack(Material.RED_BANNER);
        private static final ItemStack townButton = new ItemStack(Material.GLOWSTONE);
        private static final ItemStack outpostButton = new ItemStack(Material.STONE_BRICKS);
        private static final ItemStack fallbackButton = new ItemStack(Material.STONE_BRICKS);

        private static final Sound SOUND_RESPAWN = Sound.sound(Key.key("entity.experience_orb.pickup"), Sound.Source.AMBIENT, 0.5f, 1.0F);
        private static final Sound SOUND_RESPAWN_DENY = Sound.sound(Key.key("entity.enderman.teleport"), Sound.Source.AMBIENT, 0.5f, 1.0F);

        /**
         * Add all spawns to the gui
         *
         * @param gui gui
         * @param p   player
         */
        public static void populate(PaginatedGui gui, Player p) {
            final List<Spawn> spawns = SpawnUtils.sortSpawns(SpawnUtils.getSpawns(p));

            spawns.forEach(spawn -> { // TODO Gray out distant spawns
                final ItemStack item;
                switch (spawn.getType()) {
                    case RALLY -> {
                        item = rallyButton.clone();
                        item.editMeta(meta -> meta.customName(spawnName("alathranwars.gui.spawn-menu.spawn-rally", spawn, p)));
                    }
                    case TOWN -> {
                        item = townButton.clone();
                        item.editMeta(meta -> meta.customName(spawnName("alathranwars.gui.spawn-menu.spawn-town", spawn, p)));
                    }
                    case OUTPOST -> {
                        item = outpostButton.clone();
                        item.editMeta(meta -> meta.customName(spawnName("alathranwars.gui.spawn-menu.spawn-outpost", spawn, p)));
                    }
                    default -> {
                        item = fallbackButton.clone();
                        item.editMeta(meta -> meta.customName(translate("alathranwars.gui.spawn-menu.spawn-rally")));
                    }
                }

                item.editMeta(meta -> {
                    meta.lore(
                        Translation.ofList("alathranwars.gui.spawn-menu.spawn-lore").stream()
                            .map(s -> ColorParser.of(s)
                                .papi(p)
                                .legacy()
                                .with("name", StringUtils.capitalize(spawn.getName()))
                                .with("type", StringUtils.capitalize(spawn.getType().name()))
                                .with("proxied", spawn.isProxied() ? Translation.as("alathranwars.gui.spawn-menu.spawn-disabled") : Translation.as("alathranwars.gui.spawn-menu.spawn-enabled"))
                                .with("last_proxied_minutes", String.valueOf(Duration.between(Instant.now(), spawn.getLastProxied()).abs().toMinutesPart()))
                                .with("last_proxied_seconds", String.valueOf(Duration.between(Instant.now(), spawn.getLastProxied()).abs().toSecondsPart()))
                                .with("x", String.valueOf(Math.round(spawn.getLocation().x())))
                                .with("y", String.valueOf(Math.round(spawn.getLocation().y())))
                                .with("z", String.valueOf(Math.round(spawn.getLocation().z())))
                                .build()
                            )
                            .toList()
                    );
                });

                if (spawn.isProxied()) {
                    // Enchant item
                    item.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    item.editMeta(meta -> meta.addEnchant(Enchantment.MENDING, 1, true));
                }

                // Add item to gui and handle click
                gui.addItem(PaperItemBuilder.from(item).asGuiItem(e -> onClick(e, gui, spawn, p)));
            });
        }

        /**
         * Handle click on spawn item
         *
         * @param e
         * @param gui   gui
         * @param spawn spawn
         * @param p     player
         */
        private static void onClick(InventoryClickEvent e, PaginatedGui gui, Spawn spawn, Player p) {
            // Reload on right-click
            if (e.isRightClick()) {
                reload(gui, p);
                return;
            }

            if (spawn.isProxied()) {
                onClickFail(gui, spawn, p);
            } else {
                onClickSuccess(gui, spawn, p);
            }
        }

        /**
         * On click success
         *
         * @param gui   gui
         * @param spawn spawn
         * @param p     player
         */
        private static void onClickSuccess(PaginatedGui gui, Spawn spawn, Player p) {
            gui.close(p, false);
            DeathUtil.respawnPlayerWar(p, spawn.getLocation());
            p.playSound(SOUND_RESPAWN);
        }

        /**
         * On click fail
         *
         * @param gui   gui
         * @param spawn spawn
         * @param p     player
         */
        private static void onClickFail(PaginatedGui gui, Spawn spawn, Player p) {
            SpawnSelectMenu.reload(gui, p);
            p.playSound(SOUND_RESPAWN_DENY);
        }
    }


    /**
     * Builds a spawn button's display name from the bundle.
     *
     * <p>Eagerly resolved rather than left as a translatable, because ItemMeta components are
     * serialized into the item and sent to the client untouched by GlobalTranslator.
     *
     * @param key   the bundle key
     * @param spawn the spawn the button represents
     * @param p     the viewer, for player scoped placeholders
     * @return the display name
     */
    private static Component spawnName(String key, Spawn spawn, Player p) {
        return ColorParser.of(Translation.of(key))
            .papi(p)
            .mini()
            .legacy()
            .with("name", spawn.getName())
            .with("type", StringUtils.capitalize(spawn.getType().name()))
            .build();
    }

    public static Component translate(String string) {
        return ColorParser.of(Translation.of(string))
            .papi()
            .mini()
            .legacy()
            .build();
    }

    public static Component translate(String string, Player p) {
        return ColorParser.of(Translation.of(string))
            .papi(p)
            .mini()
            .legacy()
            .build();
    }

    public static List<Component> translateList(String string) {
        return Translation.ofList(string).stream()
            .map(s -> ColorParser.of(s)
                .papi()
                .mini()
                .legacy()
                .build()
            )
            .toList();
    }

    public static List<Component> translateList(String string, Player p) {
        return Translation.ofList(string).stream()
            .map(s -> ColorParser.of(s)
                .papi(p)
                .mini(p)
                .legacy()
                .build()
            )
            .toList();
    }
}
