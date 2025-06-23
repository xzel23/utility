project.description = "Java utilities (samples - BOM usage)"

dependencies {
    // For demonstration purposes, we'll use project dependencies
    // In a real project, you would use the BOM like this:
    // implementation(platform("com.dua3.utility:utility-bom:${project.version}"))

    // Use project dependencies for now
    implementation(project(":utility"))
    implementation(project(":utility-logging"))

    // You can also mix with other BOMs
    implementation(platform(rootProject.libs.log4j.bom))
    implementation(rootProject.libs.log4j.core)
    implementation(rootProject.libs.log4j.slf4j2)
}

// Fix task dependency issue
tasks.named("compileJava") {
    dependsOn(":utility-logging:cabe", ":utility:cabe")
}

tasks.register<JavaExec>("runBomSample") {
    description = "Run the BomSample application."
    group = ApplicationPlugin.APPLICATION_GROUP
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.dua3.utility.samples.bom.BomSample")
    enableAssertions = true
}
