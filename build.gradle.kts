// Copyright (c) 2019, 2022 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

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
    alias(libs.plugins.versions)
    alias(libs.plugins.test.logger)
    alias(libs.plugins.spotbugs)
    alias(libs.plugins.cabe)
    alias(libs.plugins.forbiddenapis)
}

/////////////////////////////////////////////////////////////////////////////
object Meta {
    const val GROUP = "com.dua3.utility"
    const val SCM = "https://github.com/xzel23/utility.git"
    const val REPO = "public"
    const val LICENSE_NAME = "MIT"
    const val LICENSE_URL = "https://opensource.org/licenses/MIT"
    const val DEVELOPER_ID = "axh"
    const val DEVELOPER_NAME = "Axel Howind"
    const val DEVELOPER_EMAIL = "axh@dua3.com"
    const val ORGANIZATION_NAME = "dua3"
    const val ORGANIZATION_URL = "https://www.dua3.com"
}
/////////////////////////////////////////////////////////////////////////////

tasks.register("printStartMessage") {
    doFirst {
        println("NOTE: A JDK with prepackaged JavaFX (i.e., Azul Zulu 'JDK FX' or Bellsoft 'Full JDK') or a properly configured local JavaFX installation is needed!")
    }
}

tasks.named("build") {
    dependsOn(tasks.named("printStartMessage"))
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

    apply(plugin = "java-library")
    apply(plugin = "jvm-test-suite")
    apply(plugin = "maven-publish")
    apply(plugin = "version-catalog")
    apply(plugin = "signing")
    apply(plugin = "idea")
    apply(plugin = "com.github.ben-manes.versions")
    apply(plugin = "com.adarshr.test-logger")
    apply(plugin = "com.github.spotbugs")
    apply(plugin = "com.dua3.cabe")
    apply(plugin = "de.thetaphi.forbiddenapis")

    java {
        targetCompatibility = JavaVersion.VERSION_17
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
                            // Use headless mode for AWT in unit tests
                            jvmArgs("-Djava.awt.headless=true")
                        }
                    }
                }
            }
        }
    }

    testlogger {
        theme = ThemeType.MOCHA_PARALLEL
    }

    tasks.compileJava {
        options.encoding = "UTF-8"
        options.compilerArgs.add("-Xlint:deprecation")
        options.javaModuleVersion.set(provider { project.version as String })
        options.release.set(java.targetCompatibility.majorVersion.toInt())
    }

    tasks.compileTestJava {
        options.encoding = "UTF-8"
    }

    tasks.withType<Javadoc> {
        options.encoding = "UTF-8"
    }

    // === publication: MAVEN = == >

    // Create the publication with the pom configuration:
    publishing {
        publications {
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
        sign(publishing.publications["maven"])
    }

    // === SPOTBUGS ===
    spotbugs.excludeFilter.set(rootProject.file("spotbugs-exclude.xml"))
    spotbugs.toolVersion.set("4.9.3")

    // === FORBIDDEN APIS ===
    forbiddenApis {
        bundledSignatures = setOf("jdk-internal", "jdk-deprecated")
        ignoreFailures = false
    }

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

    // === PUBLISHING ===
    tasks.withType<PublishToMavenRepository> {
        dependsOn(tasks.publishToMavenLocal)
    }

    tasks.withType<Jar> {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }

}

allprojects {
    // versions plugin configuration
    fun isStable(version: String): Boolean {
        val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase().contains(it) }
        val regex = "[0-9,.v-]+-(rc|alpha|beta|b)(-?[0-9]*)?".toRegex()
        val isStable = stableKeyword || !regex.matches(version)
        return isStable
    }

    tasks.withType<DependencyUpdatesTask> {
        resolutionStrategy {
            componentSelection {
                all {
                    if (!isStable(candidate.version)) {
                        reject("Release candidate")
                    }
                }
            }
        }
    }
}
