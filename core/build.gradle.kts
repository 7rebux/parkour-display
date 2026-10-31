import net.labymod.labygradle.common.extension.LabyModAnnotationProcessorExtension.ReferenceType

plugins {
    id("io.freefair.lombok") version "9.2.0"
}

dependencies {
    labyProcessor()
    implementation(project(":integration"))
    api(project(":api"))

    // An example of how to add an external dependency that is used by the addon.
    // addonMavenDependency("org.jeasy:easy-random:5.0.0")

    api("org.jspecify:jspecify:1.0.0")

    testImplementation(platform("org.junit:junit-bom:5.11.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

// The LabyMod processor only gets its options on the main source set and fails without them.
configurations.testAnnotationProcessor {
    exclude(group = "net.labymod.labymod4")
}

tasks.test {
    useJUnitPlatform()
}

labyModAnnotationProcessor {
    referenceType = ReferenceType.DEFAULT
}
