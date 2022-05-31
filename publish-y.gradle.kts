apply(plugin = "java")
apply(plugin = "java-library")

apply(from = "$rootDir/signing-pom-details.gradle.kts")

val javadoc = tasks.named("javadoc")

val javadocToJar by tasks.creating(Jar::class) {
  group = JavaBasePlugin.DOCUMENTATION_GROUP
  description = "Assembles java doc to jar"
  archiveClassifier.set("javadoc")
  from(javadoc)
}

// val sourcesJar by tasks.creating(Jar::class) {
//     archiveClassifier.set("sources")
//     val sourceSets: SourceSetContainer by project
//     from(sourceSets.getByName("main").allSource)
// }

fun Project.publishing(action: PublishingExtension.() -> Unit) =
  configure(action)

val publications: PublicationContainer =
  (extensions.getByName("publishing") as PublishingExtension).publications

publishing {
  publications.withType<MavenPublication>().forEach {
    it.apply {
      artifact(javadocToJar)
    }
  }
}
