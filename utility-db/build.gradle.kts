description = "Java utilities (database)"

dependencies {
    implementation(project(":utility"))

    testImplementation(rootProject.libs.h2)
}
