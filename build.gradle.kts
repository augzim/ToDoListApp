// todo: remove compose from everywhere in the project, since switched to swing


plugins {
    kotlin("jvm") version "2.3.10"
    application
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
    implementation("com.miglayout:miglayout-swing:11.4.3")
}

application {
    mainClass.set("MainKt")
}

kotlin {
    jvmToolchain(25)
}

tasks.test {
    useJUnitPlatform()
}