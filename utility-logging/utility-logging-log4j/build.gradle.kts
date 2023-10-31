description = "Java utilities (logging)"

dependencies {
    implementation(project(":utility"))
    implementation(rootProject.libs.log4j.api)
    compileOnly(rootProject.libs.log4j.core)
    testImplementation(rootProject.libs.log4j.core)
    api(project(":utility-logging"))
}
