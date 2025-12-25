project.description = "Java utilities (samples)"

plugins {
    id("application")
}

application {
    mainClass.set("com.dua3.utility.samples.graphics.FxGraphicsSample")
}

project.description = "samples"

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

fun createJavaFxRunTask(taskName: String, mainClassName: String, taskDescription: String) {
    tasks.register<JavaExec>(taskName) {
        description = taskDescription
        group = ApplicationPlugin.APPLICATION_GROUP
        classpath = sourceSets["main"].runtimeClasspath
        mainClass.set(mainClassName)
        enableAssertions = true
        jvmArgs("--enable-native-access=javafx.graphics")
    }
}

createJavaFxRunTask(
    "runFxGraphicsSample",
    "com.dua3.utility.samples.graphics.FxGraphicsSample",
    "Run the FxGraphicsSample application."
)
createJavaFxRunTask(
    "runFxTextRendering",
    "com.dua3.utility.samples.graphics.FxTextRendering",
    "Run the FxTextRendering application."
)

tasks.register<JavaExec>("runSwingGraphicsSample") {
    description = "Run the SwingGraphicsSample application."
    group = ApplicationPlugin.APPLICATION_GROUP
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.dua3.utility.samples.graphics.SwingGraphicsSample")
    enableAssertions = true
}
