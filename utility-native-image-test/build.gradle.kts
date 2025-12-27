project.description = "Java utilities (GraalVM native-image test)"

plugins {
    id("application")
    alias(libs.plugins.native)
}

jdk {
    version = 25
    javaFxBundled = true
    nativeImageCapable = true
}

// User will configure JDK and native-image manually

dependencies {
    implementation(project(":utility"))
    implementation(project(":utility-fx"))
    implementation(project(":utility-logging"))
    implementation(project(":utility-logging-log4j"))
    implementation(rootProject.libs.log4j.core)
    implementation(rootProject.libs.slf4j.api)
}

application {
    mainClass.set("com.dua3.utility.native_test.FfmTestApp")
    applicationDefaultJvmArgs = listOf(
        "--enable-native-access=javafx.graphics,ALL-UNNAMED",
        "--add-opens=javafx.graphics/javafx.stage=ALL-UNNAMED",
        "--add-opens=javafx.graphics/com.sun.glass.ui=ALL-UNNAMED"
    )
}

graalvmNative {
    binaries {
        named("main") {
            javaLauncher = jdk.getJavaLauncher(project)
            imageName.set("FfmTestApp")
            mainClass.set("com.dua3.utility.native_test.FfmTestApp")
            buildArgs.addAll(
                "--enable-native-access=javafx.graphics,ALL-UNNAMED",
                "--initialize-at-build-time=org.apache.logging.log4j",
                "--initialize-at-run-time=com.dua3.utility.application.imp.NativeHelperMacOs",
                "--initialize-at-run-time=com.dua3.utility.application.imp.DarkModeDetectorMacOs",
                "--initialize-at-run-time=com.dua3.utility.application.imp.NativeHelperWindows",
                "--initialize-at-run-time=com.dua3.utility.application.imp.DarkModeDetectorWindows",
                "-H:+UnlockExperimentalVMOptions",
                "-H:+ForeignAPISupport"
            )
        }
    }
}

// Ensure the FFM-related classes from the 'java25' source set are available
val java25Output = project(":utility").sourceSets.getByName("java25").output
sourceSets["main"].runtimeClasspath += files(java25Output)
sourceSets["main"].compileClasspath += files(java25Output)
