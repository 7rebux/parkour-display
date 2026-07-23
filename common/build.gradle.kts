plugins {
    id("java")
    id("java-library")
    id("io.freefair.lombok") version "9.2.0"
}

group = rootProject.group
version = rootProject.version

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

dependencies {
    api("org.jspecify:jspecify:1.0.0")

    // Gson is provided at runtime by both platforms (LabyMod ships it; Minecraft bundles it),
    // so keep it off the module's runtime classpath to avoid duplicate copies.
    compileOnly("com.google.code.gson:gson:2.11.0")

    testImplementation("com.google.code.gson:gson:2.11.0")
    testImplementation(platform("org.junit:junit-bom:5.11.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}
