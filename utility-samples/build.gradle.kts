project.description = "Java utilities (samples)"

dependencies {
    implementation(project(":utility"))
    implementation(project(":utility-swing"))
    implementation(project(":utility-logging"))
    implementation(rootProject.libs.miglayout.swing)
    implementation(rootProject.libs.log4j.api)
    implementation(rootProject.libs.slf4j.api)
}

tasks.register<JavaExec>("runSwingComboBoxExSample") {
    description = "Run the SwingComboBoxExSample application."
    group = ApplicationPlugin.APPLICATION_GROUP
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.dua3.utility.samples.SwingComboBoxExSample")
    enableAssertions = true
}
