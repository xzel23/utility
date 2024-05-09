description = "JavaFX utilities (controls)"

plugins {
    alias(libs.plugins.javafx)
}

javafx {
    version = rootProject.libs.versions.javafx.get()
    configuration = "compileOnly"
    modules = listOf("javafx.controls", "javafx.fxml", "javafx.graphics")
}

dependencies {
    implementation(project(":utility"))
    implementation(project(":utility-fx"))
    implementation(project(":utility-fx-icons"))
}
