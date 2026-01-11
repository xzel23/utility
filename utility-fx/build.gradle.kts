project.description = "Java utilities (JavaFX)"

dependencies {
    implementation(project(":utility"))

    // Add dependency on javaTestUtil for tests
    testImplementation(project(path = ":utility", configuration = "javaTestUtil"))
    testImplementation(rootProject.libs.sawmill.lumberjack)
}

// Configure tests to run in a forked JVM
tasks.withType<Test> {
    // Fork a new JVM for each test class
    forkEvery = 1

    // Set maximum heap size for test JVM
    maxHeapSize = "1g"

    // Configure JavaFX headless mode and software rendering
    jvmArgs(
        "-ea",
        "-Djava.awt.headless=true",
        "-Dprism.order=sw",
        "-Dsun.java2d.d3d=false",
        "-Dsun.java2d.opengl=false",
        "-Dsun.java2d.pmoffscreen=false"
    )
}
