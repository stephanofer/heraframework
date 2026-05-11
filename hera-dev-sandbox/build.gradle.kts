import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("hera.paper-plugin-conventions")
    id("com.gradleup.shadow") version "9.4.1"
}

dependencies {
    implementation(project(":hera-command-paper"))
}

tasks.jar {
    archiveClassifier.set("plain")
}

tasks.named<ShadowJar>("shadowJar") {
    archiveClassifier.set("")
}
