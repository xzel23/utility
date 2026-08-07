project.description = "JavaFX utilities (icons-ikonli)"

dependencies {
    api(project(":utility-fx-icons"))
    implementation(platform(libs.ikonli.bom))
    implementation(rootProject.libs.ikonli.javafx)
}
