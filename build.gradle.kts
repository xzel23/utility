// Copyright (c) 2019, 2022 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

@file:Suppress("UnstableApiUsage")

import com.adarshr.gradle.testlogger.theme.ThemeType
import com.dua3.cabe.processor.Configuration
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.gradle.internal.extensions.stdlib.toDefaultLowerCase
import java.io.ByteArrayOutputStream

plugins {
    id("java-library")
    id("jvm-test-suite")
    id("version-catalog")
    id("signing")
    id("idea")
    id("jacoco-report-aggregation")
    alias(libs.plugins.versions)
    alias(libs.plugins.test.logger)
    alias(libs.plugins.spotbugs)
    alias(libs.plugins.cabe)
    alias(libs.plugins.forbiddenapis)
    alias(libs.plugins.jmh)
    alias(libs.plugins.sonar)
    alias(libs.plugins.jreleaser)
}

/////////////////////////////////////////////////////////////////////////////
object Meta {
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

dependencies {
    // Add all subprojects to aggregation
    jacocoAggregation(project(":utility"))
    jacocoAggregation(project(":utility-db"))
    jacocoAggregation(project(":utility-swing"))
    jacocoAggregation(project(":utility-fx"))
    jacocoAggregation(project(":utility-fx-icons"))
    jacocoAggregation(project(":utility-fx-icons-ikonli"))
    jacocoAggregation(project(":utility-fx-controls"))
    jacocoAggregation(project(":utility-fx-db"))
    jacocoAggregation(project(":utility-fx-web"))
    jacocoAggregation(project(":utility-logging"))
    jacocoAggregation(project(":utility-logging-slf4j"))
    jacocoAggregation(project(":utility-logging-log4j"))
}

tasks.named<JacocoReport>("testCodeCoverageReport") {
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

// Root project SonarQube configuration - use aggregated report
sonar {
    properties {
        property("sonar.coverage.jacoco.xmlReportPaths", "${layout.buildDirectory.get()}/reports/jacoco/testCodeCoverageReport/testCodeCoverageReport.xml")
    }
}

subprojects {

    project.version = rootProject.libs.versions.projectVersion.get()

    fun isDevelopmentVersion(versionString : String) : Boolean {
        val v = versionString.toDefaultLowerCase()
        val markers = listOf("snapshot", "alpha", "beta")
        for (marker in markers) {
            if (v.contains("-$marker") || v.contains(".$marker")) {
                return true
            }
        }
        return false
    }
    val isReleaseVersion = !isDevelopmentVersion(project.version.toString())
    val isSnapshot = project.version.toString().toDefaultLowerCase().contains("snapshot")

    // Apply maven-publish plugin to all subprojects for snapshot publishing
    apply(plugin = "maven-publish")

    // Skip java-library plugin for utility-bom as it uses java-platform instead
    if (project.name != "utility-bom") {
        apply(plugin = "java-library")
        apply(plugin = "jvm-test-suite")
        apply(plugin = "jacoco")
        apply(plugin = "com.github.spotbugs")
        apply(plugin = "com.dua3.cabe")
        apply(plugin = "de.thetaphi.forbiddenapis")
        apply(plugin = "me.champeau.jmh")
    }

    // These plugins are compatible with both java-library and java-platform
    apply(plugin = "version-catalog")
    apply(plugin = "signing")
    apply(plugin = "idea")
    apply(plugin = "com.github.ben-manes.versions")
    apply(plugin = "com.adarshr.test-logger")

    // Java configuration only for projects with java-library plugin
    if (project.name != "utility-bom") {
        java {
            toolchain {
                languageVersion.set(JavaLanguageVersion.of(21))
            }

            targetCompatibility = JavaVersion.VERSION_21
            sourceCompatibility = targetCompatibility

            withJavadocJar()
            withSourcesJar()
        }

        cabe {
            if (isReleaseVersion) {
                config.set(Configuration.parse("publicApi=THROW_IAE:privateApi=ASSERT"))
            } else {
                config.set(Configuration.DEVELOPMENT)
            }
        }

        // JaCoCo
        tasks.withType<JacocoReport> {
            reports {
                xml.required.set(true)
                html.required.set(false)
            }
        }

        // Configure test task to use JaCoCo
        tasks.withType<Test> {
            useJUnitPlatform()
            finalizedBy(tasks.jacocoTestReport)
        }
    }

    // Sonar
    sonar {
        properties {
            property("sonar.coverage.jacoco.xmlReportPaths", "**/build/reports/jacoco/test/jacocoTestReport.xml")
            property("sonar.coverage.exclusions", "**/com/dua3/utility/samples/**")
        }
    }

    // Only apply these configurations to non-BOM projects
    if (project.name != "utility-bom") {
        // dependencies
        dependencies {
            // source annotations
            implementation(rootProject.libs.jspecify)

            // LOG4J
            implementation(platform(rootProject.libs.log4j.bom))
            implementation(rootProject.libs.log4j.api)
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
                val test by getting(JvmTestSuite::class) {
                    useJUnitJupiter()

                    dependencies {
                        implementation(rootProject.libs.log4j.core)
                        implementation(rootProject.libs.jimfs)
                        implementation(rootProject.libs.mockito)
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

    // Only apply Java-specific tasks to non-BOM projects
    if (project.name != "utility-bom") {
        tasks.compileJava {
            options.encoding = "UTF-8"
            options.compilerArgs.add("-Xlint:deprecation")
            options.compilerArgs.add("-Xlint:-module")
            options.javaModuleVersion.set(provider { project.version as String })
            options.release.set(java.targetCompatibility.majorVersion.toInt())
        }

        tasks.compileTestJava {
            options.encoding = "UTF-8"
        }

        tasks.javadoc {
            (options as StandardJavadocDocletOptions).apply {
                encoding = "UTF-8"
                addStringOption("Xdoclint:all,-missing/private")
            }
        }
    }

    // === publication: MAVEN = == >
    // Publication is now handled by JReleaser

    // === sign artifacts
    // Signing is now handled by JReleaser

    // === JMH ===
    // Only apply JMH configuration to non-BOM projects
    if (project.name != "utility-bom") {
        jmh {
            jmhVersion = rootProject.libs.versions.jmh
            warmupIterations = 2
            iterations = 5
            fork = 1
        }
    }

    // Only apply these configurations to non-BOM projects
    if (project.name != "utility-bom") {
        // === FORBIDDEN APIS ===
        forbiddenApis {
            bundledSignatures = setOf("jdk-internal", "jdk-deprecated")
            ignoreFailures = false
        }

        // === SPOTBUGS ===
        spotbugs.toolVersion.set(rootProject.libs.versions.spotbugs)
        spotbugs.excludeFilter.set(rootProject.file("spotbugs-exclude.xml"))

        tasks.withType<com.github.spotbugs.snom.SpotBugsTask> {
            reports.create("html") {
                required.set(true)
                outputLocation = project.layout.buildDirectory.file("reports/spotbugs.html").get().asFile
                setStylesheet("fancy-hist.xsl")
            }
            reports.create("xml") {
                required.set(true)
                outputLocation = project.layout.buildDirectory.file("reports/spotbugs.xml").get().asFile
            }
        }
    }

    // === PUBLISHING ===
    // Publishing is now handled by JReleaser

    // Only apply Jar configuration to non-BOM projects
    if (project.name != "utility-bom") {
        tasks.withType<Jar> {
            duplicatesStrategy = DuplicatesStrategy.INCLUDE
        }
    }

    // Configure publishing for all subprojects
    configure<PublishingExtension> {
        // Add repositories for publishing
        repositories {
            // For snapshot versions, publish to Sonatype Snapshots repository
            if (isSnapshot) {
                maven {
                    name = "sonatypeSnapshots"
                    url = uri("https://central.sonatype.com/repository/maven-snapshots/")
                    credentials {
                        username = project.findProperty("sonatypeUsername") as String? ?: System.getenv("SONATYPE_USERNAME")
                        password = project.findProperty("sonatypePassword") as String? ?: System.getenv("SONATYPE_PASSWORD")
                    }
                }
            }
        }

        // Configure publications for non-BOM projects
        if (project.name != "utility-bom") {
            publications {
                create<MavenPublication>("mavenJava") {
                    from(components["java"])

                    groupId = "com.dua3.utility"
                    artifactId = project.name
                    version = project.version.toString()

                    pom {
                        name.set(project.name)
                        description.set("Utility library for Java")
                        url.set("https://github.com/xzel23/utility.git")

                        licenses {
                            license {
                                name.set("MIT")
                                url.set("https://opensource.org/licenses/MIT")
                            }
                        }

                        developers {
                            developer {
                                id.set("axh")
                                name.set("Axel Howind")
                                email.set("axh@dua3.com")
                                organization.set("dua3")
                                organizationUrl.set("https://www.dua3.com")
                            }
                        }

                        scm {
                            connection.set("scm:git:https://github.com/xzel23/utility.git")
                            developerConnection.set("scm:git:https://github.com/xzel23/utility.git")
                            url.set("https://github.com/xzel23/utility.git")
                        }

                        // Add inceptionYear
                        withXml {
                            val root = asNode()
                            root.appendNode("inceptionYear", "2019")
                        }
                    }
                }
            }
        }
    }

    // Configure signing for all subprojects
    // Defer signing configuration until publications are set up
    afterEvaluate {
        configure<SigningExtension> {
            val shouldSign = !project.version.toString().lowercase().contains("snapshot")
            setRequired(shouldSign && gradle.taskGraph.hasTask("publish"))

            // Get the publishing extension
            val publishing = project.extensions.getByType<PublishingExtension>()

            // Sign the appropriate publication based on the project
            if (project.name == "utility-bom") {
                // Only sign if the publication exists
                if (publishing.publications.names.contains("bomPublication")) {
                    sign(publishing.publications["bomPublication"])
                }
            } else {
                // Only sign if the publication exists
                if (publishing.publications.names.contains("mavenJava")) {
                    sign(publishing.publications["mavenJava"])
                }
            }
        }
    }
}

fun Project.readSecretFromKeychain(service: String): String {
    val result = ByteArrayOutputStream()
    this.exec {
        commandLine("security", "find-generic-password", "-a", "gradle", "-s", service, "-w")
        standardOutput = result
        isIgnoreExitValue = true
    }
    return result.toString().trim()
}

fun getSecret(key: String, fallbackEnv: String): String =
    try {
        readSecretFromKeychain(key)
    } catch (e: Exception) {
        System.getenv(fallbackEnv) ?: error("Missing secret for $key")
    }

// JReleaser configuration
// Create staging directory for JReleaser
tasks.register("createStagingDirectory") {
    description = "Creates the staging directory for JReleaser"
    group = "publishing"

    doLast {
        mkdir("build/staging-deploy")
    }
}

// Make jreleaserDeploy depend on createStagingDirectory
tasks.named("jreleaserDeploy") {
    dependsOn("createStagingDirectory")
}

jreleaser {
    project {
        name.set(rootProject.name)
        version.set(rootProject.libs.versions.projectVersion.get())
        group = Meta.GROUP
        description.set("Utility libraries for Java")
        authors.set(listOf(Meta.DEVELOPER_NAME))
        license.set(Meta.LICENSE_NAME)
        links {
            homepage.set(Meta.ORGANIZATION_URL)
        }
        inceptionYear.set("2019")
        gitRootSearch.set(true)
    }

    signing {
        publicKey.set("<KEY>")
        secretKey.set(readSecretFromKeychain("SIGNING_SECRET_KEY"))
        passphrase.set(readSecretFromKeychain("SIGNING_PASSWORD"))
        active.set(org.jreleaser.model.Active.ALWAYS)
        armored.set(true)
    }

    deploy {
        maven {
            mavenCentral {
                create("release-deploy") {
                    active.set(org.jreleaser.model.Active.RELEASE)
                    url.set("https://central.sonatype.com/api/v1/publisher")
                    stagingRepositories.add("build/staging-deploy")
                    username.set("\${sonatypeUsername}")
                    password.set("\${sonatypePassword}")
                }
            }
            nexus2 {
                create("snapshot-deploy") {
                    active.set(org.jreleaser.model.Active.SNAPSHOT)
                    snapshotUrl.set("https://central.sonatype.com/repository/maven-snapshots/")
                    applyMavenCentralRules.set(true)
                    snapshotSupported.set(true)
                    closeRepository.set(true)
                    releaseRepository.set(true)
                    stagingRepositories.add("build/staging-deploy")
                    username.set("\${sonatypeUsername}")
                    password.set("\${sonatypePassword}")
                }
            }
        }
    }
}

// Task to generate JReleaser configuration file for reference
tasks.register("generateJReleaserConfig") {
    description = "Generates JReleaser configuration file for reference"
    group = "documentation"

    doLast {
        // Use ProcessBuilder instead of deprecated exec
        val process = ProcessBuilder("./gradlew", "jreleaserConfig", "-PconfigFile=jreleaser-config.yml")
            .directory(project.rootDir)
            .inheritIO()
            .start()
        val exitCode = process.waitFor()
        if (exitCode == 0) {
            println("JReleaser configuration file generated at: jreleaser-config.yml")
        } else {
            println("Failed to generate JReleaser configuration file. Exit code: $exitCode")
        }
    }
}

allprojects {
    // versions plugin configuration
    fun isStable(version: String): Boolean {
        val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase().contains(it) }
        val regex = "[0-9,.v-]+-(rc|alpha|beta|b|M)(-?[0-9]*)?".toRegex()
        val isStable = stableKeyword || !regex.matches(version)
        return isStable
    }

    tasks.withType<DependencyUpdatesTask> {
        rejectVersionIf {
            !isStable(candidate.version)
        }
    }
}
