rootProject.name = "dua3-utility"

include("utility")
include("utility-db")
include("utility-swing")
include("utility-logging")

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            plugin("versions", "com.github.ben-manes.versions").version("0.42.0")
            plugin("test-logger", "com.adarshr.test-logger").version("3.2.0")
            plugin("spotbugs", "com.github.spotbugs").version("5.0.12")
            plugin("cabe", "com.dua3.cabe").version("1.0.0")

            version("cabe", "1.0.0")
            library("cabe-annotations", "com.dua3.cabe", "cabe-annotations").versionRef("cabe")

            version("slf4j", "2.0.1")
            library("slf4j-api", "org.slf4j", "slf4j-api").versionRef("slf4j")
            library("slf4j-simple", "org.slf4j", "slf4j-simple").versionRef("slf4j")
            library("jul-to-slf4j", "org.slf4j", "jul-to-slf4j").versionRef("slf4j")

            version("log4j", "2.18.0")
            library("log4j-to-slf4j", "org.apache.logging.log4j", "log4j-to-slf4j").versionRef("log4j")

            version("junit", "5.9.0")
            library("junit-jupiter-api", "org.junit.jupiter", "junit-jupiter-api").versionRef("junit")
            library("junit-jupiter-engine", "org.junit.jupiter", "junit-jupiter-engine").versionRef("junit")
        }
    }
}
