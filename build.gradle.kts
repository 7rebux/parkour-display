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

subprojects {
    group = rootProject.group
    version = rootProject.version

    // The `fabric` module uses the fabric-loom toolchain and must NOT receive the LabyMod addon
    // plugins. `common` IS a LabyMod addon submodule (like `api`/`integration`) so the addon
    // bundles its classes and labygradle's dependency index resolves it — its code stays laby-free.
    if (name == "fabric") {
        return@subprojects
    }

    plugins.apply("net.labymod.labygradle")
    plugins.apply("net.labymod.labygradle.addon")

    extensions.findByType(JavaPluginExtension::class.java)?.apply {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}
