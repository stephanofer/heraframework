import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.the
import org.gradle.jvm.toolchain.JavaLanguageVersion

plugins {
    java
}

val libs = the<VersionCatalogsExtension>().named("libs")

java {
    toolchain.languageVersion = JavaLanguageVersion.of(libs.findVersion("java").get().requiredVersion.toInt())
    withSourcesJar()
}

dependencies {
    compileOnly(libs.findLibrary("paper-api").get())
    testImplementation(platform(libs.findLibrary("junit-bom").get()))
    testImplementation(libs.findLibrary("junit-jupiter").get())
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

tasks.processResources {
    val properties = mapOf("version" to project.version)

    filesMatching(listOf("plugin.yml", "paper-plugin.yml")) {
        expand(properties)
    }
}
