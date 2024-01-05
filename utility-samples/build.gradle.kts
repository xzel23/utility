project.description = "Java utilities (samples)"

dependencies {
    implementation(project(":utility"))
    implementation(project(":utility-swing"))
    implementation(project(":utility-logging"))
    implementation(rootProject.libs.miglayout.swing)
    implementation(rootProject.libs.log4j.api)
    implementation(rootProject.libs.slf4j.api)
}
