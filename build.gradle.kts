import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        mavenCentral()
        mavenLocal()
        gradlePluginPortal()
    }

    dependencies {
        classpath(Deps.Kotlinx.Atomicfu.gradlePlugin)
    }
}

apply(plugin = "kotlinx-atomicfu")

plugins {
    kotlin("multiplatform") version Deps.kotlinVersion
    java
    `java-library`
    `maven-publish`
    signing
    kotlin(Plugins.Kotlinx.Serialization.id) version Deps.kotlinVersion
    id(Plugins.Kotlinter.id) version Plugins.Kotlinter.version
    id(Plugins.Kotest.id) version Plugins.Kotest.version
}

tasks { javadoc }

kotlin {
    targets {
        jvm {
            compilations.all {
                kotlinOptions {
                    jvmTarget = Deps.jvmTarget
                }
            }
        }
    }
}

allprojects {
    repositories {
        mavenCentral()
        mavenLocal()
    }

    apply(plugin = "kotlinx-serialization")
    apply(plugin = Plugins.Kotlinter.id)
    apply(plugin = Plugins.Kotest.id)

    kotlinter {
        reporters = arrayOf("checkstyle", "plain")
        indentSize = 0
    }

    group = "com.github.whyrising.y"
    version = Ci.publishVersion()

    tasks.withType<KotlinCompile>().configureEach {
        kotlinOptions {
            freeCompilerArgs = freeCompilerArgs.plus(
                "-Xopt-in=kotlin.RequiresOptIn"
            )
            jvmTarget = Deps.jvmTarget
            apiVersion = Deps.kotlinApiVersion
            languageVersion = Deps.kotlinApiVersion
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
        filter {
            isFailOnNoMatchingTests = false
        }
        testLogging {
            showExceptions = true
            showStandardStreams = true
            events = setOf(
                TestLogEvent.FAILED,
                TestLogEvent.PASSED,
                TestLogEvent.SKIPPED,
                TestLogEvent.STANDARD_OUT,
                TestLogEvent.STANDARD_ERROR,
            )
            exceptionFormat = TestExceptionFormat.FULL
        }
    }.configureEach {
        maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2)
            .takeIf { it > 0 } ?: 1
    }
}

val testReport = tasks.register<TestReport>("testReport") {
    destinationDir = file("$buildDir/reports/tests/all")
    reportOn(subprojects.mapNotNull { it.tasks.findByPath("test") })
}

val extension = extensions.getByName("publishing") as PublishingExtension
val publications: PublicationContainer = extension.publications

signing {
    useGpgCmd()
    if (Ci.isRelease())
        sign(publications)
}
