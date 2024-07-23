description = "Java utilities (logging, Log4J)"

dependencies {
    implementation(project(":utility"))
    api(project(":utility-logging"))
    implementation(rootProject.libs.log4j.api)
    compileOnly(rootProject.libs.log4j.core)
}
