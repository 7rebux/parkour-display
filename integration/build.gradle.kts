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
    api(project(":api"))

    compileOnly("net.labymod.serverapi:core:1.0.10")
    compileOnly("com.google.auto.service:auto-service:1.1.1")
    annotationProcessor("com.google.auto.service:auto-service:1.1.1")

    api("org.jspecify:jspecify:1.0.0")
}

tasks.shadowJar {
    // Only bundle the :api project; keep everything else as normal dependencies.
    dependencies {
        include(project(":api"))
    }
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
