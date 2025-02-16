description = "JavaFX utilities (icons-ikonli)"

java {
    toolchain { languageVersion.set(JavaLanguageVersion.of(21)) }
    withJavadocJar()
    withSourcesJar()

    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

dependencies {
    api(project(":utility-fx-icons"))

    implementation(rootProject.libs.ikonli.javafx)
}
