import java.util.Base64

plugins {
    id("net.labymod.labygradle")
    id("net.labymod.labygradle.addon")
}

val versions = providers.gradleProperty("net.labymod.minecraft-versions").get().split(";")

group = "pw.rebux.parkourdisplay"
version = providers.environmentVariable("VERSION").getOrElse("1.3.0")

labyMod {
    defaultPackageName = "pw.rebux.parkourdisplay"

    minecraft {
        registerVersion(versions.toTypedArray()) {
            runs {
                getByName("client") {
                    // When the property is set to true, you can log in with a Minecraft account
                    // devLogin = true
                }
            }
        }
    }

    addonInfo {
        namespace = "parkourdisplay"
        displayName = "ParkourDisplay"
        author = "Rebux"
        description = "Shows different information about parkour related game mechanics."
        minecraftVersion = "*"
        version = rootProject.version.toString()
    }
}

allprojects {
    repositories {
        // Every project resolving :core's classpath (incl. the labygradle game-runner) needs this.
        // TEMP: unpublished Central Portal deployment, for end-to-end testing parkour-protocol 0.0.1.
        // Needs centralUsername/centralPassword (a Portal user token) in ~/.gradle/gradle.properties.
        maven("https://central.sonatype.com/api/v1/publisher/deployments/download/") {
            name = "centralDeployment"
            val user = providers.gradleProperty("centralUsername").orNull
            val pass = providers.gradleProperty("centralPassword").orNull
            credentials(HttpHeaderCredentials::class) {
                name = "Authorization"
                value = "Bearer " + Base64.getEncoder().encodeToString("$user:$pass".toByteArray())
            }
            authentication {
                create<HttpHeaderAuthentication>("header")
            }
            content {
                includeGroup("org.mcpk.protocol")
            }
        }
    }
}

subprojects {
    plugins.apply("net.labymod.labygradle")
    plugins.apply("net.labymod.labygradle.addon")

    group = rootProject.group
    version = rootProject.version

    extensions.findByType(JavaPluginExtension::class.java)?.apply {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}
