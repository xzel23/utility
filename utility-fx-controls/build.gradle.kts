description = "JavaFX utilities (controls)"

java {
    targetCompatibility = JavaVersion.VERSION_21
    sourceCompatibility = targetCompatibility
    version = targetCompatibility.toString()

    withJavadocJar()
    withSourcesJar()
}

dependencies {
    implementation(project(":utility"))
    implementation(project(":utility-fx"))
    implementation(project(":utility-fx-icons"))
}
