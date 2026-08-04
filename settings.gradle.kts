@file:Suppress("UnstableApiUsage")

import org.gradle.internal.extensions.stdlib.toDefaultLowerCase

private val publishableModuleNames = listOf(
    "utility",
    "utility-db",
    "utility-swing",
    "utility-fx",
    "utility-fx-icons",
    "utility-fx-icons-ikonli",
    "utility-fx-controls",
    "utility-fx-db",
    "utility-fx-web"
)

private data class ReleaseVersions(
    val bomVersion: String,
    val moduleVersions: Map<String, String>,
    val selectedModules: Set<String>
)

/**
 * Reads the small, deliberately constrained TOML subset used by the release files.
 * Release files contain only string, boolean, and array values in [release] and
 * [modules.<project-name>] tables, so adding a TOML library to the settings
 * classpath is unnecessary.
 */
private fun readReleaseVersions(file: File, requireSelection: Boolean): ReleaseVersions {
    require(file.isFile) { "release file does not exist: ${file.path}" }

    var section = ""
    val values = mutableMapOf<String, MutableMap<String, String>>()
    val tablePattern = Regex("""^\[([A-Za-z0-9_.-]+)]$""")
    val valuePattern = Regex("""^([A-Za-z][A-Za-z0-9_-]*)\s*=\s*(.+)$""")

    file.forEachLine { rawLine ->
        val line = rawLine.substringBefore('#').trim()
        if (line.isEmpty()) {
            return@forEachLine
        }
        tablePattern.matchEntire(line)?.let {
            section = it.groupValues[1]
            values.getOrPut(section) { mutableMapOf() }
            return@forEachLine
        }
        valuePattern.matchEntire(line)?.let {
            require(section.isNotEmpty()) { "value outside a TOML table in ${file.path}: $line" }
            values.getOrPut(section) { mutableMapOf() }[it.groupValues[1]] = it.groupValues[2].trim()
                .removeSurrounding("\"")
            return@forEachLine
        }
        throw GradleException("unsupported release TOML syntax in ${file.path}: $line")
    }

    val release = values["release"] ?: throw GradleException("[release] table missing from ${file.path}")
    val bomVersion = release["bomVersion"] ?: throw GradleException("release.bomVersion missing from ${file.path}")
    val moduleVersions = publishableModuleNames.associateWith { moduleName ->
        values["modules.$moduleName"]?.get("version")
            ?: throw GradleException("modules.$moduleName.version missing from ${file.path}")
    }
    val selectedModules = if (requireSelection) {
        publishableModuleNames.filter { moduleName ->
            values["modules.$moduleName"]?.get("selected")?.toBooleanStrictOrNull()
                ?: throw GradleException("modules.$moduleName.selected missing or invalid in ${file.path}")
        }.toSet()
    } else {
        emptySet()
    }

    return ReleaseVersions(bomVersion, moduleVersions, selectedModules)
}

pluginManagement {
    val versionsPluginVersion = Regex("""(?m)^\s*versions-plugin\s*=\s*"([^"]+)"""")
        .find(file("gradle/version.toml").readText())!!.groupValues[1]
    plugins {
        id("io.github.ben-manes.versions.settings") version versionsPluginVersion
    }
}

plugins {
    id("io.github.ben-manes.versions.settings")
}

// define project name
rootProject.name = "dua3-utility"

fun versionCatalogVersion(alias: String): String {
    val catalog = file("gradle/version.toml")
    val versions = catalog.readLines()
        .dropWhile { it.trim() != "[versions]" }
        .drop(1)
        .takeWhile { !it.trim().startsWith("[") }

    val versionDeclaration = Regex("""^\s*${Regex.escape(alias)}\s*=\s*"([^"]+)"\s*(?:#.*)?$""")
    return versions.firstNotNullOfOrNull { line ->
        versionDeclaration.matchEntire(line)?.groupValues?.get(1)
    } ?: throw GradleException("version '$alias' not found in ${catalog.path}")
}

private val developmentVersion = versionCatalogVersion("projectVersion")
private val publishedReleaseStateFile = file("gradle/release-state.toml")
private val publishedRelease = readReleaseVersions(publishedReleaseStateFile, requireSelection = false)
private val preparedReleasePlanFile = file("gradle/prepared-release.toml")
private val preparedRelease = preparedReleasePlanFile.takeIf(File::isFile)?.let {
    readReleaseVersions(it, requireSelection = true)
}
private val effectiveBomVersion = preparedRelease?.bomVersion ?: developmentVersion
private val effectiveModuleVersions = preparedRelease?.moduleVersions
    ?: publishableModuleNames.associateWith { developmentVersion }
private val selectedReleaseModules = preparedRelease?.selectedModules ?: emptySet()

gradle.extra["releaseStateFile"] = publishedReleaseStateFile
gradle.extra["publishedReleaseBomVersion"] = publishedRelease.bomVersion
gradle.extra["publishedReleaseModuleVersions"] = publishedRelease.moduleVersions
gradle.extra["preparedReleasePlanFile"] = preparedReleasePlanFile
gradle.extra["releaseBomVersion"] = effectiveBomVersion
gradle.extra["releaseModuleVersions"] = effectiveModuleVersions
gradle.extra["releasePlanPresent"] = (preparedRelease != null)
gradle.extra["releaseSelectedModules"] = selectedReleaseModules

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
include("utility-samples")
include("utility-samples:utility-samples-graphics")
include("utility-samples:utility-samples-fx")

gradle.projectsLoaded {
    rootProject.allprojects {
        version = effectiveModuleVersions[name] ?: effectiveBomVersion
    }
}

// define dependency versions and repositories
dependencyResolutionManagement {

    val isSnapshot = effectiveBomVersion.toDefaultLowerCase().contains("-snapshot")
    val isReleaseCandidate = !isSnapshot && effectiveBomVersion.toDefaultLowerCase().contains("-rc")

    if (isSnapshot && !effectiveBomVersion.endsWith("-SNAPSHOT")) {
        throw GradleException("inconsistent version definition: $effectiveBomVersion does not end with SNAPSHOT")
    }

    versionCatalogs {
        create("libs") {
            from(files("gradle/version.toml"))
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

            mavenLocal()

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
