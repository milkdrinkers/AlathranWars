package io.github.alathra.alathranwars.translation;

import io.github.alathra.alathranwars.AbstractAlathranWars;
import io.github.alathra.alathranwars.Reloadable;
import io.github.alathra.alathranwars.config.ConfigHandler;
import io.github.milkdrinkers.colorparser.common.tag.CustomTags;
import io.github.milkdrinkers.colorparser.paper.ColorParser;
import io.github.milkdrinkers.wordweaver.Translation;
import io.github.milkdrinkers.wordweaver.config.TranslationConfig;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;

import java.nio.file.Path;
import java.util.Locale;

/**
 * A wrapper handler class for handling WordWeaver lifecycle.
 */
public class TranslationHandler implements Reloadable {
    private final ConfigHandler configHandler;

    public TranslationHandler(ConfigHandler configHandler) {
        this.configHandler = configHandler;
    }

    @Override
    public void onEnable(AbstractAlathranWars plugin) {
        Translation.initialize(buildConfig(
            plugin.getName(),
            plugin.getDataPath().resolve("lang"),
            configHandler.getConfig().language
        ));
    }

    /**
     * Builds the WordWeaver config.
     *
     * <p>Split out of {@link #onEnable} so it can be built without a running server.
     * {@link TranslationConfig.Builder#build()} enforces required fields at runtime rather than at
     * compile time, so a missing one only shows up when a server starts.
     *
     * @param pluginName           the plugin name, used to derive the namespace
     * @param translationDirectory where language files live on disk
     * @param locale               the configured locale
     * @return a validated translation config
     */
    @VisibleForTesting
    static @NotNull TranslationConfig buildConfig(
        @NotNull String pluginName,
        @NotNull Path translationDirectory,
        @NotNull String locale
    ) {
        return TranslationConfig.builder()
            .namespace("wordweaver:" + pluginName.toLowerCase(Locale.ROOT))
            .translationDirectory(translationDirectory)
            .resourcesDirectory(Path.of("lang"))
            .extractBundles(true)
            .updateBundles(true)
            .locale(locale)
            .defaultLocale("en_US")
            .componentConverter(s -> ColorParser.of(s).papi().mini().build())
            .miniMessage(MiniMessage.builder()
                .editTags(builder -> builder.resolver(CustomTags.defaults()))
                .build())
            .build();
    }
}
