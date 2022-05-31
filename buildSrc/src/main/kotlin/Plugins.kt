object Plugins {
  object Kotlinter {
    const val id = "org.jmailen.kotlinter"
    const val version = "3.9.0"
  }

  object Kotlinx {
    object Serialization {
      const val id = "plugin.serialization"
    }
  }

  object Kotest {
    const val id = "io.kotest.multiplatform"
    const val version = Deps.Kotest.version
  }

  object Kover {
    const val id = "org.jetbrains.kotlinx.kover"
    const val version = "0.5.0"
  }
}
