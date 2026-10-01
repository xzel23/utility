project.description = "Java utilities (database)"

dependencies {
    implementation(project(":utility"))

    testImplementation(project(path = ":utility", configuration = "javaTestUtil"))
    testImplementation(rootProject.libs.h2)
}
