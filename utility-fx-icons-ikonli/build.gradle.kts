description = "JavaFX utilities (icons-ikonli)"

java {
    targetCompatibility = JavaVersion.VERSION_21
    sourceCompatibility = targetCompatibility
    version = targetCompatibility.toString()

    withJavadocJar()
    withSourcesJar()
}

dependencies {
    api(project(":utility-fx-icons"))

    implementation(rootProject.libs.ikonli.javafx)
}
