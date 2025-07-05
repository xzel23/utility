project.description = "JavaFX utilities (icons-ikonli)"

plugins {
    alias(libs.plugins.javafx)
}

javafx {
    version = libs.versions.javafx.get()
    modules = listOf("javafx.controls")
}

dependencies {
    api(project(":utility-fx-icons"))

    implementation(rootProject.libs.ikonli.javafx)
}
