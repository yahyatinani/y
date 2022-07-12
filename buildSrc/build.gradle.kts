plugins {
  `kotlin-dsl`
}

repositories {
  mavenCentral()
  gradlePluginPortal()
}

dependencies {
  implementation(deps.kotlin.gradle.plugin)
  implementation(deps.kotlinx.atomicfu.plugin)
}
