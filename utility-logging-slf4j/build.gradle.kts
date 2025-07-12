project.description = "Java utilities (logging, SLF4J)"

dependencies {
    implementation(project(":utility"))
    api(project(":utility-logging"))
    implementation(rootProject.libs.slf4j.api)

    testImplementation(rootProject.libs.junit.jupiter.api)
    testRuntimeOnly(rootProject.libs.junit.jupiter.engine)
    testImplementation(rootProject.libs.mockito)
}

tasks.withType<Test> {
    // Configure tests to run in a forked JVM
    forkEvery = 1
    maxParallelForks = Runtime.getRuntime().availableProcessors().coerceAtMost(4)

    // Enable assertions in the forked JVM
    jvmArgs("-ea")
}
