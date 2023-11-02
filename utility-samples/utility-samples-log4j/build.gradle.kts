project.description = "Java utilities (samples)"

dependencies {
    implementation(project(":utility"))
    implementation(project(":utility-swing"))
    implementation(project(":utility-logging"))
    implementation(rootProject.libs.miglayout.swing)
    runtimeOnly(project(":utility-logging:utility-logging-log4j"))
    implementation(rootProject.libs.log4j.core)
    implementation(rootProject.libs.log4j.jul)
    implementation(rootProject.libs.log4j.slf4j2)
    implementation(rootProject.libs.slf4j.api)
}

// test utility-swing rely on our own Logger implementation, so exclude SLF4J SimpleLogger
configurations.implementation {
    exclude(group = "org.slf4j", module = "slf4j-simple")
}
