project.description = "JavaFX utilities (database)"

plugins {
    alias(libs.plugins.javafx) apply false
}

val isWindowsArm = System.getProperty("os.name").startsWith("Windows", ignoreCase = true) &&
        (System.getProperty("os.arch").equals("aarch64", ignoreCase = true) || System.getProperty("os.arch").equals("arm64", ignoreCase = true))

if (!isWindowsArm) {
    apply(plugin = libs.plugins.javafx.get().pluginId)

    extensions.configure<org.openjfx.gradle.JavaFXOptions>("javafx") {
        version = libs.versions.javafx.get()
        modules = listOf("javafx.controls")
    }
} else {
    logger.lifecycle("Windows ARM detected: skipping JavaFX Gradle plugin. Assuming JDK provides JavaFX modules.")
}

dependencies {
    implementation(project(":utility-fx"))
    api(project(":utility-db"))
}
