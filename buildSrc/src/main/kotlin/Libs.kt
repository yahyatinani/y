object Libs {
    const val kotlinVersion = "1.5.21"
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

        private const val atomicfuVersion = "0.16.2"
        private const val serialVersion = "1.2.2"
        private const val coroutinesV = "1.5.1"

        const val atomicfu = "$gr:atomicfu:$atomicfuVersion"
        const val serialJson = "$gr:kotlinx-serialization-json:$serialVersion"
        const val coroutines = "$gr:kotlinx-coroutines-core:$coroutinesV"
        const val coroutinesTest = "$gr:kotlinx-coroutines-test:$coroutinesV"
    }
}
