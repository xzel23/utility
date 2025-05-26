import org.gradle.kotlin.dsl.invoke

project.description = "Java utilities (swing)"

dependencies {
    implementation(project(":utility"))
    compileOnly(project(":utility-logging"))

    // AssertJ Swing for UI testing
    testImplementation(rootProject.libs.assertj.core)
    testImplementation(rootProject.libs.assertj.swing.junit)
    testImplementation(rootProject.libs.caciocavallo)
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()

            dependencies {
                implementation(rootProject.libs.log4j.core)
                implementation(rootProject.libs.jimfs)
                implementation(rootProject.libs.mockito)
            }

            targets {
                all {
                    testTask {
                        // enable assertions and use caciocavallo for headless Swing testing
                        jvmArgs(
                            "-ea",
                            "-Djava.awt.headless=false",
                            "-Dawt.toolkit=org.caciocavallo.CaciocavalloToolkit",
                            "-Dcacio.managed.screensize=1024x768"
                        )
                    }
                }
            }
        }
    }
}
