import org.gradle.internal.impldep.org.junit.experimental.categories.Categories.CategoryFilter.exclude
import org.gradle.kotlin.dsl.invoke

project.description = "Java utilities (swing)"

dependencies {
    implementation(project(":utility"))
    compileOnly(project(":utility-logging"))

    // JUnit4, AssertJ Swing and caciocavallo for UI testing
    testImplementation(rootProject.libs.junit4)
    testImplementation(rootProject.libs.assertj.core)
    testImplementation(rootProject.libs.assertj.swing.junit)
    testImplementation(rootProject.libs.caciocavallo)
}

tasks.withType<Test> {
    // Use JUnit 4
    useJUnit()

    // enable assertions and use caciocavallo for headless Swing testing
    jvmArgs(
        "-ea",
        "-Djava.awt.headless=false",
        "-Dawt.toolkit=org.caciocavallo.CaciocavalloToolkit",
        "-Dcacio.managed.screensize=1024x768"
    )
}
