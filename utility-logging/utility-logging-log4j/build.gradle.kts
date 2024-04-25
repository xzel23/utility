description = "Java utilities (logging, Log4J)"

dependencies {
    api(project(":utility-logging"))
    implementation(project(":utility"))
    implementation(rootProject.libs.log4j.api)
    compileOnly(rootProject.libs.log4j.core)
}
