import org.gradle.api.JavaVersion

object Deps {
    val jvmTarget = JavaVersion.VERSION_1_8.toString()
    const val kotlinApiVersion = "1.6"
    const val kotlinVersion = "1.6.20"

    object Kotlinx {
        const val group = "org.jetbrains.kotlinx"

        object Atomicfu {
            private const val version = "0.17.1"
            const val atomicfu = "$group:atomicfu:$version"
            const val gradlePlugin = "$group:atomicfu-gradle-plugin:$version"
        }

        object Serialization {
            private const val version = "1.3.2"
            const val json = "$group:kotlinx-serialization-json:$version"
        }

        object Coroutines {
            private const val coroutinesV = "1.6.0"
            const val core = "$group:kotlinx-coroutines-core:$coroutinesV"

            const val test = "$group:kotlinx-coroutines-test:$coroutinesV"
        }
    }

    object Kotest {
        const val version = "5.2.2"
        const val runnerJvm = "io.kotest:kotest-runner-junit5-jvm:$version"
        const val assertions = "io.kotest:kotest-assertions-core:$version"
        const val propertyTest = "io.kotest:kotest-property:$version"
        const val framework = "io.kotest:kotest-framework-engine:$version"
    }
}
