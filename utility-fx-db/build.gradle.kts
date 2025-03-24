description = "JavaFX utilities (database)"

java {
    version = JavaVersion.VERSION_21.toString()
    withJavadocJar()
    withSourcesJar()

    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

dependencies {
    implementation(project(":utility-fx"))
    api(project(":utility-db"))
}
