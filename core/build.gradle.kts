import net.labymod.labygradle.common.extension.LabyModAnnotationProcessorExtension.ReferenceType

plugins {
    id("io.freefair.lombok") version "9.2.0"
}

repositories {
    // TODO: Replace with https://minecraft-parkour.github.io/parkour-protocol/ once parkour-protocol 1.0.0 is released.
    mavenLocal()
}

// Dependencies bundled into the addon jar. labygradle refuses addonMavenDependency from mavenLocal,
// since the LabyMod loader downloads those at runtime. Once parkour-protocol is published to a remote
// repository, this can be replaced with addonMavenDependency.
val bundled: Configuration by configurations.creating {
    isTransitive = true
}

configurations.implementation {
    extendsFrom(bundled)
}

dependencies {
    labyProcessor()
    implementation(project(":integration"))
    api(project(":api"))

    bundled("com.mcpk.protocol:client-common:1.0.0-SNAPSHOT")

    api("org.jspecify:jspecify:1.0.0")
}

tasks.jar {
    dependsOn(bundled)
    from({ bundled.map { zipTree(it) } }) {
        exclude("META-INF/**")
    }
}

labyModAnnotationProcessor {
    referenceType = ReferenceType.DEFAULT
}
