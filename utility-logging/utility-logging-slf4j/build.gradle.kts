description = "Java utilities (logging, SLF4J)"

dependencies {
    implementation(project(":utility"))
    implementation(rootProject.libs.slf4j.api)
    api(project(":utility-logging"))
}
