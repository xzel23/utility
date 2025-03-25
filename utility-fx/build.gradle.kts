project.description = "Java utilities (JavaFX)"

java {
    targetCompatibility = JavaVersion.VERSION_21
    sourceCompatibility = targetCompatibility
    version = targetCompatibility.toString()

    withJavadocJar()
    withSourcesJar()
}

dependencies {
    implementation(project(":utility"))
    compileOnly(project(":utility-logging"))
}
