pluginManagement {
    includeBuild("build-logic")

    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS

    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

rootProject.name = "HeraFramework"

include(
    "hera-core-api",
    "hera-core-runtime",
    "hera-config",
    "hera-feedback",
    "hera-scheduler",
    "hera-command-paper",
    "hera-data-mysql",
    "hera-data-redis",
    "hera-hook-placeholderapi",
    "hera-hook-zmenu",
    "hera-dev-sandbox",
)
