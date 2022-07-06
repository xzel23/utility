project.description = "Java utilities (swing)"

dependencies {
    implementation(project(":utility"))

    testImplementation(group = "org.apache.logging.log4j", name = "log4j-core", version = "2.17.2")
}
