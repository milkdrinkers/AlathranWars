import versioning.VersioningPlugin

plugins {
    `java-library`

    projectextensions
    versioning

    eclipse
    idea
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(libs.versions.java.get().toInt()))
}

tasks {
    jar {
        enabled = false
    }
}

subprojects {
    apply<JavaLibraryPlugin>()
    apply<ProjectExtensionsPlugin>()
    apply<VersioningPlugin>()

    project.version = rootProject.version
    project.description = rootProject.description

    base.archivesName.set("${rootProject.name}-${project.name}")

    java {
        toolchain.languageVersion.set(JavaLanguageVersion.of(rootProject.libs.versions.java.get().toInt()))
        withJavadocJar() // Enable javadoc jar generation
        withSourcesJar() // Enable sources jar generation
    }

    repositories {
        mavenCentral()
        maven("https://central.sonatype.com/repository/maven-snapshots/")

        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://mvn-repo.arim.space/lesser-gpl3/")

        maven("https://repo.glaremasters.me/repository/towny/") {
            content { includeGroup("com.palmergames.bukkit.towny") }
        }

        maven("https://jitpack.io/") {
            content {
                includeGroup("com.github.MilkBowl") // VaultAPI
                includeGroup("com.palmergames.bukkit.towny")
                includeGroup("com.github.gecolay")
                includeGroup("com.github.gecolay.GSit")
                includeGroup("com.github.NEZNAMY")
                includeGroup("com.github.milkdrinkers")
                includeGroup("ca.tweetzy")
                includeGroup("com.github.Thatsmusic99")
            }
        }

        maven("https://repo.codemc.org/repository/maven-public/") {
            content {
                includeGroup("dev.jorel")
                includeGroup("com.github.retrooper")
            }
        }

        maven("https://repo.extendedclip.com/content/repositories/placeholderapi/") {
            content { includeGroup("me.clip") }
        }

        maven("https://repo.essentialsx.net/releases/")
        maven("https://repo.essentialsx.net/snapshots/")
        maven("https://repo.dmulloy2.net/repository/public/") // ProtocolLib
        maven("https://maven.pvphub.me/tofaa") { // EntityLib
            content { includeGroup("io.github.tofaa2") }
        }
        maven("https://repo.cwhead.dev/repository/maven-public/") {
            content { includeGroup("com.ranull") }
        }
        maven("https://repo.triumphteam.dev/snapshots/") {
            content { includeGroup("dev.triumphteam") }
        }

        maven("https://repo.opencollab.dev/maven-snapshots/") {
            content { includeGroup("org.spongepowered") }
        }
    }

    dependencies {
        compileOnly(rootProject.libs.annotations)
        annotationProcessor(rootProject.libs.annotations)

        compileOnly(rootProject.libs.paper.api)
    }

    tasks {
        compileJava {
            options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything

            // Set the release flag. This configures what version bytecode the compiler will emit, as well as what JDK APIs are usable.
            // See https://openjdk.java.net/jeps/247 for more information.
            options.release.set(rootProject.libs.versions.java.get().toInt())
            options.compilerArgs.addAll(listOf("-Xlint:all", "-Xlint:-processing", "-Xdiags:verbose"))
        }

        javadoc {
            isFailOnError = false
            val options = options as StandardJavadocDocletOptions
            options.encoding = Charsets.UTF_8.name()
            options.windowTitle = "${rootProject.name} Javadoc"
            options.tags("apiNote:a:API Note:", "implNote:a:Implementation Note:", "implSpec:a:Implementation Requirements:")
            options.addStringOption("Xdoclint:none", "-quiet")
            options.use()
        }

        processResources {
            filteringCharset = Charsets.UTF_8.name() // We want UTF-8 for everything
        }

        test {
            useJUnitPlatform()
            failFast = false
        }
    }
}
