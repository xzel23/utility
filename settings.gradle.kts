
@file:Suppress("UnstableApiUsage")

import org.gradle.internal.extensions.stdlib.toDefaultLowerCase

// define project name and version
rootProject.name = "dua3-utility"
val projectVersion = "20.0.0-SNAPSHOT"

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
include("utility-samples:utility-samples-graphics")
include("utility-samples:utility-samples-fx")

plugins {
    id("org.gradle.toolchains.foojay-resolver") version "1.0.0"
}

toolchainManagement {
    jvm {
        javaRepositories {
            repository("foojay") {
                resolverClass.set(org.gradle.toolchains.foojay.FoojayToolchainResolver::class.java)
            }
        }
    }
}

// define dependency versions and repositories
dependencyResolutionManagement {

    val isSnapshot = projectVersion.toDefaultLowerCase().contains("snapshot")

    if (isSnapshot) {
        println("SNAPSHOT version detected, using local Maven repository")
    }

    versionCatalogs {
        create("libs") {
            version("projectVersion", projectVersion)

            plugin("versions", "com.github.ben-manes.versions").version("0.52.0")
            plugin("test-logger", "com.adarshr.test-logger").version("4.0.0")
            plugin("spotbugs", "com.github.spotbugs").version("6.2.0")
            plugin("cabe", "com.dua3.cabe").version("3.1.0")
            plugin("jmh", "me.champeau.jmh").version("0.7.3")
            plugin("forbiddenapis", "de.thetaphi.forbiddenapis").version("3.9")
            plugin("foojay-resolver-convention", "org.gradle.toolchains.foojay-resolver-convention").version("0.10.0")
            plugin("sonar", "org.sonarqube").version("6.2.0.5505")
            plugin("javafx", "org.openjfx.javafxplugin").version("0.1.0")

            version("h2database", "2.3.232")
            version("ikonli", "12.4.0")
            version("javafx", "23.0.2") // IMPORTANT: JavaFX 23 is the last version to support Java 21!
            version("jimfs", "1.3.0")
            version("jmh", "1.37")
            version("jspecify", "1.0.0")
            version("junit4", "4.13.2")
            version("log4j-bom", "2.24.3")
            version("miglayout", "11.4.2")
            version("mockito", "5.18.0")
            version("slf4j", "2.0.17")
            version("spotbugs", "4.9.3")
            version("bouncycastle", "1.81")

            library("jspecify", "org.jspecify", "jspecify").versionRef("jspecify")

            library("slf4j-api", "org.slf4j", "slf4j-api").versionRef("slf4j")
            library("slf4j-simple", "org.slf4j", "slf4j-simple").versionRef("slf4j")
            library("jul-to-slf4j", "org.slf4j", "jul-to-slf4j").versionRef("slf4j")

            library("miglayout-swing", "com.miglayout", "miglayout-swing").versionRef("miglayout")

            library("log4j-bom", "org.apache.logging.log4j", "log4j-bom").versionRef("log4j-bom")
            library("log4j-api", "org.apache.logging.log4j", "log4j-api").withoutVersion()
            library("log4j-core", "org.apache.logging.log4j", "log4j-core").withoutVersion()
            library("log4j-jul", "org.apache.logging.log4j", "log4j-jul").withoutVersion()
            library("log4j-jcl", "org.apache.logging.log4j", "log4j-jcl").withoutVersion()
            library("log4j-slf4j2", "org.apache.logging.log4j", "log4j-slf4j2-impl").withoutVersion()
            library("log4j-to-slf4j", "org.apache.logging.log4j", "log4j-to-slf4j").withoutVersion()

            library("ikonli-fontawesome", "org.kordamp.ikonli", "ikonli-fontawesome-pack").versionRef("ikonli")
            library("ikonli-javafx", "org.kordamp.ikonli", "ikonli-javafx").versionRef("ikonli")

            library("jimfs", "com.google.jimfs", "jimfs").versionRef("jimfs")
            library("mockito", "org.mockito", "mockito-core").versionRef("mockito")
            library("mockito-junit-jupiter", "org.mockito", "mockito-junit-jupiter").versionRef("mockito")

            library("junit4", "junit", "junit").versionRef("junit4")
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
