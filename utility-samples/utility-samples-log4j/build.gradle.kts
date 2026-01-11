project.description = "Java utilities (samples)"

dependencies {
    implementation(project(":utility"))
    implementation(project(":utility-swing"))
    implementation(project(":utility-samples"))
    implementation(rootProject.libs.miglayout.swing)
    implementation(rootProject.libs.sawmill.lumberjack)
}

tasks.register<JavaExec>("runSwingComponentsSampleLog4j") {
    description = "Run the SwingComponentsSampleLog4j application."
    group = ApplicationPlugin.APPLICATION_GROUP
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.dua3.utility.samples.log4j.SwingComponentsSampleLog4j")
    enableAssertions = true
}
