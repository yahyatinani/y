plugins {
    id("java")
    id("kotlin-multiplatform")
    id("java-library")
}

repositories {
    mavenCentral()
}

kotlin {
    targets {
        jvm {
            compilations.all {
                kotlinOptions {
                    jvmTarget = "1.8"
                }
            }
        }

        js {
            browser()
            nodejs()
        }

        linuxX64()

        mingwX64()

        macosX64()
        tvos()
        watchos()
        iosX64()
        iosArm64()
        iosArm32()
    }

    targets.all {
        compilations.all {
            kotlinOptions {
                freeCompilerArgs = freeCompilerArgs +
                    "-Xopt-in=kotlin.RequiresOptIn"
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(Libs.Kotlinx.serialization)
            }
        }

        val jvmTest by getting {
            dependencies {
                implementation(Libs.Kotest.runner)
                implementation(Libs.Kotest.assertions)
                implementation(Libs.Kotest.propertyTest)
                implementation(Libs.Kotlinx.serialization)
            }
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

apply(from = "../publish-y.gradle.kts")
