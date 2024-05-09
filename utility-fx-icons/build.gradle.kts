plugins {
    alias(libs.plugins.javafx)
}

description = "JavaFX utilities (icons)"

javafx {
    version = rootProject.libs.versions.javafx.get()
    configuration = "compileOnly"
    modules = listOf("javafx.controls", "javafx.graphics")
}
