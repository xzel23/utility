description = "Java utilities (logging)"

dependencies {
    implementation(project(":utility"))
    implementation(rootProject.libs.slf4j.api)
    api(project(":utility-logging"))
}
