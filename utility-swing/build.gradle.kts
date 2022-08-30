project.description = "Java utilities (swing)"

dependencies {
    implementation(project(":utility"))
    compileOnly(project(":utility-logging"))
    
    testImplementation(project(":utility-logging"))
    testImplementation("org.slf4j:jul-to-slf4j:2.0.0")
    testImplementation("org.apache.logging.log4j:log4j-to-slf4j:2.18.0")
}

task("runSwingComponentsSample", JavaExec::class) {
    mainClass.set("com.dua3.utility.swing.test.SwingComponentsSample")
    classpath = sourceSets["test"].runtimeClasspath
}
