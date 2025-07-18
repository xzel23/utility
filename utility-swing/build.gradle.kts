dependencies {
    implementation(project(":utility"))
    compileOnly(project(":utility-logging"))

    // Test dependencies
    testImplementation(rootProject.libs.junit4)
    testImplementation(project(":utility-logging"))

    // Use the regular logging setup for tests
    testImplementation(rootProject.libs.log4j.core)
    testImplementation(rootProject.libs.jimfs)

    // Add dependency on javaTestUtil for tests
    testImplementation(project(path = ":utility", configuration = "javaTestUtil"))
}
