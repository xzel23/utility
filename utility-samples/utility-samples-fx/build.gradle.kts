project.description = "Java utilities (samples - JavaFX Log Pane)"

java {
    toolchain { languageVersion.set(JavaLanguageVersion.of(21)) }
    withJavadocJar()
    withSourcesJar()

    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

dependencies {
    implementation(project(":utility"))
    implementation(project(":utility-fx"))
    implementation(project(":utility-fx-controls"))
    implementation(project(":utility-fx-icons"))
    implementation(project(":utility-fx-icons-ikonli"))
    runtimeOnly(rootProject.libs.ikonli.fontawesome)
    runtimeOnly(rootProject.libs.ikonli.javafx)
    implementation(project(":utility-logging"))
    implementation(project(":utility-logging-log4j"))
    implementation(rootProject.libs.log4j.core)
    implementation(rootProject.libs.log4j.jcl)
    implementation(rootProject.libs.log4j.jul)
    implementation(rootProject.libs.log4j.slf4j2)
    implementation(rootProject.libs.slf4j.api)
}
