project.description = "Java utilities (logging)"

dependencies {
    implementation(project(":utility"))
    compileOnly(rootProject.libs.log4j.api)
    compileOnly(rootProject.libs.slf4j.api)
    compileOnly(rootProject.libs.commons.logging)
}
