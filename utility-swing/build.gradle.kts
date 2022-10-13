project.description = "Java utilities (swing)"

dependencies {
    implementation(project(":utility"))
    compileOnly(project(":utility-logging"))

    testImplementation(project(":utility-logging"))
    testImplementation(rootProject.libs.jul.to.slf4j)
    testImplementation(rootProject.libs.log4j.to.slf4j)
}

// test utility-swing rely on our own Logger implementation, so exclude SLF4J SimpleLogger
configurations.testImplementation {
    exclude(group = "org.slf4j", module = "slf4j-simple")
}

task("runSwingComponentsSample", JavaExec::class) {
    mainClass.set("com.dua3.utility.swing.test.SwingComponentsSample")
    classpath = sourceSets["test"].runtimeClasspath
}
