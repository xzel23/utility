plugins {
    alias(libs.plugins.javafx)
}

project.description = "Java utilities (JavaFX)"

dependencies {
    implementation(project(":utility"))
    compileOnly(project(":utility-logging"))
}

javafx {
    version = rootProject.libs.versions.javafx.get()
    configuration = "compileOnly"
    modules = listOf("javafx.controls")
}
