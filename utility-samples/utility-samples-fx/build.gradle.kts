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
    runtimeOnly(rootProject.libs.ikonli.fontawesome)
    runtimeOnly(rootProject.libs.ikonli.javafx)
}

tasks.register<JavaExec>("runDialogSample") {
    description = "Run the DialogSample application."
    group = ApplicationPlugin.APPLICATION_GROUP
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.dua3.utility.samples.fx.DialogSample")
    enableAssertions = true
}

tasks.register<JavaExec>("runFxLogPaneSample") {
    description = "Run the FxLogPaneSample application."
    group = ApplicationPlugin.APPLICATION_GROUP
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.dua3.utility.samples.fx.FxLogPaneSample")
    enableAssertions = true
}

tasks.register<JavaExec>("runIconViewSample") {
    description = "Run the IconViewSample application."
    group = ApplicationPlugin.APPLICATION_GROUP
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.dua3.utility.samples.fx.IconViewSample")
    enableAssertions = true
}

tasks.register<JavaExec>("runPinBoardSample") {
    description = "Run the PinBoardSample application."
    group = ApplicationPlugin.APPLICATION_GROUP
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.dua3.utility.samples.fx.PinBoardSample")
    enableAssertions = true
}

tasks.register<JavaExec>("runProgressViewSample") {
    description = "Run the ProgressViewSample application."
    group = ApplicationPlugin.APPLICATION_GROUP
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.dua3.utility.samples.fx.ProgressViewSample")
    enableAssertions = true
}

tasks.register<JavaExec>("runShapeFx") {
    description = "Run the ShapeFx application."
    group = ApplicationPlugin.APPLICATION_GROUP
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.dua3.utility.samples.fx.ShapeFx")
    enableAssertions = true
}
