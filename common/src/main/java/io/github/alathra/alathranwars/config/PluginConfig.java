package io.github.alathra.alathranwars.config;

import io.github.alathra.alathranwars.config.exception.ConfigValidationException;
import io.github.alathra.alathranwars.config.migration.Migration;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.interfaces.meta.Exclude;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.List;
import java.util.Map;

/**
 * The main plugin configuration, mirroring {@code config.yml}.
 *
 * <p>Field names are the keys. Configurate derives kebab-case YAML keys from camelCase field
 * names, so {@code updateChecker} is {@code update-checker} and {@code requiredPlaytime} is
 * {@code required-playtime}. A field whose derived key does not match the file silently loads
 * its own default instead, so renaming a field here is a config-format change.
 */
@ConfigSerializable
public class PluginConfig implements VersionedConfig {
    @Comment("Do not change this value!")
    public int configVersion = 2;

    @Override
    @Exclude
    public int configVersion() {
        return configVersion;
    }

    @Override
    @Exclude
    public @NotNull Map<Integer, Migration> migrations() {
        return Map.of(
            2, Migration.builder()
                .withTransform(root -> {
                    final ConfigurationNode stray = root.node("colors", "nametags", "hostile");
                    if (stray.virtual() || stray.raw() == null)
                        return;

                    final ConfigurationNode destination = root.node("colors", "nametags", "prefix", "hostile");
                    if (destination.raw() == null)
                        destination.set(stray.raw());

                    stray.raw(null);
                })
                .build()
        );
    }

    @Override
    @Exclude
    public void validate() throws ConfigValidationException {
        if (battles.sieges.duration <= 0)
            throw new ConfigValidationException(
                "battles.sieges.duration (" + battles.sieges.duration + ") must be greater than zero, " +
                    "or every siege would end immediately"
            );

        if (respawns.time < 0)
            throw new ConfigValidationException(
                "respawns.time (" + respawns.time + ") must not be negative"
            );

        if (war.wartimeDuration <= 0)
            throw new ConfigValidationException(
                "war.wartime-duration (" + war.wartimeDuration + ") must be greater than zero"
            );
    }

    @Comment("Update Checker Settings")
    public UpdateChecker updateChecker = new UpdateChecker();

    @ConfigSerializable
    public static class UpdateChecker {
        public boolean enable = true;

        @Comment("Post message to console")
        public boolean console = true;

        @Comment("Send update notification to opped players on join")
        public boolean op = true;
    }

    @Comment("Language, specify the language file to use, for example `en_US` which will load `/lang/en_US.json`")
    public String language = "en_US";

    public Cooldowns cooldowns = new Cooldowns();

    @ConfigSerializable
    public static class Cooldowns {
        public WarCooldowns war = new WarCooldowns();

        @ConfigSerializable
        public static class WarCooldowns {
            public CooldownGroup government = new CooldownGroup();
            public CooldownGroup resident = new CooldownGroup();
        }

        @ConfigSerializable
        public static class CooldownGroup {
            public Offensive offensive = new Offensive();
            public Defensive defensive = new Defensive();

            @ConfigSerializable
            public static class Offensive {
                @Setting("default")
                @Comment("Days")
                public int defaultDays = 14;

                @Comment("Days")
                public int mercenary = 30;
            }

            @ConfigSerializable
            public static class Defensive {
                @Setting("default")
                @Comment("Days")
                public int defaultDays = 7;
            }
        }
    }

    public War war = new War();

    @ConfigSerializable
    public static class War {
        @Comment("Minutes (Does not apply to force-joined players)")
        public int requiredPlaytime = 60;

        @Comment("Duration of wartime in minutes")
        public int wartimeDuration = 120;
    }

    public Battles battles = new Battles();

    @ConfigSerializable
    public static class Battles {
        @Comment("Worlds players may teleport from (prevents getting stuck in lobby worlds) (usage of t spawn etc)")
        public List<String> teleportWorldWhitelist = List.of("spawn");

        public Sieges sieges = new Sieges();

        @ConfigSerializable
        public static class Sieges {
            @Comment("The radius in blocks around the siege point that will be considered the battlefield")
            public int range = 500;

            @Comment("The duration of the siege in minutes")
            public int duration = 60;

            public Trigger trigger = new Trigger();

            public Points points = new Points();

            @ConfigSerializable
            public static class Trigger {
                @Comment("The range in blocks around the siege point that a siege can be triggered from")
                public int range = 250;

                @Comment("When these items are used in proximity to a town by a war-leader or captain they will trigger a siege for the closest town")
                public List<String> items = List.of("minecraft:goat_horn");
            }

            @ConfigSerializable
            public static class Points {
                @Comment("Given to attackers if they win")
                public int attackerVictory = 50;

                @Comment("Given to defenders if they lose")
                public int defenderLoss = 5;

                @Comment("Given to attackers if they lose")
                public int attackerLoss = 1;

                @Comment("Given to defenders if they win")
                public int defenderVictory = 10;
            }
        }
    }

    public Respawns respawns = new Respawns();

    @ConfigSerializable
    public static class Respawns {
        @Comment("Seconds a player must wait before respawning")
        public int time = 15;

        @Comment("If users should need the \"alathranwars.death.spectate\" permission to enable respawns")
        public boolean permission = false;

        @Comment("Worlds to enable respawning from (Leave empty to disable)")
        public List<String> whitelistedWorlds = List.of();

        @Comment("Commands allowed to be run while respawning (Leave empty to disable)")
        public List<String> whitelistedCommands = List.of();

        @Comment("Causes that will not trigger respawning (Leave empty to disable)\n(https://jd.papermc.io/paper/org/bukkit/event/entity/EntityDamageEvent.DamageCause.html)")
        public List<String> blacklistedDamageCauses = List.of();

        public Proxy proxy = new Proxy();

        @ConfigSerializable
        public static class Proxy {
            @Comment("Time in seconds it takes for a spawn to re-enable after being proxied")
            public int reenableTime = 10;

            @Comment("Required players to proxy a spawn")
            public int minPlayers = 1;

            @Comment("The range around a spawn point to check for proxying players")
            public int range = 8;
        }

        @Comment("Settings related to rally points (Placeable spawn points)")
        public Rallies rallies = new Rallies();

        @ConfigSerializable
        public static class Rallies {
            @Comment("The cooldown in minutes a player must wait before being able to place another rally")
            public int placementCooldown = 5;

            @Comment("Time in seconds it takes for a rally to be removed if proxied")
            public int proxyDelete = 60;

            @Comment("The ranges in blocks rally points need to be away from other types of spawns")
            public int minRangeTownFriendly = 150;

            public int minRangeOutpostFriendly = 100;

            public int minRangeRallyFriendly = 120;

            public int minRangeTownHostile = 100;

            public int minRangeOutpostHostile = 50;

            public int minRangeRallyHostile = 25;
        }
    }

    public Colors colors = new Colors();

    @ConfigSerializable
    public static class Colors {
        public Particles particles = new Particles();

        public Nametags nametags = new Nametags();

        @ConfigSerializable
        public static class Particles {
            public String friendly = "#5555FF";
            public String neutral = "#FFFF55";
            public String hostile = "#FF5555";
        }

        @ConfigSerializable
        public static class Nametags {
            public Prefix prefix = new Prefix();

            public Suffix suffix = new Suffix();

            @ConfigSerializable
            public static class Prefix {
                public String friendly = "<blue>";
                public String neutral = "<yellow>";
                public String hostile = "<red>";
            }

            @ConfigSerializable
            public static class Suffix {
                public String friendly = "</blue>";
                public String neutral = "</yellow>";
                public String hostile = "</red>";
            }
        }
    }
}
