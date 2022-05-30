pluginManagement {
  repositories {
    mavenCentral()
    gradlePluginPortal()
  }
}

rootProject.name = "y"

include("y-core")
include("y-concurrency")

plugins {
  id("com.gradle.enterprise") version "3.9"
  // See https://jmfayard.github.io/refreshVersions
  // id("de.fayard.refreshVersions") version "0.11.0"
}

gradleEnterprise {
  buildScan {
    termsOfServiceUrl = "https://gradle.com/terms-of-service"
    termsOfServiceAgree = "yes"
  }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
