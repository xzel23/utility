// define project name and version
rootProject.name = "dua3-utility"
val projectVersion = "12.0.0-beta7-SNAPSHOT"

// define subprojects
include("utility")
include("utility-db")
include("utility-swing")
include("utility-logging")
include("utility-logging:utility-logging-slf4j")
include("utility-logging:utility-logging-log4j")
include("utility-samples:utility-samples-slf4j")
include("utility-samples:utility-samples-log4j")

// use the foojay-resolver-convention plugin to add JVM toolchain repository
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.7.0"
}

// define dependency versions and repositories
dependencyResolutionManagement {

    val isSnapshot = projectVersion.endsWith("SNAPSHOT")

    versionCatalogs {
        create("libs") {
            version("projectVersion", projectVersion)

            plugin("versions", "com.github.ben-manes.versions").version("0.50.0")
            plugin("test-logger", "com.adarshr.test-logger").version("4.0.0")
            plugin("spotbugs", "com.github.spotbugs").version("6.0.2")
            plugin("cabe", "com.dua3.cabe").version("1.3.0")

            version("cabe", "1.0.0")
            version("junit", "5.10.1")
            version("log4j", "2.22.0")
            version("slf4j", "2.0.9")
            version("jimfs", "1.2")
            version("miglayout", "11.2")
            version("spotbugs", "4.8.2")

            library("cabe-annotations", "com.dua3.cabe", "cabe-annotations").versionRef("cabe")

            library("slf4j-api", "org.slf4j", "slf4j-api").versionRef("slf4j")
            library("slf4j-simple", "org.slf4j", "slf4j-simple").versionRef("slf4j")
            library("jul-to-slf4j", "org.slf4j", "jul-to-slf4j").versionRef("slf4j")

            library("miglayout-swing", "com.miglayout", "miglayout-swing").versionRef("miglayout")

            library("log4j-api", "org.apache.logging.log4j", "log4j-api").versionRef("log4j")
            library("log4j-core", "org.apache.logging.log4j", "log4j-core").versionRef("log4j")
            library("log4j-jul", "org.apache.logging.log4j", "log4j-jul").versionRef("log4j")
            library("log4j-slf4j2", "org.apache.logging.log4j", "log4j-slf4j2-impl").versionRef("log4j")
            library("log4j-to-slf4j", "org.apache.logging.log4j", "log4j-to-slf4j").versionRef("log4j")

            library("junit-jupiter-api", "org.junit.jupiter", "junit-jupiter-api").versionRef("junit")
            library("junit-jupiter-engine", "org.junit.jupiter", "junit-jupiter-engine").versionRef("junit")
            library("junit-jupiter-params", "org.junit.jupiter", "junit-jupiter-params").versionRef("junit")

            library("jimfs", "com.google.jimfs", "jimfs").version("1.3.0")
        }
    }

    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {

        // Maven Central Repository
        mavenCentral()

        // Sonatype Releases
        maven {
            name = "oss.sonatype.org-releases"
            url = java.net.URI("https://s01.oss.sonatype.org/content/repositories/releases/")
            mavenContent {
                releasesOnly()
            }
        }

        // Apache releases
        maven {
            name = "apache-releases"
            url = java.net.URI("https://repository.apache.org/content/repositories/releases/")
            mavenContent {
                releasesOnly()
            }
        }

        if (isSnapshot) {
            // local maven repository
            mavenLocal()

            // Sonatype Snapshots
            maven {
                name = "oss.sonatype.org-snapshots"
                url = java.net.URI("https://s01.oss.sonatype.org/content/repositories/snapshots/")
                mavenContent {
                    snapshotsOnly()
                }
            }

            // Apache staging
            maven {
                name = "apache-staging"
                url = java.net.URI("https://repository.apache.org/content/repositories/staging/")
                mavenContent {
                    releasesOnly()
                }
            }

            // Apache snapshots
            maven {
                name = "apache-snapshots"
                url = java.net.URI("https://repository.apache.org/content/repositories/snapshots/")
                mavenContent {
                    snapshotsOnly()
                }
            }
        }
    }

}
