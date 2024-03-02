pluginManagement {
    plugins {
        kotlin("jvm") version "1.9.21"
        id("io.quarkus") version "3.6.4"
    }
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}
rootProject.name = "kotlin"

