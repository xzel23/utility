project.description = "Java utilities (samples)"

dependencies {
    implementation(project(":utility"))
    implementation(project(":utility-swing"))
    implementation(project(":utility-logging"))
    implementation(rootProject.libs.miglayout.swing)
    implementation(rootProject.libs.log4j.api)
    implementation(rootProject.libs.slf4j.api)
}

tasks.register<JavaExec>("runFfmTestApp") {
    description = "Run the FfmTestApp application for GraalVM configuration generation."
    group = ApplicationPlugin.APPLICATION_GROUP
    
    val java25Output = project(":utility").sourceSets.getByName("java25").output
    classpath = files(java25Output) + sourceSets["main"].runtimeClasspath
    
    mainClass.set("com.dua3.utility.samples.FfmTestApp")
    enableAssertions = true
    
    val launcher = javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
    setExecutable(launcher.map { it.executablePath.asFile.absolutePath })

    // FFM requires native access
    jvmArgs("--enable-native-access=ALL-UNNAMED")
}

tasks.register<JavaExec>("runSwingComboBoxExSample") {
    description = "Run the SwingComboBoxExSample application."
    group = ApplicationPlugin.APPLICATION_GROUP
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.dua3.utility.samples.SwingComboBoxExSample")
    enableAssertions = true
}
