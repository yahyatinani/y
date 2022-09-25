import com.github.whyrising.y.Ci
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED
import org.gradle.api.tasks.testing.logging.TestLogEvent.STANDARD_ERROR
import org.gradle.api.tasks.testing.logging.TestLogEvent.STANDARD_OUT
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("multiplatform")
}

group = "com.github.whyrising.y"
version = Ci.publishVersion

tasks.withType<Test> {
  useJUnitPlatform()
  filter {
    isFailOnNoMatchingTests = false
  }
  testLogging {
    exceptionFormat = TestExceptionFormat.FULL
    events = setOf(SKIPPED, FAILED, STANDARD_OUT, STANDARD_ERROR)
  }
}

tasks.withType<KotlinCompile>().configureEach {
  kotlinOptions {
    freeCompilerArgs = freeCompilerArgs + "-Xopt-in=kotlin.RequiresOptIn"
    jvmTarget = "1.8"
    apiVersion = "1.7"
    languageVersion = "1.7"
  }
}

kotlin {
  sourceSets {
    all {
      languageSettings.optIn("kotlin.time.ExperimentalTime")
      languageSettings.optIn("kotlin.experimental.ExperimentalTypeInference")
      languageSettings.optIn("kotlin.contracts.ExperimentalContracts")
    }
  }
}
