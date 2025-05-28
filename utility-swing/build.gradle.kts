dependencies {
    implementation(project(":utility"))
    compileOnly(project(":utility-logging"))

    // Test dependencies
    testImplementation(rootProject.libs.junit4)

    // Use the regular logging setup for tests
    testImplementation(rootProject.libs.log4j.core)
    testImplementation(rootProject.libs.jimfs)
    testImplementation(rootProject.libs.mockito)
}
