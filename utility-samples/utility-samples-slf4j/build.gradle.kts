project.description = "Java utilities (samples)"

dependencies {
    implementation(project(":utility"))
    implementation(project(":utility-swing"))
    implementation(project(":utility-samples"))
    implementation(rootProject.libs.miglayout.swing)
    implementation(rootProject.libs.sawmill.lumberjack)
}

tasks.register<JavaExec>("runSwingComponentsSampleSlf4j") {
    description = "Run the SwingComponentsSampleSlf4j application."
    group = ApplicationPlugin.APPLICATION_GROUP
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.dua3.utility.samples.slf4j.SwingComponentsSampleSlf4j")
    enableAssertions = true
}
