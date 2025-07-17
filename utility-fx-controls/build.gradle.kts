project.description = "JavaFX utilities (controls)"

plugins {
    alias(libs.plugins.javafx)
}

javafx {
    version = libs.versions.javafx.get()
    modules = listOf("javafx.controls", "javafx.graphics")
}

dependencies {
    implementation(project(":utility"))
    implementation(project(":utility-fx"))
    implementation(project(":utility-fx-icons"))
    compileOnly(project(":utility-logging"))

    // Add dependency on javaTestUtil for tests
    testImplementation(project(path = ":utility", configuration = "javaTestUtil"))
    testImplementation(project(":utility-logging"))
    testRuntimeOnly(project(":utility-logging-log4j"))
    testRuntimeOnly(project(":utility-fx-icons-ikonli"))
    testRuntimeOnly(libs.ikonli.fontawesome6)
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
