// Copyright (c) 2019, 2022 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

import java.net.URI;
import com.adarshr.gradle.testlogger.theme.ThemeType;

plugins {
    id("java-library")
    id("maven-publish")
    id("signing")
    id("idea")
    id("com.github.ben-manes.versions") version "0.42.0"
    id("com.adarshr.test-logger") version "3.2.0"
    id("com.github.spotbugs") version "5.0.9"
    id("com.dua3.cabe") version "1.0.0"
}

/////////////////////////////////////////////////////////////////////////////
object meta {
    val group           = "com.dua3.utility"
    val version         = "10.0.0" 
    val scm             = "https://gitlab.com/com.dua3/lib/utility.git"
    val repo            = "public"
    val licenseName     = "MIT"
    val licenseUrl      = "https://opensource.org/licenses/MIT"
    val developerId     = "axh"
    val developerName   = "Axel Howind"
    val developerEmail  = "axh@dua3.com"
    val organization    = "dua3"
    val organizationUrl = "https://www.dua3.com"
}
/////////////////////////////////////////////////////////////////////////////

val isReleaseVersion = !meta.version.endsWith("SNAPSHOT")

subprojects {
    
    project.setVersion(meta.version)

    apply(plugin = "java-library")
    apply(plugin = "maven-publish")
    apply(plugin = "signing")
    apply(plugin = "idea")
    apply(plugin = "com.github.ben-manes.versions")
    apply(plugin = "com.adarshr.test-logger")
    apply(plugin = "com.github.spotbugs")
    apply(plugin = "com.dua3.cabe")

    java {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        
        withJavadocJar()
        withSourcesJar()
    }
    
    repositories {
        mavenCentral()
    }

    // dependencies
    dependencies {
        // Cabe (source annotations)
        compileOnly(group = "com.dua3.cabe", name = "cabe-annotations", version = "1.0.0")

        // JUnit
        testImplementation(group = "org.junit.jupiter", name = "junit-jupiter-api", version = "5.9.0")
        testRuntimeOnly(group = "org.junit.jupiter", name = "junit-jupiter-engine", version = "5.9.0")
    }

    idea {
        module {
            inheritOutputDirs = false
            outputDir = file("$buildDir/classes/java/main/")
            testOutputDir = file("$buildDir/classes/java/test/")
        }
    }

    tasks.test {
        useJUnitPlatform()
    }

    testlogger {
        theme = ThemeType.MOCHA_PARALLEL
    }

    tasks.compileJava {
        options.encoding = "UTF-8"
        options.javaModuleVersion.set(provider { project.version as String })
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
                groupId    = meta.group
                artifactId = project.name
                version    = meta.version

                from(components["java"])

                pom {
                    withXml {
                        val root = asNode()
                        root.appendNode("description", project.description)
                        root.appendNode("name", project.name)
                        root.appendNode("url", meta.scm)
                    }

                    licenses {
                        license {
                            name.set(meta.licenseName)
                            url.set(meta.licenseUrl)
                        }
                    }
                    developers {
                        developer {
                            id.set(meta.developerId)
                            name.set(meta.developerName)
                            email.set(meta.developerEmail)
                            organization.set(meta.organization)
                            organizationUrl.set(meta.organizationUrl)
                        }
                    }

                    scm {
                        url.set(meta.scm)
                    }
                }
            }
        }

        repositories {
            // Sonatype OSSRH
            maven {
                val releaseRepo = URI("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
                val snapshotRepo = URI("https://s01.oss.sonatype.org/content/repositories/snapshots/")
                url = if (isReleaseVersion) releaseRepo else snapshotRepo
                credentials {
                    username = project.properties["ossrhUsername"].toString()
                    password = project.properties["ossrhPassword"].toString()
                }
            }
        }
    }

    // === sign artifacts
    signing {
        setRequired(isReleaseVersion && gradle.taskGraph.hasTask("publish"))
        sign(publishing.publications["maven"])
    }

    // === SPOTBUGS ===
    spotbugs.excludeFilter.set(rootProject.file("spotbugs-exclude.xml"))

    tasks.withType<com.github.spotbugs.snom.SpotBugsTask>() {
        reports.create("html") {
            required.set(true)
            outputLocation.set(file("$buildDir/reports/spotbugs.html"))
            setStylesheet("fancy-hist.xsl")
        }
    }

    // === PUBLISHING ===
    tasks.withType<PublishToMavenRepository>() {
        dependsOn(tasks.publishToMavenLocal)
    }

    tasks.withType<Jar>() {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }

}
