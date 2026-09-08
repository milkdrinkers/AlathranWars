import net.minecrell.pluginyml.paper.PaperPluginDescription

plugins {
    alias(libs.plugins.shadow) // Shades and relocates dependencies, see https://gradleup.com/shadow/
    alias(libs.plugins.run.paper) // Built in test server using runServer task
    alias(libs.plugins.plugin.yml.bukkit) // Automatic plugin.yml generation
    alias(libs.plugins.plugin.yml.paper) // Automatic plugin.yml generation
}

val cleanAlathraPorts by tasks.registering(Jar::class) {
    from(zipTree("lib/AlathraPorts-1.0.5.jar")) {
        exclude("io/github/milkdrinkers/**")
    }
    archiveFileName.set("AlathraPorts-1.0.5-cleaned.jar")
    destinationDirectory.set(layout.buildDirectory.dir("cleaned-libs"))
}

val cleanActiveUpkeep by tasks.registering(Jar::class) {
    from(zipTree("lib/ActiveUpkeep-1.0.0-SNAPSHOT-1754074990.jar")) {
        exclude("io/github/milkdrinkers/**")
    }
    archiveFileName.set("ActiveUpkeep-1.0.0-SNAPSHOT-1754074990.jar")
    destinationDirectory.set(layout.buildDirectory.dir("cleaned-libs"))
}

dependencies {
    // Core dependencies
    implementation(projects.common)
    implementation(libs.morepaperlib)

    // API
    implementation(libs.javasemver)
    implementation(libs.versionwatch)
    implementation(libs.bundles.wordweaver)
    api(libs.colorparser.paper) {
        exclude("net.kyori")
    }
    implementation(libs.commandapi.shade.paper)
    implementation(libs.triumph.gui)
    implementation(libs.itemutils)

    // Plugin Dependencies
    implementation(libs.bstats)
    compileOnly(libs.vault)
    compileOnly(libs.packetevents)
    implementation(libs.entitylib) {
        exclude("org.jetbrains")
    }
    compileOnly(libs.placeholderapi) {
        exclude("me.clip.placeholderapi.libs", "kyori")
    }
    compileOnly(libs.towny) {
        exclude("com.palmergames.adventure")
    }
    compileOnly(libs.tabapi)
    compileOnly(files(cleanAlathraPorts))
    compileOnly(files(cleanActiveUpkeep))
    compileOnly(files("lib/Graves-4.9.jar"))
    compileOnly(files("lib/GSit-2.4.3.jar"))
    compileOnly(files("lib/HeadDrop.jar"))
    compileOnly(libs.headsplus)
    compileOnly(libs.essentialsx.core) {
        exclude("org.spigotmc")
    }
    compileOnly(libs.essentialsx.spawn) {
        exclude("org.spigotmc")
    }

    // Database dependencies - Core
    library(libs.bundles.flyway)
    library(libs.jooq)

    // Database dependencies - JDBC drivers
    library(libs.bundles.jdbcdrivers)

    // Messaging service clients
    library(libs.bundles.messagingclients)

    // Testing - Core
    testImplementation(libs.annotations)
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.junit)
    testRuntimeOnly(libs.slf4j)
    testImplementation(platform(libs.testcontainers.bom))
    testImplementation(libs.bundles.testcontainers)
    testImplementation(libs.paper.api)

    // Testing - Database dependencies
    testImplementation(libs.hikaricp)
    testImplementation(libs.bundles.flyway)
    testImplementation(libs.jooq)

    // Testing - JDBC drivers
    testImplementation(libs.bundles.jdbcdrivers)
}

tasks {
    build {
        dependsOn(shadowJar)
    }

    jar {
        enabled = false
    }

    shadowJar {
        archiveBaseName.set(rootProject.name)
        archiveClassifier.set("")

        // Shadow classes
        fun reloc(originPkg: String, targetPkg: String) = relocate(originPkg, "${project.relocationPackage}.${targetPkg}")

        reloc("space.arim.morepaperlib", "morepaperlib")
        reloc("io.github.milkdrinkers", "milkdrinkers")
        reloc("org.snakeyaml", "snakeyaml")
        reloc("org.json", "json")
        reloc("dev.jorel.commandapi", "commandapi")
        reloc("com.zaxxer.hikari", "hikaricp")
        reloc("org.bstats", "bstats")
        reloc("me.tofaa.entitylib", "entitylib")
        reloc("dev.triumphteam.gui", "triumphgui")

        reloc("io.leangen.geantyref", "geantyref")
        reloc("org.yaml", "yaml")
        reloc("org.spongepowered", "spongepowered")

        mergeServiceFiles()
        filesMatching("META-INF/services/**") {
            duplicatesStrategy = DuplicatesStrategy.INCLUDE
        }
    }

    runServer {
        minecraftVersion(libs.versions.paper.run.get())

        // IntelliJ IDEA debugger setup: https://docs.papermc.io/paper/dev/debugging#using-a-remote-debugger
        jvmArgs("-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005", "-DPaper.IgnoreJavaVersion=true", "-Dcom.mojang.eula.agree=true", "-DIReallyKnowWhatIAmDoingISwear", "-Dpaper.playerconnection.keepalive=6000")
        systemProperty("terminal.jline", false)
        systemProperty("terminal.ansi", true)

        downloadPlugins {
            github("MilkBowl", "Vault", "1.7.3", "Vault.jar")
            modrinth("towny", "0.103.2.0")
            modrinth("tab-was-taken", "4.1.8")
            modrinth("PlaceholderAPI", "2.12.2")
            github("retrooper", "packetevents", "v2.13.0", "packetevents-spigot-2.13.0.jar")
        }
    }
}

bukkit { // Options: https://docs.eldoria.de/pluginyml/bukkit/
    // Plugin main class (required)
    main = rootProject.entryPointClass

    // Plugin Information
    name = rootProject.name
    prefix = rootProject.name
    version = "${rootProject.version}"
    description = "${rootProject.description}"
    authors = rootProject.authors
    contributors = rootProject.contributors
    apiVersion = libs.versions.paper.api.get().substringBefore("-R").substringBefore("-pre")
    foliaSupported = false

    // Misc properties
    load = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.PluginLoadOrder.POSTWORLD // STARTUP or POSTWORLD
    depend = listOf("Vault", "Towny")
    softDepend = listOf("PacketEvents", "PlaceholderAPI", "AlathraPorts", "GravesX", "Graves", "TAB", "UnlimitedNametags", "Skulls", "HeadsPlus", "HeadDatabase", "HeadDrop", "Essentials")
    loadBefore = listOf()
    provides = listOf()
}

paper { // Options: https://docs.eldoria.de/pluginyml/paper/
    main = rootProject.entryPointClass
    loader = rootProject.entryPointClass + "PluginLoader"
    generateLibrariesJson = true
    load = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.PluginLoadOrder.POSTWORLD

    // Info
    name = rootProject.name
    prefix = rootProject.name
    version = "${rootProject.version}"
    description = "${rootProject.description}"
    authors = rootProject.authors
    contributors = rootProject.contributors
    apiVersion = libs.versions.paper.api.get().substringBefore("-R").substringBefore("-pre")
    foliaSupported = false

    // Dependencies
    hasOpenClassloader = true
    bootstrapDependencies {}
    serverDependencies {
        // Hard depends
        register("Vault") {
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
        }
        register("Towny") {
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
        }

        // Soft depends
        register("PacketEvents") {
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
            required = false
        }
        register("PlaceholderAPI") {
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
            required = false
        }
        register("AlathraPorts") {
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
            required = false
        }
        register("GravesX") {
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
            required = false
        }
        register("Graves") {
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
            required = false
        }
        register("TAB") {
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
            required = false
        }
        register("UnlimitedNametags") {
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
            required = false
        }
        register("Skulls") {
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
            required = false
        }
        register("HeadsPlus") {
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
            required = false
        }
        register("HeadDatabase") {
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
            required = false
        }
        register("HeadDrop") {
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
            required = false
        }
        register("Essentials") {
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
            required = false
        }
    }
    provides = listOf()
}

