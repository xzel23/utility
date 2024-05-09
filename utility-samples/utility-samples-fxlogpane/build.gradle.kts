project.description = "Java utilities (samples - JavaFX Log Pane)"

plugins {
    alias(libs.plugins.javafx)
}

javafx {
    configuration = "implementation"
    modules = listOf("javafx.base", "javafx.fxml", "javafx.controls")
}

dependencies {
    implementation(project(":utility"))
    implementation(project(":utility-fx"))
    implementation(project(":utility-logging"))
    implementation(project(":utility-logging:utility-logging-log4j"))
    implementation(rootProject.libs.log4j.core)
    implementation(rootProject.libs.log4j.jcl)
    implementation(rootProject.libs.log4j.jul)
    implementation(rootProject.libs.log4j.slf4j2)
    implementation(rootProject.libs.slf4j.api)
}
