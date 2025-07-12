project.description = "Java utilities (logging, SLF4J)"

dependencies {
    api(project(":utility-logging"))
    implementation(project(":utility"))
    implementation(rootProject.libs.slf4j.api)

    testImplementation(rootProject.libs.junit.jupiter.api)
    testImplementation(rootProject.libs.mockito)
    testRuntimeOnly(rootProject.libs.junit.jupiter.engine)
}

tasks.withType<Test> {
    // Configure tests to run in a forked JVM
    forkEvery = 1
    maxParallelForks = Runtime.getRuntime().availableProcessors().coerceAtMost(4)

    // Enable assertions in the forked JVM
    jvmArgs("-ea")
}
