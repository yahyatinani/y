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
        val commonMain by getting


        val jvmMain by getting {
            dependsOn(commonMain)
        }

        val jvmTest by getting {
            dependencies {
                implementation(Libs.Kotest.runner)
                implementation(Libs.Kotest.assertions)
                implementation(Libs.Kotest.propertyTest)
            }
        }

        val desktopMain by creating {
            dependsOn(commonMain)
        }

        val macosX64Main by getting {
            dependsOn(desktopMain)
        }

        val mingwX64Main by getting {
            dependsOn(desktopMain)
        }

        val linuxX64Main by getting {
            dependsOn(desktopMain)
        }

        val tvosMain by getting {
            dependsOn(desktopMain)
        }

        val watchosMain by getting {
            dependsOn(desktopMain)
        }

        val iosX64Main by getting {
            dependsOn(desktopMain)
        }

        val iosArm64Main by getting {
            dependsOn(desktopMain)
        }

        val iosArm32Main by getting {
            dependsOn(desktopMain)
        }
    }
}
tasks.withType<Test> {
    useJUnitPlatform()
}

apply(from = "../publish-y.gradle.kts")
