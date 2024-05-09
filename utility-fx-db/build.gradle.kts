description = "JavaFX utilities (database)"

plugins {
    alias(libs.plugins.javafx)
}

javafx {
    version = rootProject.libs.versions.javafx.get()
    configuration = "compileOnly"
    modules = listOf("javafx.controls", "javafx.graphics")
}

dependencies {
    implementation(project(":utility-fx"))
    api(project(":utility-db"))
}
