plugins {
    id("fabric-loom")
}

group = rootProject.group
version = rootProject.version

// Standalone Fabric mod. Independent of LabyMod; targets the latest Minecraft version.
val minecraftVersion = "1.21.8"
val yarnMappings = "1.21.8+build.1"
val loaderVersion = "0.17.2"
val fabricApiVersion = "0.133.4+1.21.8"

repositories {
    mavenCentral()
    maven("https://maven.fabricmc.net/")
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraftVersion")
    mappings("net.fabricmc:yarn:$yarnMappings:v2")
    modImplementation("net.fabricmc:fabric-loader:$loaderVersion")
    modImplementation("net.fabricmc.fabric-api:fabric-api:$fabricApiVersion")

    // Bundle the platform-neutral domain logic into the mod jar (jar-in-jar).
    implementation(project(":common"))
    include(project(":common"))
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.processResources {
    inputs.property("version", project.version)
    filesMatching("fabric.mod.json") {
        expand("version" to project.version)
    }
}
