// Copyright (c) 2019, 2022 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

@file:Suppress("UnstableApiUsage")

import com.adarshr.gradle.testlogger.theme.ThemeType
import com.dua3.cabe.processor.Configuration
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.gradle.internal.extensions.stdlib.toDefaultLowerCase
import java.net.URI

plugins {
    id("java-library")
    id("jvm-test-suite")
    id("maven-publish")
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
    apply(plugin = "maven-publish")
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

    // Create the publication with the pom configuration:
    publishing {
        publications {
            // Skip creating the maven publication for utility-bom as it has its own publication
            if (project.name != "utility-bom") {
                create<MavenPublication>("maven") {
                    groupId = Meta.GROUP
                    artifactId = project.name
                    version = project.version.toString()

                    from(components["java"])

                    pom {
                        withXml {
                            val root = asNode()
                            root.appendNode("description", project.description)
                            root.appendNode("name", project.name)
                            root.appendNode("url", Meta.SCM)
                        }

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
                            url.set(Meta.SCM)
                        }
                    }
                }
            }
        }

        repositories {
            // Sonatype OSSRH
            maven {
                val releaseRepo = URI("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
                val snapshotRepo = URI("https://s01.oss.sonatype.org/content/repositories/snapshots/")
                url = if (isSnapshot) snapshotRepo else releaseRepo
                credentials {
                    username = project.properties["ossrhUsername"].toString()
                    password = project.properties["ossrhPassword"].toString()
                }
            }
        }
    }

    // === sign artifacts
    signing {
        isRequired = isReleaseVersion && gradle.taskGraph.hasTask("publish")
        // Only sign the maven publication for non-BOM projects
        // The BOM project has its own signing configuration
        if (project.name != "utility-bom") {
            sign(publishing.publications["maven"])
        }
    }

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
    tasks.withType<PublishToMavenRepository> {
        dependsOn(tasks.publishToMavenLocal)
    }

    // Only apply Jar configuration to non-BOM projects
    if (project.name != "utility-bom") {
        tasks.withType<Jar> {
            duplicatesStrategy = DuplicatesStrategy.INCLUDE
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
