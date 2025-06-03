description = "JavaFX utilities (web)"

plugins {
    alias(libs.plugins.javafx)
}

javafx {
    version = libs.versions.javafx.get()
    modules = listOf("javafx.controls", "javafx.web")
}

dependencies {
    implementation(project(":utility"))
    implementation(project(":utility-fx-controls"))
}
