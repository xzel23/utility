@file:Suppress("UnstableApiUsage")

import org.gradle.internal.extensions.stdlib.toDefaultLowerCase

// define project name and version
rootProject.name = "dua3-utility"
val projectVersion = "20.0.0-rc-9"

// define subprojects
include("utility")
include("utility-bom")
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
include("utility-samples")
include("utility-samples:utility-samples-slf4j")
include("utility-samples:utility-samples-log4j")
include("utility-samples:utility-samples-graphics")
include("utility-samples:utility-samples-fx")

plugins {
    id("org.gradle.toolchains.foojay-resolver") version "1.0.0"
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

// define dependency versions and repositories
dependencyResolutionManagement {

    val isSnapshot = projectVersion.toDefaultLowerCase().contains("-snapshot")
    val isReleaseCandidate = !isSnapshot && projectVersion.toDefaultLowerCase().contains("-rc")

    if (isSnapshot && !projectVersion.endsWith("-SNAPSHOT")) {
        throw GradleException("inconsistent version definition: $projectVersion does not end with SNAPSHOT")
    }

    versionCatalogs {
        create("libs") {
            version("projectVersion", projectVersion)

            plugin("cabe", "com.dua3.cabe").version("3.3.0")
            plugin("forbiddenapis", "de.thetaphi.forbiddenapis").version("3.10")
            plugin("sonar", "org.sonarqube").version("7.0.1.6134")
            plugin("javafx", "org.openjfx.javafxplugin").version("0.1.0")
            plugin("jmh", "me.champeau.jmh").version("0.7.3")
            plugin("jreleaser", "org.jreleaser").version("1.21.0")
            plugin("spotbugs", "com.github.spotbugs").version("6.4.5")
            plugin("test-logger", "com.adarshr.test-logger").version("4.0.0")
            plugin("versions", "com.github.ben-manes.versions").version("0.53.0")

            version("atlantafx", "2.1.0")
            version("h2database", "2.4.240")
            version("ikonli", "12.4.0")
            version("javafx", "23.0.2") // IMPORTANT: JavaFX 23 is the last version to support Java 21!
            version("jimfs", "1.3.1")
            version("jmh", "1.37")
            version("jspecify", "1.0.0")
            version("junit-bom", "6.0.1")
            version("log4j-bom", "2.25.2")
            version("miglayout", "11.4.2")
            version("slf4j", "2.0.17")
            version("spotbugs", "4.9.8")
            version("bouncycastle", "1.82")
            version("utility-bom", projectVersion)

            library("atlantafx", "io.github.mkpaz", "atlantafx-base").versionRef("atlantafx")

            library("jspecify", "org.jspecify", "jspecify").versionRef("jspecify")

            library("slf4j-api", "org.slf4j", "slf4j-api").versionRef("slf4j")
            library("slf4j-simple", "org.slf4j", "slf4j-simple").versionRef("slf4j")
            library("jul-to-slf4j", "org.slf4j", "jul-to-slf4j").versionRef("slf4j")

            library("miglayout-swing", "com.miglayout", "miglayout-swing").versionRef("miglayout")

            library("utility-bom", "com.dua3.utility", "utility-bom").versionRef("utility-bom")

            library("log4j-bom", "org.apache.logging.log4j", "log4j-bom").versionRef("log4j-bom")
            library("log4j-api", "org.apache.logging.log4j", "log4j-api").withoutVersion()
            library("log4j-core", "org.apache.logging.log4j", "log4j-core").withoutVersion()
            library("log4j-jul", "org.apache.logging.log4j", "log4j-jul").withoutVersion()
            library("log4j-jcl", "org.apache.logging.log4j", "log4j-jcl").withoutVersion()
            library("log4j-slf4j2", "org.apache.logging.log4j", "log4j-slf4j2-impl").withoutVersion()
            library("log4j-to-slf4j", "org.apache.logging.log4j", "log4j-to-slf4j").withoutVersion()

            library("ikonli-fontawesome6", "org.kordamp.ikonli", "ikonli-fontawesome6-pack").versionRef("ikonli")
            library("ikonli-javafx", "org.kordamp.ikonli", "ikonli-javafx").versionRef("ikonli")

            library("jimfs", "com.google.jimfs", "jimfs").versionRef("jimfs")

            library("junit-bom", "org.junit", "junit-bom").versionRef("junit-bom")
            library("junit-jupiter-api", "org.junit.jupiter", "junit-jupiter-api").withoutVersion()
            library("junit-jupiter-engine", "org.junit.jupiter", "junit-jupiter-engine").withoutVersion()
            library("junit-platform-launcher", "org.junit.platform", "junit-platform-launcher").withoutVersion()
            library("h2", "com.h2database", "h2").versionRef("h2database")
            library("bouncycastle-provider", "org.bouncycastle", "bcprov-jdk18on").versionRef("bouncycastle")
            library("bouncycastle-pkix", "org.bouncycastle", "bcpkix-jdk18on").versionRef("bouncycastle")
        }
    }

    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {

        // Maven Central Repository
        mavenCentral()

        // Sonatype Releases
        maven {
            name = "central.sonatype.com-releases"
            url = java.net.URI("https://oss.sonatype.org/content/repositories/releases/")
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
            println("snapshot version detected, adding Maven snapshot repositories")

            // Sonatype Snapshots
            maven {
                name = "Central Portal Snapshots"
                url = java.net.URI("https://central.sonatype.com/repository/maven-snapshots/")
                mavenContent {
                    snapshotsOnly()
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

        if (isReleaseCandidate) {
            println("release candidate version detected, adding Maven staging repositories")

            // Apache staging
            maven {
                name = "apache-staging"
                url = java.net.URI("https://repository.apache.org/content/repositories/staging/")
                mavenContent {
                    releasesOnly()
                }
            }
        }
    }

}
