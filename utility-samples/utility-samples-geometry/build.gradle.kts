project.description = "Java utilities (samples)"

plugins {
    id("application")
    id("org.openjfx.javafxplugin") version "0.1.0"
}

// set main
application {
    mainClass.set("com.dua3.utility.samples.geom.Shape")
}

description = "samples"

dependencies {
    implementation(project(":utility"))
    implementation(project(":utility-swing"))
    implementation(project(":utility-logging"))
    implementation(project(":utility-logging-log4j"))
    implementation(rootProject.libs.miglayout.swing)
    implementation(rootProject.libs.log4j.core)
    implementation(rootProject.libs.log4j.jul)
    implementation(rootProject.libs.log4j.slf4j2)
    implementation(rootProject.libs.slf4j.api)
}
