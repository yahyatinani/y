import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {

    repositories {
        mavenCentral()
        mavenLocal()
    }
}

plugins {
    base
    `java-library`
    kotlin("jvm") version Libs.kotlinVersion
    id(Libs.Ktlint.id) version Libs.Ktlint.version
    jacoco
    id("maven-publish")
    signing
}

tasks {
    javadoc
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
}

allprojects {

    group = "com.github.whyrising.y"

    version = Ci.publishVersion

    apply(plugin = "jacoco")

    repositories {
        jcenter()
        mavenCentral()
    }

    tasks.withType<KotlinCompile>().configureEach {
        kotlinOptions.jvmTarget = Libs.jvmTargetVersion
        kotlinOptions.apiVersion = Libs.kotlinApiVersion
    }
}

val testReport = tasks.register<TestReport>("testReport") {
    destinationDir = file("$buildDir/reports/tests/test")
    reportOn(subprojects.mapNotNull { it.tasks.findByPath("test") })
}

subprojects {
    buildscript {
        repositories {
            jcenter()
            mavenCentral()
        }
    }

    apply(plugin = Libs.Ktlint.id)

    ktlint {
        debug.set(true)
    }

    tasks.withType<Test> {
        maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2)
            .takeIf { it > 0 } ?: 1

        useJUnitPlatform()
        finalizedBy(testReport)
        testLogging {
            events("passed", "skipped", "failed")
        }
    }
}

tasks.register<JacocoReport>("jacocoRootReport") {
    subprojects {
        this@subprojects.plugins.withType<JacocoPlugin>().configureEach {
            this@subprojects.tasks.matching {
                it.extensions.findByType<JacocoTaskExtension>() != null
            }.configureEach {
                sourceSets(
                    this@subprojects.the<SourceSetContainer>()
                        .named("main").get()
                )
                executionData(this)
            }
        }
    }

    reports {
        xml.isEnabled = true
        html.isEnabled = true
    }
}

tasks.jacocoTestReport {
    // tests are required to run before generating the report
    dependsOn(tasks.test)
}

val extension = extensions.getByName("publishing") as PublishingExtension
val publications: PublicationContainer = extension.publications

signing {
    useGpgCmd()

    if (Ci.isRelease)
        sign(publications)
}
