project.description = "Java utilities (samples - JavaFX Log Pane)"

plugins {
    id("application")
}

jdk {
    version = 25
    javaFxBundled = true
    nativeImageCapable = false
}

java {
    withJavadocJar()
    withSourcesJar()
}

dependencies {
    implementation(project(":utility"))
    implementation(project(":utility-fx"))
    implementation(project(":utility-fx-controls"))
    implementation(project(":utility-fx-icons"))
    implementation(project(":utility-fx-icons-ikonli"))
    implementation(project(":utility-logging"))
    implementation(rootProject.libs.atlantafx)
    implementation(rootProject.libs.log4j.jcl)
    implementation(rootProject.libs.log4j.jul)
    implementation(rootProject.libs.log4j.slf4j2)
    implementation(rootProject.libs.slf4j.api)
    runtimeOnly(rootProject.libs.ikonli.fontawesome6)
    runtimeOnly(rootProject.libs.ikonli.javafx)
}

fun createJavaFxRunTask(taskName: String, mainClassName: String, taskDescription: String) {
    tasks.register<JavaExec>(taskName) {
        description = taskDescription
        group = ApplicationPlugin.APPLICATION_GROUP
        
        val java25Output = project(":utility").sourceSets.getByName("java25").output
        classpath = files(java25Output) + sourceSets["main"].runtimeClasspath
        
        mainClass.set(mainClassName)
        enableAssertions = true
        jvmArgs(
            "--enable-native-access=javafx.graphics",
            "--enable-native-access=ALL-UNNAMED",
            "--add-opens=javafx.graphics/javafx.stage=ALL-UNNAMED",
            "--add-opens=javafx.graphics/com.sun.glass.ui=ALL-UNNAMED"
        )
    }
}

createJavaFxRunTask(
    "runFxDialogSample",
    "com.dua3.utility.samples.fx.FxDialogSample",
    "Run the DialogSample application."
)
createJavaFxRunTask(
    "runFxLogPaneSample",
    "com.dua3.utility.samples.fx.FxLogPaneSample",
    "Run the FxLogPaneSample application."
)
createJavaFxRunTask(
    "runIconViewSample",
    "com.dua3.utility.samples.fx.IconViewSample",
    "Run the IconViewSample application."
)
createJavaFxRunTask(
    "runPinBoardSample",
    "com.dua3.utility.samples.fx.PinBoardSample",
    "Run the PinBoardSample application."
)
createJavaFxRunTask(
    "runProgressViewSample",
    "com.dua3.utility.samples.fx.ProgressViewSample",
    "Run the ProgressViewSample application."
)
createJavaFxRunTask("runShapeFx", "com.dua3.utility.samples.fx.ShapeFx", "Run the ShapeFx application.")
