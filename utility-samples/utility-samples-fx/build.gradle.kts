project.description = "Java utilities (samples - JavaFX Log Pane)"

plugins {
    id("application")
    alias(libs.plugins.javafx)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }

    withJavadocJar()
    withSourcesJar()
}

javafx {
    version = libs.versions.javafx.get()
    modules = listOf("javafx.base", "javafx.controls", "javafx.graphics")
}

dependencies {
    implementation(project(":utility"))
    implementation(project(":utility-fx"))
    implementation(project(":utility-fx-controls"))
    implementation(project(":utility-fx-icons"))
    implementation(project(":utility-fx-icons-ikonli"))
    implementation(project(":utility-logging"))
    implementation(project(":utility-logging-log4j"))
    implementation(rootProject.libs.log4j.core)
    implementation(rootProject.libs.log4j.jcl)
    implementation(rootProject.libs.log4j.jul)
    implementation(rootProject.libs.log4j.slf4j2)
    implementation(rootProject.libs.slf4j.api)
    runtimeOnly(rootProject.libs.ikonli.fontawesome6)
    runtimeOnly(rootProject.libs.ikonli.javafx)
}

fun createJavaFxRunTask(taskName: String, mainClassName: String, description: String) {
    tasks.register<JavaExec>(taskName) {
        this.description = description
        group = ApplicationPlugin.APPLICATION_GROUP
        classpath = sourceSets["main"].runtimeClasspath
        mainClass.set(mainClassName)
        enableAssertions = true

        doFirst {
            val javaFxModules = listOf("javafx.base", "javafx.controls", "javafx.graphics")
            jvmArgs = listOf(
                "--module-path", classpath.asPath,
                "--add-modules", javaFxModules.joinToString(",")
            )
        }
    }
}

createJavaFxRunTask("runFxDialogSample", "com.dua3.utility.samples.fx.FxDialogSample", "Run the DialogSample application.")
createJavaFxRunTask("runFxLogPaneSample", "com.dua3.utility.samples.fx.FxLogPaneSample", "Run the FxLogPaneSample application.")
createJavaFxRunTask("runIconViewSample", "com.dua3.utility.samples.fx.IconViewSample", "Run the IconViewSample application.")
createJavaFxRunTask("runPinBoardSample", "com.dua3.utility.samples.fx.PinBoardSample", "Run the PinBoardSample application.")
createJavaFxRunTask("runProgressViewSample", "com.dua3.utility.samples.fx.ProgressViewSample", "Run the ProgressViewSample application.")
createJavaFxRunTask("runShapeFx", "com.dua3.utility.samples.fx.ShapeFx", "Run the ShapeFx application.")
