description = "Java utilities (database)"

dependencies {
    implementation(project(":utility"))

    // H2 database for testing
    testImplementation("com.h2database:h2:2.2.224")
}
