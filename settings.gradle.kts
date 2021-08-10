dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "y"

include(":y-core")
include(":y-collections")

plugins {
    id("com.gradle.enterprise") version "3.6.1"
    // See https://jmfayard.github.io/refreshVersions
    // id("de.fayard.refreshVersions") version "0.11.0"
}

gradleEnterprise {
    buildScan {
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
    }
}
