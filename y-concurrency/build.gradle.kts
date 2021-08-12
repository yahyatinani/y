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
                implementation(kotlin("stdlib-common"))
                implementation(Libs.Kotlinx.atomicfu)
                api(project(":y-collections"))
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(Libs.Kotest.assertions)
                implementation(Libs.Kotest.propertyTest)
            }
        }

        val jvmTest by getting {
            dependencies {
                implementation(Libs.Kotest.runner)
                implementation(Libs.Kotlinx.coroutinesTest)
            }
        }
    }
}

tasks.named<Test>("jvmTest") {
    useJUnitPlatform()
}

apply(from = "../publish-y.gradle.kts")
