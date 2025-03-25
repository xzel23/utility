description = "JavaFX utilities (web)"

java {
    targetCompatibility = JavaVersion.VERSION_21
    sourceCompatibility = targetCompatibility
    version = targetCompatibility.toString()

    withJavadocJar()
    withSourcesJar()
}

dependencies {
    implementation(project(":utility-fx-controls"))
}
