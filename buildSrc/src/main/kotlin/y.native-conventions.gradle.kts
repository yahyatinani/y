import com.github.whyrising.y.Ci
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
  id("kotlin-conventions")
}

kotlin {
  if (!project.hasProperty(Ci.JVM_ONLY)) {
    linuxX64()

    mingwX64()

    macosX64()
    macosArm64()

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    tvosX64()
    tvosArm64()
    tvosSimulatorArm64()

    watchosX64()
    watchosArm32()
    watchosArm64()
    watchosSimulatorArm64()

    sourceSets {
      val commonMain by getting
      val commonTest by getting

      val nativeMain by creating { dependsOn(commonMain) }
      val nativeTest by creating {
        dependsOn(commonTest)
        dependsOn(commonMain)
      }

      targets.whenObjectAdded {
        if (this is KotlinNativeTarget) {
          sourceSets.getByName("${name}Main").dependsOn(nativeMain)
          sourceSets.getByName("${name}Test").dependsOn(nativeTest)
        }
      }
    }
  } else {
    jvm()
  }
}
