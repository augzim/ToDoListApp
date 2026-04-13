// todo: remove compose from everywhere in the project, since switched to swing


plugins {
    kotlin("jvm") version "2.3.10"
    id("org.jetbrains.compose") version "1.6.11"
    id("org.jetbrains.kotlin.plugin.compose") version "2.3.10"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    google()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("org.postgresql:postgresql:42.7.10")
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
}

compose.desktop {
    application {
        mainClass = "MainKt"
    }
}

kotlin {
    jvmToolchain(25)
}

tasks.test {
    useJUnitPlatform()
}