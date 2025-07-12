project.description = "Java utilities (logging, Log4J)"

dependencies {
    api(project(":utility-logging"))
    implementation(project(":utility"))
    implementation(rootProject.libs.log4j.api)
    compileOnly(rootProject.libs.log4j.core)

    testImplementation(rootProject.libs.junit.jupiter.api)
    testImplementation(rootProject.libs.log4j.core)
    testRuntimeOnly(rootProject.libs.junit.jupiter.engine)
}

tasks.withType<Test> {
    // Configure tests to run in a forked JVM
    forkEvery = 1
    maxParallelForks = Runtime.getRuntime().availableProcessors().coerceAtMost(4)

    // Enable assertions in the forked JVM
    jvmArgs("-ea")
}
