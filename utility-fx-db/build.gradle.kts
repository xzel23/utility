project.description = "JavaFX utilities (database)"

plugins {
    alias(libs.plugins.javafx)
}

javafx {
    version = libs.versions.javafx.get()
    modules = listOf("javafx.controls")
}

dependencies {
    implementation(project(":utility-fx"))
    api(project(":utility-db"))
}
