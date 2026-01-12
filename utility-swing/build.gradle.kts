dependencies {
    implementation(project(":utility"))

    // Use the regular logging setup for tests
    testImplementation(rootProject.libs.slb4j)
    testImplementation(rootProject.libs.jimfs)

    // Add dependency on javaTestUtil for tests
    testImplementation(project(path = ":utility", configuration = "javaTestUtil"))
}
