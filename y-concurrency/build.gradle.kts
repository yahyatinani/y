plugins {
    kotlin("multiplatform")
    java
    `java-library`
}

kotlin {
    targets {
        jvm()

        linuxX64()

        mingwX64()

        macosX64()
        macosArm64()

        tvos()
        tvosSimulatorArm64()

        watchosArm32()
        watchosArm64()
        watchosX86()
        watchosX64()
        watchosSimulatorArm64()

        iosX64()
        iosArm64()
        iosArm32()
        iosSimulatorArm64()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
                implementation(projects.yCore)
                implementation(Deps.Kotlinx.Atomicfu.atomicfu)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(Deps.Kotest.framework)
                implementation(Deps.Kotest.assertions)
            }
        }

        val jvmTest by getting {
            dependencies {
                implementation(Deps.Kotlinx.Coroutines.test)
                implementation(Deps.Kotest.runnerJvm)
            }
        }

        val nativeMain by creating {
            dependsOn(commonMain)
        }

        val macosX64Main by getting {
            dependsOn(nativeMain)
        }

        val macosArm64Main by getting {
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

        val iosSimulatorArm64Main by getting {
            dependsOn(nativeMain)
        }

        val watchosArm32Main by getting {
            dependsOn(nativeMain)
        }

        val watchosArm64Main by getting {
            dependsOn(nativeMain)
        }

        val watchosX86Main by getting {
            dependsOn(nativeMain)
        }

        val watchosX64Main by getting {
            dependsOn(nativeMain)
        }

        val watchosSimulatorArm64Main by getting {
            dependsOn(nativeMain)
        }

        val tvosMain by getting {
            dependsOn(nativeMain)
        }
        val tvosSimulatorArm64Main by getting {
            dependsOn(nativeMain)
        }

        all {
            languageSettings.optIn("kotlin.time.ExperimentalTime")
            languageSettings.optIn("kotlin.experimental.ExperimentalTypeInference")
            languageSettings.optIn("kotlin.RequiresOptIn")
        }
    }
}

apply(from = "../publish-y.gradle.kts")
