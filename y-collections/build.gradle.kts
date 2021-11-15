plugins {
    id("java")
    id("kotlin-multiplatform")
    id("java-library")
}

val ideaActive = System.getProperty("idea.active") == "true"

kotlin {
    targets {
        jvm {
            compilations.all {
                kotlinOptions {
                    jvmTarget = "1.8"
                }
            }
        }

        if (!ideaActive) {
            linuxX64()
            mingwX64()
            macosX64()

            tvos()

            watchosArm32()
            watchosArm64()
            watchosX86()
            watchosX64()

            iosX64()
            iosArm64()
            iosArm32()
        } else {
            val hostOs = System.getProperty("os.name")
            val nativeTarget = when {
                hostOs == "Mac OS X" -> macosX64("native")
                hostOs == "Linux" -> linuxX64("native")
                hostOs.startsWith("Windows") -> mingwX64("native")
                else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(Libs.Atomicfu.atomicfu)
                implementation(Libs.Serialization.json)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(Libs.Kotest.assertions)
                implementation(Libs.Kotest.propertyTest)
            }
        }

        val jvmMain by getting
        val jvmTest by getting {
            dependencies {
                implementation(Libs.Kotest.runner)
                implementation(Libs.Coroutines.test)
            }
        }

        if (!ideaActive) {
            val nativeMain by creating {
                dependsOn(commonMain)
            }
            val nativeTest by creating

            val macosX64Main by getting {
                dependsOn(nativeMain)
            }

            val mingwX64Main by getting {
                dependsOn(nativeMain)
            }

            val linuxX64Main by getting {
                dependsOn(nativeMain)
            }

            val iosX64Main by getting {
                dependsOn(nativeMain)
            }

            val iosArm64Main by getting {
                dependsOn(nativeMain)
            }

            val iosArm32Main by getting {
                dependsOn(nativeMain)
            }

            val watchosX86Main by getting {
                dependsOn(nativeMain)
            }

            val watchosArm32Main by getting {
                dependsOn(nativeMain)
            }

            val watchosArm64Main by getting {
                dependsOn(nativeMain)
            }

            val watchosX64Main by getting {
                dependsOn(nativeMain)
            }

            val tvosMain by getting {
                dependsOn(nativeMain)
            }
        }

        all {
            languageSettings.optIn("kotlin.time.ExperimentalTime")
            languageSettings.optIn("kotlin.experimental.ExperimentalTypeInference")
            languageSettings.optIn("kotlin.RequiresOptIn")
        }
    }
}

tasks.named<Test>("jvmTest") {
    useJUnitPlatform()
}

apply(from = "../publish-y.gradle.kts")
