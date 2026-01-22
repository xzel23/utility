dependencies {
    implementation(project(":utility"))

    testImplementation(rootProject.libs.slb4j)
    testImplementation(rootProject.libs.jimfs)

    // Add dependency on javaTestUtil for tests
    testImplementation(project(path = ":utility", configuration = "javaTestUtil"))
}
