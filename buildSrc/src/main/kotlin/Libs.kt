object Libs {

    const val kotlinVersion = "1.4.0"
    const val kotlinApiVersion = "1.3"
    const val jvmTargetVersion = "1.8"
    const val ktlintVersion = "9.3.0"

    const val ktlintId = "org.jlleitschuh.gradle.ktlint"

    object Kotest {
        private const val version = "4.2.0"
        const val runner = "io.kotest:kotest-runner-junit5-jvm:$version"
        const val assertions = "io.kotest:kotest-assertions-core-jvm:$version"
        const val propertyTest = "io.kotest:kotest-property-jvm:$version"
    }
}