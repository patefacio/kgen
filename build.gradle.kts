import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

java {
    sourceCompatibility = org.gradle.api.JavaVersion.VERSION_17
    targetCompatibility = org.gradle.api.JavaVersion.VERSION_17
}

plugins {
    kotlin("jvm") version "1.9.0"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.microutils:kotlin-logging-jvm:2.0.11")
    implementation(
        group = "ch.qos.logback",
        name = "logback-classic",
        version = "1.2.9"
    )

    implementation("org.jetbrains.kotlin-wrappers:kotlin-css:1.0.0-pre.636")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
    testLogging { showStandardStreams = true }
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
