project.description = "Java utilities (samples)"

plugins {
    id("application")
}

java {
    targetCompatibility = JavaVersion.VERSION_21
    sourceCompatibility = targetCompatibility

    withJavadocJar()
    withSourcesJar()
}

application {
    mainClass.set("com.dua3.utility.samples.graphics.FxGraphicsSample")
}

description = "samples"

dependencies {
    implementation(project(":utility"))
    implementation(project(":utility-fx"))
    implementation(project(":utility-fx-controls"))
    implementation(project(":utility-swing"))
    implementation(project(":utility-logging"))
    implementation(project(":utility-logging-log4j"))
    implementation(rootProject.libs.log4j.core)
    implementation(rootProject.libs.log4j.jul)
    implementation(rootProject.libs.log4j.slf4j2)
    implementation(rootProject.libs.slf4j.api)
}
