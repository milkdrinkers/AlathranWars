pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0" // allow automatic download of JDKs
}

rootProject.name = "AlathranWars"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include("api", "common", "paper")
