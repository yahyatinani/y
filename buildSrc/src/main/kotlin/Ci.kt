object Ci {
    private const val snapshotBase = "0.0.3"

    private fun githubBuildNumber() = System.getenv("GITHUB_RUN_NUMBER")

    private fun snapshotVersion(): String = when (val n = githubBuildNumber()) {
        null -> "$snapshotBase-LOCAL"
        else -> "$snapshotBase.$n-SNAPSHOT"
    }

    private fun releaseVersion() = System.getenv("RELEASE_VERSION")

    fun isRelease() = releaseVersion() != null

    fun publishVersion() = releaseVersion() ?: snapshotVersion()
}
