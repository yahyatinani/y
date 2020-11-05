object Libs {

    const val kotlinVersion = "1.4.10"
    const val kotlinApiVersion = "1.4"
    const val jvmTargetVersion = "1.8"

    object Kotest {
        private const val version = "4.2.6"
        const val runner = "io.kotest:kotest-runner-junit5-jvm:$version"
        const val assertions = "io.kotest:kotest-assertions-core-jvm:$version"
        const val propertyTest = "io.kotest:kotest-property-jvm:$version"
    }

    object Ktlint {
        const val version = "9.3.0"
        const val id = "org.jlleitschuh.gradle.ktlint"
    }

    object Kotlinx {
        private const val version = "1.0.0"

        const val serialization =
            "org.jetbrains.kotlinx:kotlinx-serialization-json:$version"
    }

    object Atomicfu {
        private const val version = "0.14.4"

        const val atomicfcu = "org.jetbrains.kotlinx:atomicfu:$version"
    }
}
