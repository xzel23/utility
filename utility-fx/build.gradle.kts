project.description = "Java utilities (JavaFX)"

dependencies {
    implementation(project(":utility"))
    compileOnly(project(":utility-logging"))

    // Add dependency on javaTestUtil for tests
    testImplementation(project(path = ":utility", configuration = "javaTestUtil"))
}
