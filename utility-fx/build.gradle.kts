project.description = "Java utilities (JavaFX)"

java {
    targetCompatibility = JavaVersion.VERSION_21
    sourceCompatibility = targetCompatibility

    withJavadocJar()
    withSourcesJar()
}

dependencies {
    implementation(project(":utility"))
    compileOnly(project(":utility-logging"))
}
