project.description = "Java utilities (JavaFX)"

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
