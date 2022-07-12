import com.github.whyrising.y.Ci
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
  id("kotlin-conventions")
}

kotlin {
  if (!project.hasProperty(Ci.JVM_ONLY)) {
    targets {
      jvm()

      iosX64()
      iosArm32()
      iosArm64()
      iosSimulatorArm64()

      tvosX64()
      tvosArm64()
      tvosSimulatorArm64()

      watchosX86()
      watchosX64()
      watchosArm32()
      watchosArm64()
      watchosSimulatorArm64()

      linuxX64()
      mingwX64()
      macosX64()
      macosArm64()
    }

    sourceSets {
      val commonMain by getting
      val commonTest by getting

      val nativeMain by creating {
        dependsOn(commonMain)
      }
      val nativeTest by creating {
        dependsOn(commonTest)
      }

      targets.whenObjectAdded {
        if (this is KotlinNativeTarget) {
          sourceSets.getByName("${name}Main").dependsOn(nativeMain)
          sourceSets.getByName("${name}Test").dependsOn(nativeTest)
        }
      }

      // linking fails for the linux test build if not built on a linux host
      // ensure the tests and linking for them is only done on linux hosts
//    if (!HostManager.hostIsLinux) {
//      project.tasks.findByName("linuxX64Test")?.enabled = false
//      project.tasks.findByName("linkDebugTestLinuxX64")?.enabled = false
//    }
//
//    if (!HostManager.hostIsMingw) {
//      project.tasks.findByName("mingwX64Test")?.enabled = false
//      project.tasks.findByName("linkDebugTestMingwX64")?.enabled = false
//    }
    }
  } else {
    targets {
      jvm()
    }
  }
}
