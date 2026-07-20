import net.labymod.labygradle.common.extension.LabyModAnnotationProcessorExtension.ReferenceType

plugins {
    id("io.freefair.lombok") version "9.2.0"
}

repositories {
    mavenCentral()
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

    // Gson is provided at runtime by LabyMod; make it explicit for the standalone tests.
    testImplementation("com.google.code.gson:gson:2.10.1")
}

labyModAnnotationProcessor {
    referenceType = ReferenceType.DEFAULT
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// The LabyMod annotation processor only applies to the addon's main sources; the standalone
// tests neither need nor tolerate it, so annotation processing is disabled for test compilation.
tasks.named<JavaCompile>("compileTestJava") {
    options.compilerArgs.add("-proc:none")
}
