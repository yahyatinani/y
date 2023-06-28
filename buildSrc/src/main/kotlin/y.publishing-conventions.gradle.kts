import io.github.yahyatinani.y.Ci

plugins {
  signing
  `java-library`
  `maven-publish`
}

val javadoc = tasks.named("javadoc")

val javadocJar by tasks.creating(Jar::class) {
  group = JavaBasePlugin.DOCUMENTATION_GROUP
  description = "Assembles java doc to jar"
  archiveClassifier.set("javadoc")
  from(javadoc)
}

publishing {
  publications.withType<MavenPublication>().forEach {
    it.apply {
      artifact(javadocJar)
    }
  }
}

val ossrhUsername: String by project
val ossrhPassword: String by project
val signingKey: String? by project
val signingPassword: String? by project

val publications: PublicationContainer =
  (extensions.getByName("publishing") as PublishingExtension).publications

signing {
  useGpgCmd()
  if (signingKey != null && signingPassword != null)
    @Suppress("UnstableApiUsage")
    useInMemoryPgpKeys(signingKey, signingPassword)

  if (Ci.isRelease)
    sign(publications)
}

publishing {
  repositories {
    maven {
      val base = "https://s01.oss.sonatype.org"
      val releasesUrl = uri("$base/service/local/staging/deploy/maven2/")
      val snapshotsUrl = uri("$base/content/repositories/snapshots/")

      name = "deploy"

      url = when {
        Ci.isRelease -> releasesUrl
        else -> snapshotsUrl
      }

      credentials {
        username = System.getenv("OSSRH_USERNAME") ?: ossrhUsername
        password = System.getenv("OSSRH_PASSWORD") ?: ossrhPassword
      }
    }
  }

  publications.withType<MavenPublication>().forEach {
    it.apply {
      pom {
        val devUrl = "https://github.com/yahyatinani"
        val libUrl = "$devUrl/y"

        name.set("y")
        description.set("Functional Programming Library In Kotlin")
        url.set(libUrl)

        licenses {
          license {
            name.set("Eclipse Public License - v 1.0")
            url.set("https://opensource.org/licenses/EPL-1.0")
          }
        }

        developers {
          developer {
            id.set("yahyatinani")
            name.set("Yahya Tinani")
            email.set("yahyatinani@gmail.com")
          }
        }

        scm {
          connection.set("scm:git:$libUrl")
          developerConnection.set("scm:git:$devUrl")
          url.set(libUrl)
        }
      }
    }
  }
}
