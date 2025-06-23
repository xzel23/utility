project.description = "Java utilities (samples)"

dependencies {
    implementation(project(":utility"))
    implementation(project(":utility-swing"))
    implementation(project(":utility-logging"))
    implementation(project(":utility-samples"))
    implementation(project(":utility-logging-log4j"))
    implementation(rootProject.libs.miglayout.swing)
    implementation(rootProject.libs.log4j.core)
    implementation(rootProject.libs.log4j.jcl)
    implementation(rootProject.libs.log4j.jul)
    implementation(rootProject.libs.log4j.slf4j2)
    implementation(rootProject.libs.slf4j.api)
}

tasks.register<JavaExec>("runSwingComponentsSampleLog4j") {
    description = "Run the SwingComponentsSampleLog4j application."
    group = ApplicationPlugin.APPLICATION_GROUP
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.dua3.utility.samples.log4j.SwingComponentsSampleLog4j")
    enableAssertions = true
}
