apply(plugin = "java")
apply(plugin = "java-library")
apply(plugin = "maven-publish")
apply(plugin = "signing")

repositories {
    mavenCentral()
}

val ossrhUsername: String by project
val ossrhPassword: String by project
val signingKey: String? by project
val signingPassword: String? by project

fun Project.publishing(action: PublishingExtension.() -> Unit) =
    configure(action)

fun Project.signing(configure: SigningExtension.() -> Unit): Unit =
    configure(configure)

val javadoc = tasks.named("javadoc")

val javadocToJar by tasks.creating(Jar::class) {
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    description = "Assembles java doc to jar"
    archiveClassifier.set("javadoc")
    from(javadoc)
}

//val sourcesJar by tasks.creating(Jar::class) {
//    val sourceSets: SourceSetContainer by project
//
//    archiveClassifier.set("sources")
//    from(sourceSets["main"].allSource)
//}

val pubs: PublicationContainer =
    (extensions.getByName("publishing") as PublishingExtension).publications

publishing {
    repositories {
        maven {
            val base = "https://oss.sonatype.org"
            val releasesUrl = uri("$base/service/local/staging/deploy/maven2/")
            val snapshotsUrl = uri("$base/content/repositories/snapshots/")

            name = "deploy"

            url = if (Ci.isRelease) releasesUrl else snapshotsUrl

            credentials {
                username = System.getenv("OSSRH_USERNAME") ?: ossrhUsername
                password = System.getenv("OSSRH_PASSWORD") ?: ossrhPassword
            }
        }
    }

    pubs.withType<MavenPublication>().forEach {
        it.apply {
            artifact(javadocToJar)

            pom {
                val devUrl = "http://github.com/whyrising/"
                val libUrl = "$devUrl/y"

                name.set("Y")
                description.set("Functional Programming Library In Kotlin")
                url.set(libUrl)

                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("https://opensource.org/licenses/Apache-2.0")
                    }
                }

                developers {
                    developer {
                        id.set("whyrising")
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

signing {
    useGpgCmd()
    if (signingKey != null && signingPassword != null) {
        @Suppress("UnstableApiUsage")
        useInMemoryPgpKeys(signingKey, signingPassword)
    }

    if (Ci.isRelease) sign(pubs)
}
