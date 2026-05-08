plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    implementation("xyz.jpenilla.run-paper:xyz.jpenilla.run-paper.gradle.plugin:3.0.2")
}
