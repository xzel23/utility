import org.gradle.internal.extensions.stdlib.toDefaultLowerCase

// define project name and version
rootProject.name = "dua3-utility"
val projectVersion = "14-rc-1"

// define subprojects
include("utility")
include("utility-db")
include("utility-swing")
include("utility-fx")
include("utility-fx-icons")
include("utility-fx-icons-ikonli")
include("utility-fx-controls")
include("utility-fx-db")
include("utility-fx-web")
include("utility-logging")
include("utility-logging-slf4j")
include("utility-logging-log4j")
include("utility-samples:utility-samples-slf4j")
include("utility-samples:utility-samples-log4j")
include("utility-samples:utility-samples-geometry")
include("utility-samples:utility-samples-fx")

// use the foojay-resolver-convention plugin to add JVM toolchain repository
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

// define dependency versions and repositories
dependencyResolutionManagement {

    val isSnapshot = projectVersion.toDefaultLowerCase().contains("snapshot")

    if (isSnapshot) {
        System.out.println("SNAPSHOT version detected, using local Maven repository")
    }

    versionCatalogs {
        create("libs") {
            version("projectVersion", projectVersion)

            plugin("versions", "com.github.ben-manes.versions").version("0.51.0")
            plugin("test-logger", "com.adarshr.test-logger").version("4.0.0")
            plugin("spotbugs", "com.github.spotbugs").version("6.0.26")
            plugin("cabe", "com.dua3.cabe").version("3.0-beta-10")

            version("log4j", "2.24.1")
            version("slf4j", "2.0.16")
            version("ikonli", "12.3.1")
            version("jimfs", "1.3.0")
            version("miglayout", "11.4.2")
            version("mockito", "5.14.2")
            version("javafx", "22")
            version("jspecify", "1.0.0")

            library("jspecify", "org.jspecify", "jspecify").versionRef("jspecify")

            library("slf4j-api", "org.slf4j", "slf4j-api").versionRef("slf4j")
            library("slf4j-simple", "org.slf4j", "slf4j-simple").versionRef("slf4j")
            library("jul-to-slf4j", "org.slf4j", "jul-to-slf4j").versionRef("slf4j")

            library("miglayout-swing", "com.miglayout", "miglayout-swing").versionRef("miglayout")

            library("log4j-api", "org.apache.logging.log4j", "log4j-api").versionRef("log4j")
            library("log4j-core", "org.apache.logging.log4j", "log4j-core").versionRef("log4j")
            library("log4j-jul", "org.apache.logging.log4j", "log4j-jul").versionRef("log4j")
            library("log4j-jcl", "org.apache.logging.log4j", "log4j-jcl").versionRef("log4j")
            library("log4j-slf4j2", "org.apache.logging.log4j", "log4j-slf4j2-impl").versionRef("log4j")
            library("log4j-to-slf4j", "org.apache.logging.log4j", "log4j-to-slf4j").versionRef("log4j")

            library("ikonli-fontawesome", "org.kordamp.ikonli", "ikonli-fontawesome-pack").versionRef("ikonli")
            library("ikonli-javafx", "org.kordamp.ikonli", "ikonli-javafx").versionRef("ikonli")

            library("jimfs", "com.google.jimfs", "jimfs").versionRef("jimfs")
            library("mockito", "org.mockito", "mockito-core").versionRef("mockito")
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
