project.description = "Java utilities (common logger sample)"

dependencies {
    implementation(project(":utility"))
    implementation(project(":utility-swing"))
    implementation(project(":utility-logging"))
    implementation(project(":utility-samples"))
    implementation(project(":utility-logging-log4j"))
    implementation(rootProject.libs.miglayout.swing)
    implementation(rootProject.libs.log4j.api)
    implementation(rootProject.libs.slf4j.api)
    implementation(rootProject.libs.commons.logging)
}

tasks.register<JavaExec>("runSwingComponentsSampleCommonLogger") {
    description = "Run the SwingComponentsSampleCommonLogger application."
    group = ApplicationPlugin.APPLICATION_GROUP
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.dua3.utility.samples.log4j.SwingComponentsSampleCommonLogger")
    enableAssertions = true
}
