project.description = "Java utilities (samples)"

dependencies {
    implementation(project(":utility"))
    implementation(project(":utility-swing"))
    implementation(project(":utility-logging"))
    implementation(rootProject.libs.jul.to.slf4j)
    implementation(rootProject.libs.log4j.to.slf4j)

    implementation("com.miglayout:miglayout-swing:11.1")
}

// test utility-swing rely on our own Logger implementation, so exclude SLF4J SimpleLogger
configurations.implementation {
    exclude(group = "org.slf4j", module = "slf4j-simple")
}
