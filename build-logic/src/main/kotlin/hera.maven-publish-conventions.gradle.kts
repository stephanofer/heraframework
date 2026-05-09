import org.gradle.api.publish.maven.MavenPublication

plugins {
    `maven-publish`
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifactId = project.name

            pom {
                name.set(project.name)
                description.set(project.description ?: rootProject.description ?: project.name)
            }
        }
    }
}
