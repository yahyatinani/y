rootProject.name = "buildSrc"

dependencyResolutionManagement {
  versionCatalogs {
    create("deps") {
      from(files("../gradle/libs.versions.toml"))
    }
  }
}
