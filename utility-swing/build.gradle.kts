dependencies {
    implementation(project(":utility"))

    testImplementation(platform(rootProject.libs.slb4j.bom))
    testImplementation(rootProject.libs.slb4j)
    testImplementation(rootProject.libs.jimfs)

    // Add dependency on javaTestUtil for tests
    testImplementation(project(path = ":utility", configuration = "javaTestUtil"))
}

tasks.withType<Test> {
    // All tests in this module exercise component logic only. Running them headlessly avoids
    // starting X11-backed AWT resources on CI, even when a virtual display is available.
    systemProperty("java.awt.headless", "true")

    // AWT/Swing has JVM-wide state. Isolate each test class so a stalled toolkit cannot block
    // unrelated tests or Gradle's test worker.
    forkEvery = 1
}
