project.description = "Java utilities (samples)"

plugins {
    id("application")
    alias(libs.plugins.javafx)
}

javafx {
    version = libs.versions.javafx.get()
    modules = listOf("javafx.base", "javafx.controls", "javafx.graphics")
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

tasks.register<JavaExec>("runFxGraphicsSample") {
    description = "Run the FxGraphicsSample application."
    group = ApplicationPlugin.APPLICATION_GROUP
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.dua3.utility.samples.graphics.FxGraphicsSample")
    enableAssertions = true
}

tasks.register<JavaExec>("runFxTextRendering") {
    description = "Run the FxTextRendering application."
    group = ApplicationPlugin.APPLICATION_GROUP
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.dua3.utility.samples.graphics.FxTextRendering")
    enableAssertions = true
}

tasks.register<JavaExec>("runSwingGraphicsSample") {
    description = "Run the SwingGraphicsSample application."
    group = ApplicationPlugin.APPLICATION_GROUP
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.dua3.utility.samples.graphics.SwingGraphicsSample")
    enableAssertions = true
}
