// Copyright (c) 2019, 2022 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

@file:Suppress("UnstableApiUsage")

import com.adarshr.gradle.testlogger.theme.ThemeType
import com.dua3.utility.release.PrepareReleaseTask
import com.dua3.cabe.processor.Configuration
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.HttpURLConnection
import java.net.URI
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.security.MessageDigest
import java.util.Properties
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.external.javadoc.StandardJavadocDocletOptions
import org.gradle.internal.extensions.stdlib.toDefaultLowerCase
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JvmVendorSpec
import org.gradle.plugins.signing.Sign

plugins {
    id("java-library")
    id("jvm-test-suite")
    id("version-catalog")
    id("signing")
    id("idea")
    id("jacoco-report-aggregation")
    alias(libs.plugins.jdk)
    alias(libs.plugins.test.logger)
    alias(libs.plugins.spotbugs)
    alias(libs.plugins.cabe)
    alias(libs.plugins.jmh)
    alias(libs.plugins.sonar)
    alias(libs.plugins.jreleaser)
}

/////////////////////////////////////////////////////////////////////////////
// Meta data object
/////////////////////////////////////////////////////////////////////////////
object Meta {
    const val DESCRIPTION = "Utility library for Java"
    const val INCEPTION_YEAR = "2019"
    const val GROUP = "com.dua3.utility"
    const val SCM = "https://github.com/xzel23/utility.git"
    const val LICENSE_NAME = "MIT"
    const val LICENSE_URL = "https://opensource.org/licenses/MIT"
    const val DEVELOPER_ID = "axh"
    const val DEVELOPER_NAME = "Axel Howind"
    const val DEVELOPER_EMAIL = "axh@dua3.com"
    const val ORGANIZATION_NAME = "dua3"
    const val ORGANIZATION_URL = "https://www.dua3.com"
}

/////////////////////////////////////////////////////////////////////////////
// Selective release model
/////////////////////////////////////////////////////////////////////////////

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

private data class ModuleReleaseState(
    val version: String,
    val publishedRevision: String,
    val paths: List<String>
)

private data class PublishedReleaseState(
    val bomVersion: String,
    val bomPublishedRevision: String,
    val modules: Map<String, ModuleReleaseState>
)

private data class PreparedReleaseModule(
    val version: String,
    val sourceRevision: String,
    val selected: Boolean,
    val reason: String
)

private data class PreparedReleasePlan(
    val releaseType: String,
    val bomVersion: String,
    val sourceRevision: String,
    val modules: Map<String, PreparedReleaseModule>
)

private data class SemanticVersion(val major: Int, val minor: Int, val patch: Int) {
    override fun toString(): String = "$major.$minor.$patch"
}

private data class CommandResult(val exitValue: Int, val output: String)

@Suppress("UNCHECKED_CAST")
private val configuredReleaseModuleVersions = gradle.extra["releaseModuleVersions"] as Map<String, String>
@Suppress("UNCHECKED_CAST")
private val configuredSelectedReleaseModules = gradle.extra["releaseSelectedModules"] as Set<String>
private val releasePlanPresent = gradle.extra["releasePlanPresent"] as Boolean
private val releaseStateFile = gradle.extra["releaseStateFile"] as File
private val preparedReleasePlanFile = gradle.extra["preparedReleasePlanFile"] as File

private fun parseToml(file: File): Map<String, Map<String, String>> {
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
    return values
}

private fun parseTomlStringArray(value: String): List<String> =
    Regex(""""((?:\\.|[^"\\])*)"""").findAll(value).map { match ->
        match.groupValues[1].replace("\\\"", "\"").replace("\\\\", "\\")
    }.toList()

private fun readPublishedReleaseState(file: File): PublishedReleaseState {
    val values = parseToml(file)
    val release = values["release"] ?: throw GradleException("[release] table missing from ${file.path}")
    val bomVersion = release["bomVersion"] ?: throw GradleException("release.bomVersion missing from ${file.path}")
    val bomPublishedRevision = release["publishedRevision"]
        ?: throw GradleException("release.publishedRevision missing from ${file.path}")
    val modules = publishableModuleNames.associateWith { moduleName ->
        val module = values["modules.$moduleName"]
            ?: throw GradleException("[modules.$moduleName] table missing from ${file.path}")
        ModuleReleaseState(
            version = module["version"]
                ?: throw GradleException("modules.$moduleName.version missing from ${file.path}"),
            publishedRevision = module["publishedRevision"]
                ?: throw GradleException("modules.$moduleName.publishedRevision missing from ${file.path}"),
            paths = parseTomlStringArray(module["paths"]
                ?: throw GradleException("modules.$moduleName.paths missing from ${file.path}"))
        )
    }
    return PublishedReleaseState(bomVersion, bomPublishedRevision, modules)
}

private fun readPreparedReleasePlan(file: File): PreparedReleasePlan {
    val values = parseToml(file)
    val release = values["release"] ?: throw GradleException("[release] table missing from ${file.path}")
    val releaseType = release["releaseType"] ?: throw GradleException("release.releaseType missing from ${file.path}")
    val bomVersion = release["bomVersion"] ?: throw GradleException("release.bomVersion missing from ${file.path}")
    val sourceRevision = release["sourceRevision"]
        ?: throw GradleException("release.sourceRevision missing from ${file.path}")
    val modules = publishableModuleNames.associateWith { moduleName ->
        val module = values["modules.$moduleName"]
            ?: throw GradleException("[modules.$moduleName] table missing from ${file.path}")
        PreparedReleaseModule(
            version = module["version"]
                ?: throw GradleException("modules.$moduleName.version missing from ${file.path}"),
            sourceRevision = module["sourceRevision"]
                ?: throw GradleException("modules.$moduleName.sourceRevision missing from ${file.path}"),
            selected = module["selected"]?.toBooleanStrictOrNull()
                ?: throw GradleException("modules.$moduleName.selected missing or invalid in ${file.path}"),
            reason = module["reason"] ?: throw GradleException("modules.$moduleName.reason missing from ${file.path}")
        )
    }
    return PreparedReleasePlan(releaseType, bomVersion, sourceRevision, modules)
}

private fun parseSemanticVersion(value: String): SemanticVersion {
    val match = Regex("""^(0|[1-9]\d*)\.(0|[1-9]\d*)\.(0|[1-9]\d*)$""").matchEntire(value)
        ?: throw GradleException("release version must be a stable major.minor.patch value: $value")
    return SemanticVersion(match.groupValues[1].toInt(), match.groupValues[2].toInt(), match.groupValues[3].toInt())
}

private fun runGit(vararg arguments: String): CommandResult {
    val output = ByteArrayOutputStream()
    val process = ProcessBuilder(listOf("git") + arguments)
        .directory(rootDir)
        .redirectErrorStream(true)
        .start()
    process.inputStream.copyTo(output)
    val exitValue = process.waitFor()
    return CommandResult(exitValue, output.toString(StandardCharsets.UTF_8).trim())
}

private fun requireGitSuccess(description: String, vararg arguments: String): String {
    val result = runGit(*arguments)
    check(result.exitValue == 0) {
        "$description failed (${result.output.ifBlank { "no output" }})"
    }
    return result.output
}

private fun gitHasChanges(fromRevision: String, toRevision: String, pathspecs: List<String>): Boolean {
    val result = runGit("diff", "--quiet", fromRevision, toRevision, "--", *pathspecs.toTypedArray())
    return when (result.exitValue) {
        0 -> false
        1 -> true
        else -> throw GradleException("could not compare Git revisions: ${result.output}")
    }
}

private fun isMavenCentralCoordinatePublished(artifactId: String, version: String): Boolean {
    val path = "${Meta.GROUP.replace('.', '/')}/$artifactId/$version/$artifactId-$version.pom"
    val connection = (URI("https://repo1.maven.org/maven2/$path").toURL().openConnection() as HttpURLConnection).apply {
        requestMethod = "HEAD"
        connectTimeout = 10_000
        readTimeout = 10_000
        instanceFollowRedirects = true
    }
    return try {
        when (val responseCode = connection.responseCode) {
            HttpURLConnection.HTTP_NOT_FOUND -> false
            in 200..399 -> true
            else -> throw GradleException(
                "could not determine whether $artifactId:$version exists on Maven Central (HTTP $responseCode)"
            )
        }
    } finally {
        connection.disconnect()
    }
}

private fun tomlString(value: String): String =
    value.replace("\\", "\\\\").replace("\"", "\\\"")

private fun writePreparedReleasePlan(plan: PreparedReleasePlan) {
    val content = buildString {
        appendLine("[release]")
        appendLine("schemaVersion = 1")
        appendLine("releaseType = \"${tomlString(plan.releaseType)}\"")
        appendLine("bomVersion = \"${tomlString(plan.bomVersion)}\"")
        appendLine("sourceRevision = \"${tomlString(plan.sourceRevision)}\"")
        publishableModuleNames.forEach { moduleName ->
            val module = plan.modules.getValue(moduleName)
            appendLine()
            appendLine("[modules.$moduleName]")
            appendLine("version = \"${tomlString(module.version)}\"")
            appendLine("sourceRevision = \"${tomlString(module.sourceRevision)}\"")
            appendLine("selected = ${module.selected}")
            appendLine("reason = \"${tomlString(module.reason)}\"")
        }
    }
    Files.writeString(preparedReleasePlanFile.toPath(), content, StandardCharsets.UTF_8)
}

private fun writePublishedReleaseState(plan: PreparedReleasePlan, previousState: PublishedReleaseState) {
    val content = buildString {
        appendLine("[release]")
        appendLine("schemaVersion = 2")
        appendLine("bomVersion = \"${tomlString(plan.bomVersion)}\"")
        appendLine("publishedRevision = \"${tomlString(plan.sourceRevision)}\"")
        publishableModuleNames.forEach { moduleName ->
            val oldModule = previousState.modules.getValue(moduleName)
            val plannedModule = plan.modules.getValue(moduleName)
            val version = if (plannedModule.selected) plannedModule.version else oldModule.version
            val revision = if (plannedModule.selected) plannedModule.sourceRevision else oldModule.publishedRevision
            val paths = oldModule.paths.joinToString(", ") { "\"${tomlString(it)}\"" }
            appendLine()
            appendLine("[modules.$moduleName]")
            appendLine("version = \"${tomlString(version)}\"")
            appendLine("publishedRevision = \"${tomlString(revision)}\"")
            appendLine("paths = [$paths]")
        }
    }
    Files.writeString(releaseStateFile.toPath(), content, StandardCharsets.UTF_8)
}

private fun nextDevelopmentVersion(releaseVersion: String): String {
    val parsed = parseSemanticVersion(releaseVersion)
    return SemanticVersion(parsed.major, parsed.minor, parsed.patch + 1).toString() + "-SNAPSHOT"
}

private fun writeDevelopmentVersion(version: String) {
    val catalog = file("gradle/version.toml")
    val previous = Files.readString(catalog.toPath(), StandardCharsets.UTF_8)
    val projectVersionPattern = Regex("""(?m)^(\s*projectVersion\s*=\s*")[^"]+("\s*(?:#.*)?)$""")
    check(projectVersionPattern.containsMatchIn(previous)) {
        "projectVersion declaration not found in ${catalog.path}"
    }
    val updated = previous.replace(projectVersionPattern, $$"$1$$version$2")
    Files.writeString(catalog.toPath(), updated, StandardCharsets.UTF_8)
}

/////////////////////////////////////////////////////////////////////////////
// Root project configuration
/////////////////////////////////////////////////////////////////////////////

project.description = Meta.DESCRIPTION

val japicmpTool = configurations.create("japicmpTool") {
    isCanBeConsumed = false
    isCanBeResolved = true
}

dependencies {
    // The classifier is a self-contained CLI, which keeps compatibility checks independent of the build JVM classpath.
    add(japicmpTool.name, "com.github.siom79.japicmp:japicmp:0.26.1:jar-with-dependencies") {
        isTransitive = false
    }
}

tasks.register("printVersion") {
    description = "Print the project version to stdout."
    group = HelpTasksPlugin.HELP_GROUP
    val version = project.version.toString()
    doLast { println(version) }
}

private fun validatePreparedReleasePlan(plan: PreparedReleasePlan) {
    check(plan.releaseType in setOf("patch", "minor", "major")) {
        "unsupported prepared release type: ${plan.releaseType}"
    }
    parseSemanticVersion(plan.bomVersion)
    check(runGit("merge-base", "--is-ancestor", plan.sourceRevision, "HEAD").exitValue == 0) {
        "prepared release source revision is not available as an ancestor of HEAD: ${plan.sourceRevision}"
    }
    val selectedModules = plan.modules.filterValues { it.selected }.keys
    check(selectedModules == configuredSelectedReleaseModules) {
        "prepared release plan does not match the modules selected during Gradle configuration"
    }
    check(project.version.toString() == plan.bomVersion) {
        "configured BOM version ${project.version} does not match prepared plan ${plan.bomVersion}"
    }
    val publishedState = readPublishedReleaseState(releaseStateFile)
    publishableModuleNames.forEach { moduleName ->
        val plannedModule = plan.modules.getValue(moduleName)
        val plannedVersion = plannedModule.version
        check(configuredReleaseModuleVersions[moduleName] == plannedVersion) {
            "configured version for $moduleName does not match the prepared release plan"
        }
        if (plannedModule.selected) {
            check(plannedModule.sourceRevision == plan.sourceRevision) {
                "selected module $moduleName does not use the plan source revision"
            }
        } else {
            val publishedModule = publishedState.modules.getValue(moduleName)
            check(plannedModule.version == publishedModule.version &&
                plannedModule.sourceRevision == publishedModule.publishedRevision) {
                "retained module $moduleName does not match published release state"
            }
        }
    }
}

private fun renderReleasePlan(plan: PreparedReleasePlan) = buildString {
    appendLine("Selective release plan")
    appendLine("  type: ${plan.releaseType}")
    appendLine("  source revision: ${plan.sourceRevision}")
    appendLine("  BOM: utility-bom:${plan.bomVersion}")
    appendLine("  modules to publish:")
    plan.modules.filterValues { it.selected }.forEach { (moduleName, module) ->
        appendLine("    $moduleName:${module.version} (${module.reason})")
    }
    if (plan.modules.values.none { it.selected }) {
        appendLine("    (none; BOM-only dependency catalog release)")
    }
    appendLine("  retained modules:")
    plan.modules.filterValues { !it.selected }.forEach { (moduleName, module) ->
        appendLine("    $moduleName:${module.version}")
    }
}

tasks.register<PrepareReleaseTask>("prepareRelease") {
    group = "release"
    description = "Plans a selective release; add -PconfirmRelease=true to write gradle/prepared-release.toml."
    repositoryDirectory.set(layout.projectDirectory)
    this.releaseStateFile.set(layout.projectDirectory.file("gradle/release-state.toml"))
    this.preparedReleasePlanPath.set(layout.projectDirectory.file("gradle/prepared-release.toml").asFile.absolutePath)
    releaseType.convention(providers.gradleProperty("releaseType").orElse(""))
    requestedReleaseVersion.convention(providers.gradleProperty("releaseVersion").orElse(""))
    additionalReleaseModules.convention(providers.gradleProperty("additionalReleaseModules").orElse(""))
    confirmRelease.convention(providers.gradleProperty("confirmRelease").map { it == "true" }.orElse(false))
}

tasks.register("verifyPreparedRelease") {
    group = "release"
    description = "Validates the persisted prepared release plan and configured module versions."
    notCompatibleWithConfigurationCache(
        "Release-plan validation reads Git state and the configured multi-project build at task execution time."
    )

    doLast {
        check(releasePlanPresent) {
            "no prepared release plan exists at ${preparedReleasePlanFile.path}; run prepareRelease first"
        }
        validatePreparedReleasePlan(readPreparedReleasePlan(preparedReleasePlanFile))
        logger.lifecycle("Prepared release plan is valid.")
    }
}

tasks.register("printPreparedReleasePlan") {
    group = "release"
    description = "Prints the persisted prepared release plan."
    notCompatibleWithConfigurationCache("The task reads the persisted release plan at execution time.")

    doLast {
        check(releasePlanPresent) { "no prepared release plan exists at ${preparedReleasePlanFile.path}" }
        logger.lifecycle(renderReleasePlan(readPreparedReleasePlan(preparedReleasePlanFile)))
    }
}

private fun downloadPublishedModuleJar(moduleName: String, version: String): File {
    val target = layout.buildDirectory.file("release-compatibility/$moduleName-$version.jar").get().asFile
    if (target.isFile) {
        return target
    }
    target.parentFile.mkdirs()
    val path = "${Meta.GROUP.replace('.', '/')}/$moduleName/$version/$moduleName-$version.jar"
    URI("https://repo1.maven.org/maven2/$path").toURL().openStream().use { input ->
        Files.copy(input, target.toPath())
    }
    return target
}

private fun stagedReleaseArtifact(moduleName: String, version: String, classifier: String = ""): File {
    val filename = buildString {
        append(moduleName)
        append('-')
        append(version)
        if (classifier.isNotEmpty()) {
            append('-')
            append(classifier)
        }
        append(".jar")
    }
    return layout.buildDirectory.file(
        "staging-deploy/${Meta.GROUP.replace('.', '/')}/$moduleName/$version/$filename"
    ).get().asFile
}

tasks.register("checkReleaseCompatibility") {
    group = "verification"
    description = "Checks selected patch-release modules against their last published binary API."
    notCompatibleWithConfigurationCache(
        "Compatibility verification resolves published artifacts and invokes the external japicmp process."
    )
    if (releasePlanPresent && !prebuiltReleaseBundleMode) {
        dependsOn(configuredSelectedReleaseModules.map { moduleName -> ":$moduleName:jar" })
    }

    doLast {
        check(releasePlanPresent) {
            "no prepared release plan exists at ${preparedReleasePlanFile.path}; run prepareRelease first"
        }
        val plan = readPreparedReleasePlan(preparedReleasePlanFile)
        if (plan.releaseType != "patch") {
            logger.lifecycle("Skipping binary compatibility enforcement for ${plan.releaseType} release ${plan.bomVersion}.")
            return@doLast
        }

        val state = readPublishedReleaseState(releaseStateFile)
        val tool = japicmpTool.singleFile
        plan.modules.filterValues { it.selected }.forEach { (moduleName, module) ->
            val oldVersion = state.modules.getValue(moduleName).version
            val oldJar = downloadPublishedModuleJar(moduleName, oldVersion)
            val newJar = if (prebuiltReleaseBundleMode) {
                stagedReleaseArtifact(moduleName, module.version)
            } else {
                project(":$moduleName").layout.buildDirectory
                    .file("libs/$moduleName-${module.version}.jar").get().asFile
            }
            check(newJar.isFile) { "candidate artifact was not built: ${newJar.path}" }

            logger.lifecycle("Checking binary compatibility: $moduleName $oldVersion -> ${module.version}")
            val javaExecutable = File(System.getProperty("java.home"), "bin/java").absolutePath
            val process = ProcessBuilder(
                javaExecutable,
                "-jar",
                tool.absolutePath,
                "--old", oldJar.absolutePath,
                "--new", newJar.absolutePath,
                "--only-modified",
                "--error-on-binary-incompatibility",
                "--error-on-source-incompatibility",
                "--ignore-missing-classes"
            ).inheritIO().start()
            check(process.waitFor() == 0) { "binary compatibility check failed for $moduleName" }
        }
    }
}

// Aggregate all subprojects for JaCoCo report aggregation

dependencies {
    // Aggregate all subprojects for JaCoCo report aggregation
    jacocoAggregation(project(":"))
    jacocoAggregation(project(":utility"))
    jacocoAggregation(project(":utility-db"))
    jacocoAggregation(project(":utility-swing"))
    jacocoAggregation(project(":utility-fx"))
    jacocoAggregation(project(":utility-fx-icons"))
    jacocoAggregation(project(":utility-fx-icons-ikonli"))
    jacocoAggregation(project(":utility-fx-controls"))
    jacocoAggregation(project(":utility-fx-db"))
    jacocoAggregation(project(":utility-fx-web"))
}

tasks.named<JacocoReport>("testCodeCoverageReport") {
    reports {
        xml.required.set(true)
        html.required.set(true)
    }

    // use Cabe instrumented classes if they exist
    classDirectories.setFrom(project.provider {
        val aggregatedProjectPaths = listOf(
            ":",
            ":utility",
            ":utility-db",
            ":utility-swing",
            ":utility-fx",
            ":utility-fx-icons",
            ":utility-fx-icons-ikonli",
            ":utility-fx-controls",
            ":utility-fx-db",
            ":utility-fx-web"
        )

        aggregatedProjectPaths.flatMap { path ->
            val p = project.project(path)
            val cabeClasses = p.layout.buildDirectory.dir("classes-cabe/main").get().asFile
            val mainClasses = p.layout.buildDirectory.dir("classes/java/main").get().asFile
            if (cabeClasses.exists()) {
                listOf<File>(cabeClasses)
            } else if (mainClasses.exists()) {
                listOf<File>(mainClasses)
            } else {
                emptyList<File>()
            }
        }
    })
}

// SonarQube root project config
sonar {
    properties {
        property(
            "sonar.coverage.jacoco.xmlReportPaths",
            "${layout.buildDirectory.get()}/reports/jacoco/testCodeCoverageReport/testCodeCoverageReport.xml"
        )
        property("sonar.coverage.exclusions", "**/samples/**")

        // use Cabe instrumented classes if they exist
        val cabeClassesDir = project.layout.buildDirectory.dir("classes-cabe/main").get().asFile
        if (cabeClassesDir.exists()) {
            property("sonar.java.binaries", "build/classes-cabe/main")
        }
    }
}

// check for development/release version
fun isDevelopmentVersion(versionString: String): Boolean {
    val v = versionString.toDefaultLowerCase()
    val markers = listOf("snapshot", "alpha", "beta")
    return markers.any { marker -> v.contains("-$marker") || v.contains(".$marker") }
}

val isReleaseVersion = !isDevelopmentVersion(project.version.toString())
val isSnapshot = project.version.toString().toDefaultLowerCase().contains("snapshot")
val ciReleaseBundleMode = providers.gradleProperty("ciReleaseBundle").map(String::toBoolean).orElse(false).get()
val prebuiltReleaseBundleMode = providers.gradleProperty("prebuiltReleaseBundle").map(String::toBoolean).orElse(false).get()

/////////////////////////////////////////////////////////////////////////////
// Subprojects configuration
/////////////////////////////////////////////////////////////////////////////

allprojects {
    apply(plugin = rootProject.libs.plugins.jdk.get().pluginId)

    dependencyLocking {
        lockAllConfigurations()
    }

    jdk {
        version = rootProject.libs.versions.jdkVersion.get().toInt()
        javaFxBundled = true

        overrides {
            create("java25") {
                version = rootProject.libs.versions.javafxJdkVersion.get().toInt()
                javaFxBundled = true
            }
            create("testJava25") {
                version = rootProject.libs.versions.javafxJdkVersion.get().toInt()
                javaFxBundled = true
            }
        }
    }
}

subprojects {
    // Apply common plugins
    apply(plugin = "maven-publish")
    apply(plugin = "version-catalog")
    apply(plugin = "signing")
    apply(plugin = "idea")
    apply(plugin = rootProject.libs.plugins.test.logger.get().pluginId)

    // Skip some plugins for BOM project
    if (!project.name.endsWith("-bom")) {
        apply(plugin = "jacoco")
        apply(plugin = "java-library")
        apply(plugin = "jvm-test-suite")
        apply(plugin = rootProject.libs.plugins.spotbugs.get().pluginId)
        apply(plugin = rootProject.libs.plugins.cabe.get().pluginId)
        apply(plugin = rootProject.libs.plugins.jmh.get().pluginId)
    }

    // Java configuration for non-BOM projects
    if (!project.name.endsWith("-bom")) {
        java {
            withJavadocJar()
            withSourcesJar()
        }

        cabe {
            if (isReleaseVersion) {
                config.set(Configuration.parse("publicApi=THROW_NPE:privateApi=ASSERT:strict=true"))
            } else {
                config.set(Configuration.DEVELOPMENT.withStrict(true))
            }
        }

        // JaCoCo
        tasks.withType<JacocoReport> {
            reports {
                xml.required.set(true)
                html.required.set(false)
            }

            // Skip report generation when no execution data exists
            // (e.g. test JVM crashed before JaCoCo could write *.exec).
            onlyIf {
                executionData.files.any { it.exists() }
            }

            // use Cabe instrumented classes if they exist
            val cabeClasses = project.layout.buildDirectory.dir("classes-cabe/main")
            classDirectories.setFrom(project.provider {
                if (cabeClasses.get().asFile.exists()) {
                    val mainClassesDir = project.layout.buildDirectory.dir("classes/java/main").get().asFile
                    sourceSets.main.get().output.classesDirs.filter { it != mainClassesDir } + cabeClasses.get().asFile
                } else {
                    sourceSets.main.get().output.classesDirs
                }
            })
        }

        tasks.withType<Test> {
            systemProperty("user.language", "en")
            systemProperty("user.country", "US")

            useJUnitPlatform()
            finalizedBy(tasks.jacocoTestReport)
        }
    }

    // SonarQube properties
    sonar {
        properties {
            property("sonar.coverage.jacoco.xmlReportPaths", "**/build/reports/jacoco/test/jacocoTestReport.xml")
            property("sonar.coverage.exclusions", "**/samples/**")

            // use Cabe instrumented classes if they exist
            val cabeClassesDir = project.layout.buildDirectory.dir("classes-cabe/main").get().asFile
            if (cabeClassesDir.exists()) {
                property("sonar.java.binaries", "build/classes-cabe/main")
            }
        }
    }

    // Dependencies for non-BOM projects
    if (!project.name.endsWith("-bom")) {
        dependencies {
            implementation(rootProject.libs.jspecify)
            implementation(platform(rootProject.libs.log4j.bom))
            implementation(rootProject.libs.log4j.api)

            testImplementation(rootProject.libs.spotbugs.annotations)
            testImplementation(platform(rootProject.libs.junit.bom))
            testImplementation(rootProject.libs.junit.jupiter.api)
            testRuntimeOnly(rootProject.libs.junit.jupiter.engine)
        }

        idea {
            module {
                inheritOutputDirs = false
                outputDir = project.layout.buildDirectory.file("classes/java/main/").get().asFile
                testOutputDir = project.layout.buildDirectory.file("classes/java/test/").get().asFile
            }
        }

        testing {
            suites {
                getByName<JvmTestSuite>("test") {
                    useJUnitJupiter()
                    dependencies {
                        implementation(platform(rootProject.libs.slb4j.bom))
                        implementation(rootProject.libs.slb4j)
                        implementation(rootProject.libs.jimfs)
                    }
                    targets {
                        all {
                            testTask {
                                // enable assertions and use headless mode for AWT in unit tests
                                jvmArgs(
                                    "-ea",
                                    "-Djava.awt.headless=true",
                                    "-Dprism.order=sw",
                                    "-Dsun.java2d.d3d=false",
                                    "-Dsun.java2d.opengl=false",
                                    "-Dsun.java2d.pmoffscreen=false"
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    testlogger {
        theme = ThemeType.MOCHA_PARALLEL
    }

    // Java compilation and Javadoc config for non-BOM projects
    if (!project.name.endsWith("-bom")) {
        tasks.compileJava {
            options.encoding = "UTF-8"
            options.compilerArgs.addAll(listOf("-Xlint:deprecation", "-Xlint:-module"))
            options.javaModuleVersion.set(provider { project.version as String })
        }
        tasks.compileTestJava {
            options.encoding = "UTF-8"
            options.compilerArgs.addAll(listOf("-Xlint:deprecation", "-Xlint:-module"))
        }
        tasks.javadoc {
            (options as StandardJavadocDocletOptions).apply {
                encoding = "UTF-8"
                addStringOption("Xdoclint:all,-missing/private")
                locale = "en_US"
            }
        }
    }

    // JMH config for non-BOM projects
    if (!project.name.endsWith("-bom")) {
        jmh {
            warmupIterations = 2
            iterations = 5
            fork = 1
        }

        tasks.withType<JavaCompile>().configureEach {
            if (name == "jmhCompileGeneratedClasses") {
                javaCompiler.set(javaToolchains.compilerFor {
                    languageVersion.set(JavaLanguageVersion.of(21))
                    vendor.set(JvmVendorSpec.BELLSOFT)
                })
            }
        }
    }

    // SpotBugs for non-BOM projects
    if (!project.name.endsWith("-bom")) {

        // === SPOTBUGS ===
        spotbugs {
            excludeFilter.set(rootProject.file("spotbugs-exclude.xml"))
        }

        tasks.named<com.github.spotbugs.snom.SpotBugsTask>("spotbugsMain") {
            reports.create("html") {
                required.set(true)
                outputLocation.set(layout.buildDirectory.file("reports/spotbugs/main.html"))
                setStylesheet("fancy-hist.xsl")
            }
        }

        tasks.named<com.github.spotbugs.snom.SpotBugsTask>("spotbugsTest") {
            reports.create("html") {
                required.set(true)
                outputLocation.set(layout.buildDirectory.file("reports/spotbugs/test.html"))
                setStylesheet("fancy-hist.xsl")
            }
        }
    }

    // Jar duplicates strategy for non-BOM projects
    if (!project.name.endsWith("-bom")) {
        tasks.withType<Jar> {
            duplicatesStrategy = DuplicatesStrategy.INCLUDE
        }
    }

    // --- PUBLISHING ---

    configure<PublishingExtension> {
        // Repositories for publishing
        repositories {
            // Always add root-level staging directory for JReleaser
            maven {
                name = "stagingDirectory"
                url = rootProject.layout.buildDirectory.dir("staging-deploy").get().asFile.toURI()
            }
        }

        // Publications for non-BOM projects
        if (!project.name.endsWith("-bom")) {
            publications {
                create<MavenPublication>("mavenJava") {
                    from(components["java"])

                    groupId = Meta.GROUP
                    artifactId = project.name
                    version = project.version.toString()

                    pom {
                        name.set(project.name)
                        description.set(project.description)
                        url.set(Meta.SCM)

                        licenses {
                            license {
                                name.set(Meta.LICENSE_NAME)
                                url.set(Meta.LICENSE_URL)
                            }
                        }

                        developers {
                            developer {
                                id.set(Meta.DEVELOPER_ID)
                                name.set(Meta.DEVELOPER_NAME)
                                email.set(Meta.DEVELOPER_EMAIL)
                                organization.set(Meta.ORGANIZATION_NAME)
                                organizationUrl.set(Meta.ORGANIZATION_URL)
                            }
                        }

                        scm {
                            connection.set("scm:git:${Meta.SCM}")
                            developerConnection.set("scm:git:${Meta.SCM}")
                            url.set(Meta.SCM)
                        }

                        withXml {
                            val root = asNode()
                            root.appendNode("inceptionYear", "2019")
                        }
                    }
                }
            }
        }
    }

    // Task to publish to staging directory per subproject
    tasks.register("publishToStagingDirectory") {
        group = "publishing"
        description = "Publish artifacts to root staging directory for JReleaser"

        dependsOn(tasks.withType<PublishToMavenRepository>().matching {
            it.repository.name == "stagingDirectory"
        })
    }

    // A prepared release must not accidentally stage unchanged modules through a direct project task invocation.
    // Decide this while configuring the task: an onlyIf predicate runs during task execution, where Task.project
    // cannot be accessed when the configuration cache is enabled.
    if (releasePlanPresent && name != "utility-bom" && name !in configuredSelectedReleaseModules) {
        tasks.withType<PublishToMavenRepository>().configureEach {
            if (repository.name == "stagingDirectory") {
                onlyIf("module is not selected by the prepared release plan") { false }
            }
        }
    }

    // CI creates an unsigned, immutable publication bundle. The protected release workflow lets JReleaser
    // sign that exact bundle after it has been verified against the successful CI run.
    if (ciReleaseBundleMode) {
        tasks.withType<Sign>().configureEach {
            onlyIf("signing is deferred to the protected release workflow") { false }
        }
    }

    // Signing configuration deferred until after evaluation
    afterEvaluate {
        configure<SigningExtension> {
            val shouldSign = !project.version.toString().lowercase().contains("snapshot")
            isRequired = shouldSign && !ciReleaseBundleMode

            // The release workflow supplies the armored private key and passphrase as GitHub secrets. JReleaser
            // configures its own PGP signer below; Gradle's publication signing tasks need this signatory separately.
            val signingSecretKey = System.getenv("SIGNING_SECRET_KEY")
            if (!signingSecretKey.isNullOrBlank()) {
                useInMemoryPgpKeys(signingSecretKey, System.getenv("SIGNING_PASSWORD"))
            }

            val publishing = project.extensions.getByType<PublishingExtension>()

            if (project.name.endsWith("-bom")) {
                if (publishing.publications.names.contains("bomPublication")) {
                    sign(publishing.publications["bomPublication"])
                }
            } else {
                if (publishing.publications.names.contains("mavenJava")) {
                    sign(publishing.publications["mavenJava"])
                }
            }
        }
    }

    // set the project description after evaluation because it is not yet visible when the POM is first created
    afterEvaluate {
        project.extensions.configure<PublishingExtension> {
            publications.withType<MavenPublication> {
                pom {
                    if (description.orNull.isNullOrBlank()) {
                        description.set(project.description ?: "No description provided")
                    }
                }
            }
        }
    }
}

/////////////////////////////////////////////////////////////////////////////
// Root project tasks and JReleaser configuration
/////////////////////////////////////////////////////////////////////////////

// Aggregate all subprojects' publishToStagingDirectory tasks into a root-level task
tasks.register("publishToStagingDirectory") {
    group = "publishing"
    description = "Publish all eligible subprojects' artifacts to root staging directory for JReleaser"

    dependsOn(
        subprojects
            .filter { !releasePlanPresent || it.name == "utility-bom" || it.name in configuredSelectedReleaseModules }
            .mapNotNull { it.tasks.findByName("publishToStagingDirectory") }
    )
}

val cleanPreparedReleaseStaging = tasks.register<Delete>("cleanPreparedReleaseStaging") {
    group = "release"
    description = "Removes stale staging artifacts before a prepared release is staged."
    delete(layout.buildDirectory.dir("staging-deploy"))
}

private val releaseBundleStagingDirectory = layout.buildDirectory.dir("staging-deploy").get().asFile
private val releaseBundleDirectory = layout.buildDirectory.dir("release-bundle").get().asFile
private val releaseBundleManifest = releaseBundleDirectory.resolve("manifest.sha256")
private val releaseBundleMetadata = releaseBundleDirectory.resolve("metadata.properties")
private val releaseBundleGroupPath = Meta.GROUP.replace('.', '/')

private fun releaseBundleRelativePath(root: File, file: File): String =
    root.toPath().relativize(file.toPath()).toString().replace(File.separatorChar, '/')

private fun releaseBundleFiles(root: File): List<File> =
    if (root.isDirectory) {
        root.walkTopDown().filter(File::isFile).sortedBy { releaseBundleRelativePath(root, it) }.toList()
    } else {
        emptyList()
    }

private fun sha256(file: File): String {
    val digest = MessageDigest.getInstance("SHA-256")
    file.inputStream().use { input ->
        val buffer = ByteArray(64 * 1024)
        while (true) {
            val count = input.read(buffer)
            if (count < 0) break
            digest.update(buffer, 0, count)
        }
    }
    return digest.digest().joinToString("") { byte -> "%02x".format(byte.toInt() and 0xff) }
}

private fun stagedReleaseFile(moduleName: String, version: String, filename: String): File =
    releaseBundleStagingDirectory.resolve("$releaseBundleGroupPath/$moduleName/$version/$filename")

private fun writeReleaseBundleMetadata(plan: PreparedReleasePlan) {
    releaseBundleDirectory.mkdirs()
    val metadata = buildString {
        appendLine("commit=${requireGitSuccess("resolving CI release bundle commit", "rev-parse", "HEAD")}")
        appendLine("planSourceRevision=${plan.sourceRevision}")
        appendLine("bomVersion=${plan.bomVersion}")
        appendLine("selectedModules=${configuredSelectedReleaseModules.sorted().joinToString(",")}")
    }
    Files.writeString(releaseBundleMetadata.toPath(), metadata, StandardCharsets.UTF_8)
}

private fun writeReleaseBundleManifest() {
    val manifest = buildString {
        releaseBundleFiles(releaseBundleStagingDirectory).forEach { file ->
            append(sha256(file))
            append("  ")
            appendLine("staging-deploy/${releaseBundleRelativePath(releaseBundleStagingDirectory, file)}")
        }
    }
    Files.writeString(releaseBundleManifest.toPath(), manifest, StandardCharsets.UTF_8)
}

private fun readReleaseBundleManifest(): Map<String, String> {
    check(releaseBundleManifest.isFile) { "release bundle manifest is missing: ${releaseBundleManifest.path}" }
    val entries = linkedMapOf<String, String>()
    releaseBundleManifest.readLines(StandardCharsets.UTF_8).forEachIndexed { index, line ->
        if (line.isBlank()) return@forEachIndexed
        val separator = line.indexOf("  ")
        check(separator == 64) { "invalid release bundle manifest line ${index + 1}" }
        val digest = line.substring(0, separator)
        val path = line.substring(separator + 2)
        val stagingPath = path.removePrefix("staging-deploy/")
        check(stagingPath != path) { "release bundle manifest path is outside staging-deploy: $path" }
        check(digest.matches(Regex("[0-9a-f]{64}"))) { "invalid digest in release bundle manifest line ${index + 1}" }
        val candidate = releaseBundleStagingDirectory.toPath().resolve(stagingPath).normalize()
        check(candidate.startsWith(releaseBundleStagingDirectory.toPath())) {
            "release bundle manifest contains a path outside the staging directory: $path"
        }
        check(entries.put(stagingPath, digest) == null) { "duplicate release bundle manifest path: $path" }
    }
    return entries
}

private fun validateReleaseBundleContents(plan: PreparedReleasePlan) {
    check(releaseBundleStagingDirectory.isDirectory) {
        "release bundle staging directory is missing: ${releaseBundleStagingDirectory.path}"
    }
    check(releaseBundleMetadata.isFile) { "release bundle metadata is missing: ${releaseBundleMetadata.path}" }

    val metadata = Properties().apply {
        releaseBundleMetadata.inputStream().use { load(it) }
    }
    val currentCommit = requireGitSuccess("resolving release bundle commit", "rev-parse", "HEAD")
    check(metadata.getProperty("commit") == currentCommit) {
        "release bundle was built from ${metadata.getProperty("commit")}, but the current revision is $currentCommit"
    }
    check(metadata.getProperty("planSourceRevision") == plan.sourceRevision) {
        "release bundle plan source revision does not match the prepared release plan"
    }
    check(metadata.getProperty("bomVersion") == plan.bomVersion) {
        "release bundle BOM version does not match the prepared release plan"
    }
    check(metadata.getProperty("selectedModules") == configuredSelectedReleaseModules.sorted().joinToString(",")) {
        "release bundle module selection does not match the prepared release plan"
    }

    val files = releaseBundleFiles(releaseBundleStagingDirectory)
    check(files.none { it.name.endsWith(".asc") }) {
        "CI release bundles must be unsigned; signatures are added only by the protected release workflow"
    }
    val actualPaths = files.map { releaseBundleRelativePath(releaseBundleStagingDirectory, it) }.toSet()
    val manifest = readReleaseBundleManifest()
    check(manifest.keys == actualPaths) {
        "release bundle manifest does not match the staging directory contents"
    }
    manifest.forEach { (path, digest) ->
        val file = releaseBundleStagingDirectory.resolve(path)
        check(sha256(file) == digest) { "release bundle checksum mismatch: $path" }
    }

    fun requireFile(moduleName: String, version: String, filename: String) {
        val file = stagedReleaseFile(moduleName, version, filename)
        check(file.isFile) { "release bundle is missing ${file.relativeToOrNull(releaseBundleStagingDirectory)}" }
    }

    val bom = "utility-bom-${plan.bomVersion}"
    requireFile("utility-bom", plan.bomVersion, "$bom.pom")
    requireFile("utility-bom", plan.bomVersion, "$bom.module")
    plan.modules.filterValues { it.selected }.forEach { (moduleName, module) ->
        val artifact = "$moduleName-${module.version}"
        requireFile(moduleName, module.version, "$artifact.jar")
        requireFile(moduleName, module.version, "$artifact-sources.jar")
        requireFile(moduleName, module.version, "$artifact-javadoc.jar")
        requireFile(moduleName, module.version, "$artifact.pom")
        requireFile(moduleName, module.version, "$artifact.module")
    }
}

val prepareCiReleaseBundle = tasks.register("prepareCiReleaseBundle") {
    group = "release"
    description = "Creates an unsigned, checksummed Maven bundle from the CI build outputs."
    notCompatibleWithConfigurationCache(
        "The CI release bundle records the current Git revision and writes a promotion manifest."
    )
    dependsOn("verifyPreparedRelease", cleanPreparedReleaseStaging, "publishToStagingDirectory")

    doLast {
        check(ciReleaseBundleMode) {
            "prepareCiReleaseBundle requires -PciReleaseBundle=true"
        }
        check(releasePlanPresent) {
            "prepareCiReleaseBundle requires gradle/prepared-release.toml"
        }
        releaseBundleDirectory.deleteRecursively()
        writeReleaseBundleMetadata(readPreparedReleasePlan(preparedReleasePlanFile))
        writeReleaseBundleManifest()
        validateReleaseBundleContents(readPreparedReleasePlan(preparedReleasePlanFile))
        logger.lifecycle("Prepared unsigned CI release bundle at ${releaseBundleStagingDirectory.path}")
    }
}

val verifyCiReleaseBundle = tasks.register("verifyCiReleaseBundle") {
    group = "release"
    description = "Verifies the checksummed Maven bundle produced by a successful CI run."
    notCompatibleWithConfigurationCache(
        "The CI release bundle and Git revision are external promotion inputs."
    )
    doLast {
        check(prebuiltReleaseBundleMode) {
            "verifyCiReleaseBundle requires -PprebuiltReleaseBundle=true"
        }
        check(releasePlanPresent) {
            "verifyCiReleaseBundle requires gradle/prepared-release.toml"
        }
        validateReleaseBundleContents(readPreparedReleasePlan(preparedReleasePlanFile))
        logger.lifecycle("Verified CI release bundle for ${requireGitSuccess("resolving verified revision", "rev-parse", "HEAD")}")
    }
}

tasks.register("publishSnapshotsToMavenLocal") {
    group = "publishing"
    description = "Publishes every library module and the BOM to the local Maven repository for snapshot development."
    onlyIf { isSnapshot }
    dependsOn(
        publishableModuleNames.map { moduleName -> ":$moduleName:publishToMavenLocal" } +
            ":utility-bom:publishToMavenLocal"
    )
}

val stagePreparedRelease = tasks.register("stagePreparedRelease") {
    group = "release"
    description = "Builds, verifies, signs, and stages only the artifacts selected by the prepared release plan."
    dependsOn("verifyPreparedRelease", "checkReleaseCompatibility", cleanPreparedReleaseStaging)
    if (releasePlanPresent) {
        dependsOn(publishableModuleNames.map { moduleName -> ":$moduleName:check" })
        dependsOn(configuredSelectedReleaseModules.map { moduleName -> ":$moduleName:publishToStagingDirectory" })
        dependsOn(":utility-bom:publishToStagingDirectory")
    }
}

val jreleaserDeploy = tasks.named("jreleaserDeploy")

tasks.register("publishPreparedRelease") {
    group = "release"
    description = "Deploys the verified prepared release to Maven Central."
    dependsOn(stagePreparedRelease, jreleaserDeploy)
}

val publishPreparedReleaseFromCi = tasks.register("publishPreparedReleaseFromCi") {
    group = "release"
    description = "Signs and deploys the exact publication bundle verified by CI."
    dependsOn("verifyPreparedRelease", verifyCiReleaseBundle, "checkReleaseCompatibility", jreleaserDeploy)
}

tasks.named("checkReleaseCompatibility") {
    mustRunAfter(verifyCiReleaseBundle)
}

jreleaserDeploy.configure {
    mustRunAfter(
        stagePreparedRelease,
        verifyCiReleaseBundle,
        "verifyPreparedRelease",
        "checkReleaseCompatibility"
    )
}

gradle.projectsEvaluated {
    subprojects.forEach { subproject ->
        subproject.tasks.withType<PublishToMavenRepository>().configureEach {
            if (repository.name == "stagingDirectory") {
                mustRunAfter(cleanPreparedReleaseStaging)
            }
        }
    }
}

tasks.register("finalizeRelease") {
    group = "release"
    description = "Records a successfully deployed prepared release and creates its local Git tag."
    notCompatibleWithConfigurationCache(
        "Finalization verifies Maven Central, modifies release files, and creates Git commits and tags."
    )

    doLast {
        check(providers.gradleProperty("confirmFinalize").orNull == "true") {
            "finalization modifies Git state; re-run with -PconfirmFinalize=true after verifying Maven Central deployment"
        }

        if (!preparedReleasePlanFile.exists()) {
            val state = readPublishedReleaseState(releaseStateFile)
            val tagName = "v${state.bomVersion}"
            check(runGit("rev-parse", "-q", "--verify", "refs/tags/$tagName").exitValue != 0) {
                "no prepared plan exists and final tag $tagName already exists"
            }
            requireGitSuccess("creating final release tag", "tag", "-a", tagName, "-m", "Release ${state.bomVersion}")
            logger.lifecycle("Created missing final release tag $tagName.")
            return@doLast
        }

        check(runGit("status", "--porcelain").output.isBlank()) {
            "the Git working tree must be clean before finalizing a release"
        }
        check(runGit("ls-files", "--error-unmatch", preparedReleasePlanFile.relativeTo(rootDir).path).exitValue == 0) {
            "the prepared release plan must be committed before finalization"
        }

        val plan = readPreparedReleasePlan(preparedReleasePlanFile)
        validatePreparedReleasePlan(plan)
        val expectedCoordinates = buildList {
            add("utility-bom" to plan.bomVersion)
            plan.modules.filterValues { it.selected }.forEach { (moduleName, module) -> add(moduleName to module.version) }
        }
        expectedCoordinates.forEach { (artifactId, version) ->
            check(isMavenCentralCoordinatePublished(artifactId, version)) {
                "Maven Central does not yet expose expected artifact $artifactId:$version"
            }
        }

        val tagName = "v${plan.bomVersion}"
        check(runGit("rev-parse", "-q", "--verify", "refs/tags/$tagName").exitValue != 0) {
            "final release tag already exists: $tagName"
        }

        writePublishedReleaseState(plan, readPublishedReleaseState(releaseStateFile))
        writeDevelopmentVersion(nextDevelopmentVersion(plan.bomVersion))
        Files.delete(preparedReleasePlanFile.toPath())
        requireGitSuccess(
            "staging finalized release state",
            "add",
            releaseStateFile.relativeTo(rootDir).path,
            "gradle/version.toml",
            preparedReleasePlanFile.relativeTo(rootDir).path
        )
        requireGitSuccess("committing finalized release state", "commit", "-m", "Release ${plan.bomVersion}")
        requireGitSuccess("creating final release tag", "tag", "-a", tagName, "-m", "Release ${plan.bomVersion}")

        if (providers.gradleProperty("pushReleaseTag").orNull == "true") {
            val releaseBranch = providers.gradleProperty("releaseBranch").orNull
                ?: runGit("branch", "--show-current").output
            check(releaseBranch.isNotBlank()) {
                "supply -PreleaseBranch=<protected branch> when pushing from a detached checkout"
            }
            requireGitSuccess("pushing finalized release commit", "push", "origin", "HEAD:refs/heads/$releaseBranch")
            requireGitSuccess("pushing final release tag", "push", "origin", tagName)
        }
        logger.lifecycle("Finalized release ${plan.bomVersion} with tag $tagName.")
    }
}

// add a task to create aggregate Javadoc in the root projects build/docs/javadoc folder
tasks.register<Javadoc>("aggregateJavadoc") {
    group = "documentation"
    description = "Generates aggregated Javadoc for all subprojects"
    destinationDir = layout.buildDirectory.dir("docs/javadoc").get().asFile
    title = "${rootProject.name} ${project.version} API"

    // Set executable in doFirst to keep it lazy
    val extension = if (System.getProperty("os.name").contains("Windows", ignoreCase = true)) ".exe" else ""
    executable = jdk.jdkHome.get().file("bin/javadoc$extension").asFile.absolutePath

    // Disable module path inference
    modularity.inferModulePath.set(false)

    // Configure the task to depend on all subprojects' javadoc tasks
    val filteredProjects = subprojects.filter {
        !it.name.endsWith("-bom") && !it.name.contains("samples")
    }

    dependsOn(filteredProjects.map { it.tasks.named("javadoc") })

    // Collect all Java source directories from subprojects, excluding module-info.java files
    source(filteredProjects.flatMap { project ->
        val sourceSets = project.extensions.getByType(SourceSetContainer::class.java)
        val main = sourceSets.findByName("main")
        main?.allJava?.filter { file ->
            !file.name.equals("module-info.java")
        } ?: files()
    })

    // Collect all classpaths from subprojects
    classpath = files(filteredProjects.flatMap { project ->
        val sourceSets = project.extensions.getByType(SourceSetContainer::class.java)
        val main = sourceSets.findByName("main")
        main?.compileClasspath ?: files()
    })

    // Add runtime classpath to ensure all dependencies are available
    classpath += files(filteredProjects.flatMap { project ->
        val sourceSets = project.extensions.getByType(SourceSetContainer::class.java)
        val main = sourceSets.findByName("main")
        main?.runtimeClasspath ?: files()
    })

    // Apply the same Javadoc options as in subprojects
    (options as StandardJavadocDocletOptions).apply {
        encoding = "UTF-8"
        addStringOption("Xdoclint:all,-missing/private")
        links("https://docs.oracle.com/en/java/javase/21/docs/api/")
        use(true)
        noTimestamp(true)
        windowTitle = "${rootProject.name} ${project.version} API"
        docTitle = "${rootProject.name} ${project.version} API"
        header = "${rootProject.name} ${project.version} API"
        // Set locale to English to ensure consistent language in generated documentation
        locale = "en_US"
        // Disable module path to avoid module-related errors
        addBooleanOption("module-path", false)
    }
}

val jreleaserProjectVersion = rootProject.version.toString()

jreleaser {
    project {
        name.set(rootProject.name)
        version.set(jreleaserProjectVersion)
        group = Meta.GROUP
        authors.set(listOf(Meta.DEVELOPER_NAME))
        license.set(Meta.LICENSE_NAME)
        links {
            homepage.set(Meta.ORGANIZATION_URL)
        }
        inceptionYear.set(Meta.INCEPTION_YEAR)
        gitRootSearch.set(true)
    }

    signing {
        active.set(org.jreleaser.model.Active.ALWAYS)
        pgp {
            mode.set(org.jreleaser.model.Signing.Mode.COMMAND)
            armored.set(true)
            passphrase.set(System.getenv("SIGNING_PASSWORD"))
            verify.set(true)
            command {
                executable.set("gpg")
                homeDir.set(System.getenv("JRELEASER_GPG_HOME"))
                keyName.set(System.getenv("SIGNING_KEY_ID"))
                defaultKeyring.set(true)
            }
        }
    }

    deploy {
        maven {
            if (!isSnapshot) {
                println("adding release-deploy")
                mavenCentral {
                    create("release-deploy") {
                        active.set(org.jreleaser.model.Active.RELEASE)
                        url.set("https://central.sonatype.com/api/v1/publisher")
                        stagingRepositories.add("build/staging-deploy")
                        username.set(System.getenv("SONATYPE_USERNAME"))
                        password.set(System.getenv("SONATYPE_PASSWORD"))
                    }
                }
            }
        }
    }
}

/////////////////////////////////////////////////////////////////////////////
// Versions plugin configuration for all projects
/////////////////////////////////////////////////////////////////////////////

allprojects {
    fun isStable(version: String): Boolean {
        val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase().contains(it) }
        val regex = "[0-9,.v-]+-(rc|ea|alpha|beta|b|M|SNAPSHOT)([+-]?[0-9]*)?".toRegex(RegexOption.IGNORE_CASE)
        return stableKeyword || !regex.matches(version)
    }

    tasks.withType<DependencyUpdatesTask> {
        // refuse non-stable versions
        rejectVersionIf {
            !isStable(candidate.version)
        }

        // dependencyUpdates fails in parallel mode with Gradle 9+ (https://github.com/ben-manes/gradle-versions-plugin/issues/968)
        doFirst {
            gradle.startParameter.isParallelProjectExecutionEnabled = false
        }
    }
}
