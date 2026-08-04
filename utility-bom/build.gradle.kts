project.description = "Bill of Materials (BOM) for utility libraries"

@Suppress("UNCHECKED_CAST")
val releaseModuleVersions = gradle.extra["releaseModuleVersions"] as Map<String, String>

fun releaseModuleVersion(moduleName: String): String =
    releaseModuleVersions[moduleName]
        ?: throw GradleException("no release version configured for module '$moduleName'")

plugins {
    id("java-platform")
    id("maven-publish")
}

dependencies {
    constraints {
        // Define constraints for all utility modules
        api("com.dua3.utility:utility:${releaseModuleVersion("utility")}")
        api("com.dua3.utility:utility-db:${releaseModuleVersion("utility-db")}")
        api("com.dua3.utility:utility-swing:${releaseModuleVersion("utility-swing")}")
        api("com.dua3.utility:utility-fx:${releaseModuleVersion("utility-fx")}")
        api("com.dua3.utility:utility-fx-icons:${releaseModuleVersion("utility-fx-icons")}")
        api("com.dua3.utility:utility-fx-icons-ikonli:${releaseModuleVersion("utility-fx-icons-ikonli")}")
        api("com.dua3.utility:utility-fx-controls:${releaseModuleVersion("utility-fx-controls")}")
        api("com.dua3.utility:utility-fx-db:${releaseModuleVersion("utility-fx-db")}")
        api("com.dua3.utility:utility-fx-web:${releaseModuleVersion("utility-fx-web")}")

        // Common dependencies
        api(rootProject.libs.jspecify)

        // Logging dependencies
        api("org.apache.logging.log4j:log4j-api:${rootProject.libs.versions.log4j.bom.get()}")

        // JavaFX and UI dependencies
        api(rootProject.libs.ikonli.javafx)

        // Security dependencies
        api(rootProject.libs.bouncycastle.provider)
        api(rootProject.libs.bouncycastle.pkix)
    }
}

// Configure publication for BOM
publishing {
    publications {
        create<MavenPublication>("bomPublication") {
            from(components["javaPlatform"])

            groupId = "com.dua3.utility"
            artifactId = "utility-bom"
            version = project.version.toString()

            pom {
                name.set("Utility BOM")
                description.set("Bill of Materials (BOM) for utility libraries")
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

    // Repositories are now configured in the root build.gradle.kts file
}

// Signing is now configured in the root build.gradle.kts file
