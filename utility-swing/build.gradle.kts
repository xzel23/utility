project.description = "Java utilities (swing)"

dependencies {
    implementation(project(":utility"))
    compileOnly(project(":utility-logging"))
    
    testImplementation(project(":utility-logging"))
    testImplementation("org.apache.logging.log4j:log4j-core:2.18.0")
    testImplementation("org.apache.logging.log4j:log4j-to-slf4j:2.18.0")
}
