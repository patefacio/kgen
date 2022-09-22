import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.10"
    application
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
        version = "1.2.6"
    )

    testImplementation(kotlin("test"))
//    testImplementation("io.github.microutils:kotlin-logging-jvm:2.0.11")
//    testImplementation(
//        group = "ch.qos.logback",
//        name = "logback-classic",
//        version = "1.2.6"
//    )
}

tasks.test {
    useJUnitPlatform()
    testLogging { showStandardStreams = true }
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClass.set("MainKt")
}