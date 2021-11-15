object Libs {
    const val kotlinVersion = "1.5.31"
    const val kotlinApiVersion = "1.5"
    const val jvmTargetVersion = "1.8"

    object Kotest {
        private const val version = "4.6.3"
        const val runner = "io.kotest:kotest-runner-junit5:$version"
        const val assertions = "io.kotest:kotest-assertions-core:$version"
        const val propertyTest = "io.kotest:kotest-property:$version"
    }

    const val gr = "org.jetbrains.kotlinx"

    object Atomicfu {
        const val version = "0.16.3"
        const val atomicfu = "$gr:atomicfu:$version"
    }

    object Serialization {
        private const val version = "1.3.0"
        const val json = "$gr:kotlinx-serialization-json:$version"
    }

    object Coroutines {
        private const val coroutinesV = "1.5.2"

        const val core = "$gr:kotlinx-coroutines-core:$coroutinesV"
        const val test = "$gr:kotlinx-coroutines-test:$coroutinesV"
    }
}
