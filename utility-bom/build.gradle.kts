description = "Bill of Materials (BOM) for utility libraries"

plugins {
    id("java-platform")
}

dependencies {
    constraints {
        // Define constraints for all utility modules
        api("com.dua3.utility:utility:${project.version}")
        api("com.dua3.utility:utility-db:${project.version}")
        api("com.dua3.utility:utility-swing:${project.version}")
        api("com.dua3.utility:utility-fx:${project.version}")
        api("com.dua3.utility:utility-fx-icons:${project.version}")
        api("com.dua3.utility:utility-fx-icons-ikonli:${project.version}")
        api("com.dua3.utility:utility-fx-controls:${project.version}")
        api("com.dua3.utility:utility-fx-db:${project.version}")
        api("com.dua3.utility:utility-fx-web:${project.version}")
        api("com.dua3.utility:utility-logging:${project.version}")
        api("com.dua3.utility:utility-logging-slf4j:${project.version}")
        api("com.dua3.utility:utility-logging-log4j:${project.version}")

        // External dependencies used by utility modules
        // Common dependencies
        api("org.jspecify:jspecify:${rootProject.libs.versions.jspecify.get()}")

        // Logging dependencies
        api("org.apache.logging.log4j:log4j-api:${rootProject.libs.versions.log4j.bom.get()}")
        api("org.apache.logging.log4j:log4j-core:${rootProject.libs.versions.log4j.bom.get()}")
        api("org.slf4j:slf4j-api:${rootProject.libs.versions.slf4j.get()}")

        // JavaFX and UI dependencies
        api("org.kordamp.ikonli:ikonli-javafx:${rootProject.libs.versions.ikonli.get()}")

        // Security dependencies
        api("org.bouncycastle:bcprov-jdk18on:${rootProject.libs.versions.bouncycastle.get()}")
        api("org.bouncycastle:bcpkix-jdk18on:${rootProject.libs.versions.bouncycastle.get()}")
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
}

// Configure signing for BOM
signing {
    val isReleaseVersion = !project.version.toString().lowercase().contains("snapshot")
    isRequired = isReleaseVersion && gradle.taskGraph.hasTask("publish")
    sign(publishing.publications["bomPublication"])
}
