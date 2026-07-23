rootProject.name = "parkour-display"

pluginManagement {
    repositories {
        maven("https://maven.laby.net/api/v1/maven/release/")
        maven("https://maven.neoforged.net/releases/")
        maven("https://maven.fabricmc.net/")
        gradlePluginPortal()
        mavenCentral()
    }

    plugins {
        id("net.labymod.labygradle.settings") version "0.8.1"
        id("fabric-loom") version "1.11-SNAPSHOT"
    }
}

plugins {
    id("net.labymod.labygradle.settings")
}

include(":common")
include(":fabric")

// LabyMod modules are grouped under labymod/. Project paths stay flat so the labygradle
// addon aggregation and inter-module dependencies are unaffected by the relocation.
include(":api")
include(":core")
include(":integration")
project(":api").projectDir = file("labymod/api")
project(":core").projectDir = file("labymod/core")
project(":integration").projectDir = file("labymod/integration")
