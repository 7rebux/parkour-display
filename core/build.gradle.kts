import net.labymod.labygradle.common.extension.LabyModAnnotationProcessorExtension.ReferenceType

plugins {
    id("io.freefair.lombok") version "8.13.1"
}

dependencies {
    labyProcessor()
    api(project(":api"))

    // An example of how to add an external dependency that is used by the addon.
    // addonMavenDependency("org.jeasy:easy-random:5.0.0")
}

labyModAnnotationProcessor {
    referenceType = ReferenceType.DEFAULT
}