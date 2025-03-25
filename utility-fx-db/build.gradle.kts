description = "JavaFX utilities (database)"

java {
    targetCompatibility = JavaVersion.VERSION_21
    sourceCompatibility = targetCompatibility

    withJavadocJar()
    withSourcesJar()
}

dependencies {
    implementation(project(":utility-fx"))
    api(project(":utility-db"))
}
