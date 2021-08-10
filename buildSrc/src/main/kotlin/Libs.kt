object Libs {
    const val kotlinVersion = "1.5.10"
    const val kotlinApiVersion = "1.5"
    const val jvmTargetVersion = "1.8"

    object Kotest {
        private const val version = "4.6.1"
        const val runner = "io.kotest:kotest-runner-junit5:$version"
        const val assertions = "io.kotest:kotest-assertions-core:$version"
        const val propertyTest = "io.kotest:kotest-property:$version"
    }

    object Kotlinx {
        private const val gr = "org.jetbrains.kotlinx"

        private const val atomicfuVersion = "0.16.1"
        private const val serialVersion = "1.2.1"
        private const val coroutinesVersion = "1.5.0"

        const val atomicfu = "$gr:atomicfu:$atomicfuVersion"
        const val serialJson = "$gr:kotlinx-serialization-json:$serialVersion"
        const val coroutines = "$gr:kotlinx-coroutines-core:$coroutinesVersion"
    }
}
