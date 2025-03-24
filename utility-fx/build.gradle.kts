project.description = "Java utilities (JavaFX)"

java {
    version = JavaVersion.VERSION_21
    withJavadocJar()
    withSourcesJar()

    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

dependencies {
    implementation(project(":utility"))
    compileOnly(project(":utility-logging"))
}
