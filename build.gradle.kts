import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        mavenCentral()
        mavenLocal()
        gradlePluginPortal()
    }

    dependencies {
        classpath("org.jetbrains.kotlinx:atomicfu-gradle-plugin:0.16.2")
    }
}

plugins {
    java
    `java-library`
    kotlin("multiplatform") version Libs.kotlinVersion
    kotlin("plugin.serialization") version Libs.kotlinVersion
    id(Plugins.Ktlint.id) version Plugins.Ktlint.version

    id("maven-publish")
    signing
}

tasks { javadoc }

kotlin {
    targets {
        jvm {
            compilations.all {
                kotlinOptions {
                    jvmTarget = Libs.jvmTargetVersion
                }
            }
        }
    }
}

allprojects {
    group = "com.github.whyrising.y"

    version = Ci.publishVersion()

    tasks.withType<KotlinCompile>().configureEach {
        kotlinOptions.jvmTarget = Libs.jvmTargetVersion
        kotlinOptions.apiVersion = Libs.kotlinApiVersion
    }
}

val testReport = tasks.register<TestReport>("testReport") {
    destinationDir = file("$buildDir/reports/tests/all")
    reportOn(subprojects.mapNotNull { it.tasks.findByPath("test") })
}

subprojects {
    buildscript {
        repositories {
            mavenCentral()
        }
    }

    apply(plugin = Plugins.Ktlint.id)
    apply(plugin = "kotlinx-serialization")
    apply(plugin = "kotlinx-atomicfu")

    ktlint { debug.set(true) }

    tasks.withType<Test> {
        maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2)
            .takeIf { it > 0 } ?: 1

        useJUnitPlatform()
        finalizedBy(testReport)
        testLogging { events("passed", "skipped", "failed") }
    }
}

val extension = extensions.getByName("publishing") as PublishingExtension
val publications: PublicationContainer = extension.publications

signing {
    useGpgCmd()

    if (Ci.isRelease()) sign(publications)
}
