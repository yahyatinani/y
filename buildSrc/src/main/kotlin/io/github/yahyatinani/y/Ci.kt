package io.github.yahyatinani.y

object Ci {
  private const val snapshotBase = "0.5.0"

  private fun githubBuildNumber() = System.getenv("GITHUB_RUN_NUMBER")

  private fun snapshotVersion(): String = when (val n = githubBuildNumber()) {
    null -> "$snapshotBase-LOCAL"
    else -> "$snapshotBase.$n-SNAPSHOT"
  }

  private fun releaseVersion() = System.getenv("RELEASE_VERSION")

  val isRelease get() = releaseVersion() != null

  val publishVersion get() = releaseVersion() ?: snapshotVersion()

  const val JVM_ONLY = "jvmOnly"
}
