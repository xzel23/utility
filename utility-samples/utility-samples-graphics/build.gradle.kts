project.description = "Java utilities (samples)"

plugins {
    id("application")
    alias(libs.plugins.javafx) apply false
}

val isWindowsArm = System.getProperty("os.name").startsWith("Windows", ignoreCase = true) &&
        (System.getProperty("os.arch").equals("aarch64", ignoreCase = true) || System.getProperty("os.arch").equals("arm64", ignoreCase = true))
val useJavaFxPlugin = !isWindowsArm

if (useJavaFxPlugin) {
    apply(plugin = libs.plugins.javafx.get().pluginId)
}

if (useJavaFxPlugin) {
    extensions.configure<org.openjfx.gradle.JavaFXOptions>("javafx") {
        version = libs.versions.javafx.get()
        modules = listOf("javafx.base", "javafx.controls", "javafx.graphics")
    }
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

fun createJavaFxRunTask(taskName: String, mainClassName: String, description: String) {
    tasks.register<JavaExec>(taskName) {
        this.description = description
        group = ApplicationPlugin.APPLICATION_GROUP
        classpath = sourceSets["main"].runtimeClasspath
        mainClass.set(mainClassName)
        enableAssertions = true

        doFirst {
            val javaFxModules = listOf("javafx.base", "javafx.controls", "javafx.graphics")
            jvmArgs = if (useJavaFxPlugin) {
                listOf(
                    "--module-path", classpath.asPath,
                    "--add-modules", javaFxModules.joinToString(",")
                )
            } else {
                listOf(
                    "--add-modules", javaFxModules.joinToString(",")
                )
            }
        }
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
