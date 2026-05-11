import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.publish.maven.MavenPublication

val shade by configurations.creating

plugins {
    id("hera.paper-library-conventions")
    id("hera.maven-publish-conventions")
    id("com.gradleup.shadow") version "9.4.1"
}

dependencies {
    compileOnly(libs.boosted.yaml)
    shade(libs.boosted.yaml)
}

tasks.jar {
    archiveClassifier.set("plain")
}

tasks.named<ShadowJar>("shadowJar") {
    archiveClassifier.set("")
    configurations = listOf(shade)

    relocate("dev.dejvokep.boostedyaml", "com.stephanofer.hera.config.libs.boostedyaml")

    exclude("META-INF/maven/**")
    exclude("META-INF/*.SF")
    exclude("META-INF/*.DSA")
    exclude("META-INF/*.RSA")
    exclude("org/jetbrains/annotations/**")
    exclude("org/intellij/lang/annotations/**")
}

publishing {
    publications.named<MavenPublication>("mavenJava") {
        setArtifacts(listOf(tasks.named("shadowJar").get()))
    }
}
