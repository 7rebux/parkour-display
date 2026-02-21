plugins {
    id("net.labymod.labygradle")
    id("net.labymod.labygradle.addon")
}

val versions = providers.gradleProperty("net.labymod.minecraft-versions").get().split(";")

group = "pw.rebux.parkourdisplay"
version = providers.environmentVariable("VERSION").getOrElse("1.0.0")

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

subprojects {
    plugins.apply("net.labymod.labygradle")
    plugins.apply("net.labymod.labygradle.addon")

    // Temporary fix for missing macos-natives-patch classifier
    val isMacOS = System.getProperty("os.name").lowercase().contains("mac")

    configurations.all {
        if (isMacOS) {
            exclude(group = "org.lwjgl", module = "lwjgl-freetype")
        }
    }

    dependencies {
        if (isMacOS) {
            runtimeOnly("org.lwjgl:lwjgl-freetype:3.4.1:natives-macos-arm64")
        }
    }

    group = rootProject.group
    version = rootProject.version

    extensions.findByType(JavaPluginExtension::class.java)?.apply {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}
