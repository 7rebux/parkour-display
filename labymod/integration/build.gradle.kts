plugins {
    id("java")
    id("java-library")
    id("com.gradleup.shadow") version "8.3.5"
}

group = "pw.rebux.parkourdisplay"
version = "1.0.0"

repositories {
    mavenCentral()

    repositories {
        maven {
            name = "labymod"
            url = uri("https://dist.labymod.net/api/v1/maven/release/")
        }
    }
}

dependencies {
    // Permissions live in :api; the shared SplitBoxTriggerMode lives in :common.
    api(project(":api"))
    api(project(":common"))

    compileOnly("net.labymod.serverapi:core:1.0.10")
    compileOnly("com.google.auto.service:auto-service:1.1.1")
    annotationProcessor("com.google.auto.service:auto-service:1.1.1")

    api("org.jspecify:jspecify:1.0.0")
}

tasks.shadowJar {
    // Bundle the shared projects; keep everything else as normal dependencies.
    dependencies {
        include(project(":api"))
        include(project(":common"))
    }
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
