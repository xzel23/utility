plugins {
    alias(libs.plugins.javafx)
}

description = "JavaFX utilities (icons-ikonli)"

javafx {
    version = rootProject.libs.versions.javafx.get()
    configuration = "compileOnly"
    modules = listOf("javafx.graphics")
}

dependencies {
    api(project(":utility-fx-icons"))

    implementation(rootProject.libs.ikonli.javafx)
}
