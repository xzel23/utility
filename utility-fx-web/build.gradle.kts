description = "JavaFX utilities (web)"

java {
    version = JavaVersion.VERSION_21.toString()
    withJavadocJar()
    withSourcesJar()

    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

dependencies {
    implementation(project(":utility-fx-controls"))
}
