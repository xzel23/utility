project.description = "Java utilities (JavaFX)"

plugins {
    alias(libs.plugins.javafx) apply false
}

val isWindowsArm = System.getProperty("os.name").startsWith("Windows", ignoreCase = true) &&
        (System.getProperty("os.arch").equals("aarch64", ignoreCase = true) || System.getProperty("os.arch").equals("arm64", ignoreCase = true))

if (!isWindowsArm) {
    apply(plugin = libs.plugins.javafx.get().pluginId)

    extensions.configure<org.openjfx.gradle.JavaFXOptions>("javafx") {
        version = libs.versions.javafx.get()
        modules = listOf("javafx.controls", "javafx.graphics")
    }
} else {
    logger.lifecycle("Windows ARM detected: skipping JavaFX Gradle plugin. Assuming JDK provides JavaFX modules.")
}

dependencies {
    implementation(project(":utility"))
    compileOnly(project(":utility-logging"))
    compileOnly(project(":utility-logging-log4j"))

    // Add dependency on javaTestUtil for tests
    testImplementation(project(path = ":utility", configuration = "javaTestUtil"))
    testImplementation(project(":utility-logging"))
    testRuntimeOnly(project(":utility-logging-log4j"))
}

// Configure tests to run in a forked JVM
tasks.withType<Test> {
    // Fork a new JVM for each test class
    forkEvery = 1

    // Set maximum heap size for test JVM
    maxHeapSize = "1g"

    // Enable assertions
    jvmArgs("-ea")

    // Configure JavaFX headless mode and software rendering
    jvmArgs(
        "-Djava.awt.headless=true",
        "-Dprism.order=sw",
        "-Dsun.java2d.d3d=false",
        "-Dsun.java2d.opengl=false",
        "-Dsun.java2d.pmoffscreen=false"
    )
}
