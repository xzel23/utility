description = "Java utilities (logging, SLF4J)"

dependencies {
    implementation(project(":utility"))
    api(project(":utility-logging"))
    implementation(rootProject.libs.slf4j.api)
}
