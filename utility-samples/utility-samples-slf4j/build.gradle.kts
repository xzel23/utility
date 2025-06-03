project.description = "Java utilities (samples)"

dependencies {
    implementation(project(":utility"))
    implementation(project(":utility-swing"))
    implementation(project(":utility-logging"))
    implementation(project(":utility-samples"))
    implementation(rootProject.libs.miglayout.swing)
    implementation(rootProject.libs.jul.to.slf4j)
    implementation(rootProject.libs.log4j.to.slf4j)
    runtimeOnly(project(":utility-logging-slf4j"))
}
