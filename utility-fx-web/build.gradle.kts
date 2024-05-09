description = "JavaFX utilities (web)"

plugins {
    alias(libs.plugins.javafx)
}

javafx {
    version = rootProject.libs.versions.javafx.get()
    configuration = "compileOnly"
    modules = listOf("javafx.web")
}

dependencies {
    implementation(project(":utility-fx-controls"))
}
