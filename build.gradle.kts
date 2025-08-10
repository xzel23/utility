// Copyright (c) 2019, 2022 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

@file:Suppress("UnstableApiUsage")

import com.adarshr.gradle.testlogger.theme.ThemeType
import com.dua3.cabe.processor.Configuration
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.external.javadoc.StandardJavadocDocletOptions
import org.gradle.internal.extensions.stdlib.toDefaultLowerCase

plugins {
    id("java-library")
    id("jvm-test-suite")
    id("version-catalog")
    id("signing")
    id("idea")
    id("jacoco-report-aggregation")
    alias(libs.plugins.versions)
    alias(libs.plugins.test.logger)
    alias(libs.plugins.spotbugs)
    alias(libs.plugins.cabe)
    alias(libs.plugins.forbiddenapis)
    alias(libs.plugins.jmh)
    alias(libs.plugins.sonar)
    alias(libs.plugins.jreleaser)
}

/////////////////////////////////////////////////////////////////////////////
// Meta data object
/////////////////////////////////////////////////////////////////////////////
object Meta {
    const val DESCRIPTION = "Utility library for Java"
    const val INCEPTION_YEAR = "2019"
    const val GROUP = "com.dua3.utility"
    const val SCM = "https://github.com/xzel23/utility.git"
    const val LICENSE_NAME = "MIT"
    const val LICENSE_URL = "https://opensource.org/licenses/MIT"
    const val DEVELOPER_ID = "axh"
    const val DEVELOPER_NAME = "Axel Howind"
    const val DEVELOPER_EMAIL = "axh@dua3.com"
    const val ORGANIZATION_NAME = "dua3"
    const val ORGANIZATION_URL = "https://www.dua3.com"
}

/////////////////////////////////////////////////////////////////////////////
// Root project configuration
/////////////////////////////////////////////////////////////////////////////

project.version = libs.versions.projectVersion.get()
project.description = Meta.DESCRIPTION

tasks.register("printVersion") {
    description = "Print the project version to stdout."
    group = HelpTasksPlugin.HELP_GROUP
    doLast { println(project.version) }
}

// Task to display inputs and outputs of a specified task
tasks.register("showTaskIO") {
    description = "Shows the inputs and outputs of a specified Gradle task."
    group = HelpTasksPlugin.HELP_GROUP
    
    // Define a property for the task name
    val taskName = project.findProperty("taskName") as String? ?: "help"
    
    doLast {
        val task = project.tasks.findByName(taskName)
        if (task == null) {
            println("Task '$taskName' not found. Available tasks:")
            project.tasks.names.sorted().forEach { println("  $it") }
            return@doLast
        }
        
        println("\n=== Task: ${task.path} ===")
        
        // Display task inputs
        println("\nINPUTS:")
        if (task.inputs.hasInputs) {
            // Safely access properties
            try {
                val properties = task.inputs.properties
                if (properties.isNotEmpty()) {
                    properties.entries.forEach { entry ->
                        println("  ${entry.key}: ${entry.value}")
                    }
                } else {
                    println("  No input properties.")
                }
            } catch (e: Exception) {
                println("  Error accessing input properties: ${e.message}")
            }
            
            println("\n  Input Files:")
            try {
                val files = task.inputs.files.files
                if (files.isNotEmpty()) {
                    files.forEach { file ->
                        println("    ${file.absolutePath} (exists: ${file.exists()})")
                    }
                } else {
                    println("    No input files.")
                }
            } catch (e: Exception) {
                println("  Error accessing input files: ${e.message}")
            }
        } else {
            println("  No declared inputs.")
        }
        
        // Display task outputs
        println("\nOUTPUTS:")
        if (task.outputs.hasOutput) {
            println("  Output Files:")
            try {
                val files = task.outputs.files.files
                if (files.isNotEmpty()) {
                    files.filter { !it.isDirectory }.forEach { file ->
                        println("    ${file.absolutePath} (exists: ${file.exists()})")
                    }
                } else {
                    println("    No output files.")
                }
                
                println("\n  Output Directories:")
                if (files.any { it.isDirectory }) {
                    files.filter { it.isDirectory }.forEach { dir ->
                        println("    ${dir.absolutePath} (exists: ${dir.exists()})")
                    }
                } else {
                    println("    No output directories.")
                }
            } catch (e: Exception) {
                println("  Error accessing output files: ${e.message}")
            }
        } else {
            println("  No declared outputs.")
        }
        
        println("\n=== End of Task Info ===\n")
    }
}

// Aggregate all subprojects for JaCoCo report aggregation

dependencies {
    jacocoAggregation(rootProject)
    jacocoAggregation(project(":utility"))
    jacocoAggregation(project(":utility-db"))
    jacocoAggregation(project(":utility-swing"))
    jacocoAggregation(project(":utility-fx"))
    jacocoAggregation(project(":utility-fx-icons"))
    jacocoAggregation(project(":utility-fx-icons-ikonli"))
    jacocoAggregation(project(":utility-fx-controls"))
    jacocoAggregation(project(":utility-fx-db"))
    jacocoAggregation(project(":utility-fx-web"))
    jacocoAggregation(project(":utility-logging"))
    jacocoAggregation(project(":utility-logging-slf4j"))
    jacocoAggregation(project(":utility-logging-log4j"))
}

tasks.named<JacocoReport>("testCodeCoverageReport") {
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

// SonarQube root project config
sonar {
    properties {
        property("sonar.coverage.jacoco.xmlReportPaths", "${layout.buildDirectory.get()}/reports/jacoco/testCodeCoverageReport/testCodeCoverageReport.xml")
        property("sonar.coverage.exclusions", "**/samples/**")
    }
}

fun isDevelopmentVersion(versionString: String): Boolean {
    val v = versionString.toDefaultLowerCase()
    val markers = listOf("snapshot", "alpha", "beta")
    return markers.any { marker -> v.contains("-$marker") || v.contains(".$marker") }
}

val isReleaseVersion = !isDevelopmentVersion(project.version.toString())
val isSnapshot = project.version.toString().toDefaultLowerCase().contains("snapshot")

/////////////////////////////////////////////////////////////////////////////
// Subprojects configuration
/////////////////////////////////////////////////////////////////////////////

subprojects {

    // Set project version from root libs.versions
    project.version = rootProject.libs.versions.projectVersion.get()

    // Apply common plugins
    apply(plugin = "maven-publish")
    apply(plugin = "version-catalog")
    apply(plugin = "signing")
    apply(plugin = "idea")
    apply(plugin = rootProject.libs.plugins.versions.get().pluginId)
    apply(plugin = rootProject.libs.plugins.test.logger.get().pluginId)

    // Skip some plugins for BOM project
    if (!project.name.endsWith("-bom")) {
        apply(plugin = "jacoco")
        apply(plugin = "java-library")
        apply(plugin = "jvm-test-suite")
        apply(plugin = rootProject.libs.plugins.spotbugs.get().pluginId)
        apply(plugin = rootProject.libs.plugins.cabe.get().pluginId)
        apply(plugin = rootProject.libs.plugins.forbiddenapis.get().pluginId)
        apply(plugin = rootProject.libs.plugins.jmh.get().pluginId)
    }

    // Java configuration for non-BOM projects
    if (!project.name.endsWith("-bom")) {
        java {
            toolchain {
                languageVersion.set(JavaLanguageVersion.of(21))
            }
            targetCompatibility = JavaVersion.VERSION_21
            sourceCompatibility = targetCompatibility

            withJavadocJar()
            withSourcesJar()
        }

        cabe {
            if (isReleaseVersion) {
                config.set(Configuration.parse("publicApi=THROW_IAE:privateApi=ASSERT"))
            } else {
                config.set(Configuration.DEVELOPMENT)
            }
        }

        // JaCoCo
        tasks.withType<JacocoReport> {
            reports {
                xml.required.set(true)
                html.required.set(false)
            }
        }

        tasks.withType<Test> {
            useJUnitPlatform()
            finalizedBy(tasks.jacocoTestReport)
        }
    }

    // SonarQube properties
    sonar {
        properties {
            property("sonar.coverage.jacoco.xmlReportPaths", "**/build/reports/jacoco/test/jacocoTestReport.xml")
            property("sonar.coverage.exclusions", "**/samples/**")
        }
    }

    // Dependencies for non-BOM projects
    if (!project.name.endsWith("-bom")) {
        dependencies {
            implementation(rootProject.libs.jspecify)
            implementation(platform(rootProject.libs.log4j.bom))
            implementation(rootProject.libs.log4j.api)
        }

        idea {
            module {
                inheritOutputDirs = false
                outputDir = project.layout.buildDirectory.file("classes/java/main/").get().asFile
                testOutputDir = project.layout.buildDirectory.file("classes/java/test/").get().asFile
            }
        }

        testing {
            suites {
                val test by getting(JvmTestSuite::class) {
                    useJUnitJupiter()
                    dependencies {
                        implementation(rootProject.libs.log4j.core)
                        implementation(rootProject.libs.jimfs)
                    }
                    targets {
                        all {
                            testTask {
                                // enable assertions and use headless mode for AWT in unit tests
                                jvmArgs(
                                    "-ea",
                                    "-Djava.awt.headless=true",
                                    "-Dprism.order=sw",
                                    "-Dsun.java2d.d3d=false",
                                    "-Dsun.java2d.opengl=false",
                                    "-Dsun.java2d.pmoffscreen=false"
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    testlogger {
        theme = ThemeType.MOCHA_PARALLEL
    }

    // Java compilation and Javadoc config for non-BOM projects
    if (!project.name.endsWith("-bom")) {
        tasks.compileJava {
            options.encoding = "UTF-8"
            options.compilerArgs.addAll(listOf("-Xlint:deprecation", "-Xlint:-module", "-Xlint:unchecked"))
            options.javaModuleVersion.set(provider { project.version as String })
            options.release.set(java.targetCompatibility.majorVersion.toInt())
        }
        tasks.compileTestJava {
            options.encoding = "UTF-8"
            options.compilerArgs.addAll(listOf("-Xlint:deprecation", "-Xlint:-module", "-Xlint:unchecked"))
        }
        tasks.javadoc {
            (options as StandardJavadocDocletOptions).apply {
                encoding = "UTF-8"
                addStringOption("Xdoclint:all,-missing/private")
                locale = "en_US"
            }
        }
    }

    // JMH config for non-BOM projects
    if (!project.name.endsWith("-bom")) {
        jmh {
            jmhVersion = rootProject.libs.versions.jmh
            warmupIterations = 2
            iterations = 5
            fork = 1
        }
    }

    // Forbidden APIs and SpotBugs for non-BOM projects
    if (!project.name.endsWith("-bom")) {
        // === FORBIDDEN APIS ===
        tasks.withType(de.thetaphi.forbiddenapis.gradle.CheckForbiddenApis::class).configureEach {
            bundledSignatures = setOf("jdk-internal", "jdk-deprecated")
            ignoreFailures = false
        }

        // === SPOTBUGS ===
        spotbugs {
            toolVersion.set(rootProject.libs.versions.spotbugs)
            excludeFilter.set(rootProject.file("spotbugs-exclude.xml"))
        }

        tasks.named<com.github.spotbugs.snom.SpotBugsTask>("spotbugsMain") {
            reports.create("html") {
                required.set(true)
                outputLocation.set(layout.buildDirectory.file("reports/spotbugs/main.html"))
                setStylesheet("fancy-hist.xsl")
            }
        }

        tasks.named<com.github.spotbugs.snom.SpotBugsTask>("spotbugsTest") {
            reports.create("html") {
                required.set(true)
                outputLocation.set(layout.buildDirectory.file("reports/spotbugs/test.html"))
                setStylesheet("fancy-hist.xsl")
            }
        }
    }

    // Jar duplicates strategy for non-BOM projects
    if (!project.name.endsWith("-bom")) {
        tasks.withType<Jar> {
            duplicatesStrategy = DuplicatesStrategy.INCLUDE
        }
    }

    // --- PUBLISHING ---

    configure<PublishingExtension> {
        // Repositories for publishing
        repositories {
            // Sonatype snapshots for snapshot versions
            if (isSnapshot) {
                maven {
                    name = "sonatypeSnapshots"
                    url = uri("https://central.sonatype.com/repository/maven-snapshots/")
                    credentials {
                        username = System.getenv("SONATYPE_USERNAME")
                        password = System.getenv("SONATYPE_PASSWORD")
                    }
                }
            }

            // Always add root-level staging directory for JReleaser
            maven {
                name = "stagingDirectory"
                url = rootProject.layout.buildDirectory.dir("staging-deploy").get().asFile.toURI()
            }
        }

        // Publications for non-BOM projects
        if (!project.name.endsWith("-bom")) {
            publications {
                create<MavenPublication>("mavenJava") {
                    from(components["java"])

                    groupId = Meta.GROUP
                    artifactId = project.name
                    version = project.version.toString()

                    pom {
                        name.set(project.name)
                        description.set(project.description)
                        url.set(Meta.SCM)

                        licenses {
                            license {
                                name.set(Meta.LICENSE_NAME)
                                url.set(Meta.LICENSE_URL)
                            }
                        }

                        developers {
                            developer {
                                id.set(Meta.DEVELOPER_ID)
                                name.set(Meta.DEVELOPER_NAME)
                                email.set(Meta.DEVELOPER_EMAIL)
                                organization.set(Meta.ORGANIZATION_NAME)
                                organizationUrl.set(Meta.ORGANIZATION_URL)
                            }
                        }

                        scm {
                            connection.set("scm:git:${Meta.SCM}")
                            developerConnection.set("scm:git:${Meta.SCM}")
                            url.set(Meta.SCM)
                        }

                        withXml {
                            val root = asNode()
                            root.appendNode("inceptionYear", "2019")
                        }
                    }
                }
            }
        }
    }

    // Task to publish to staging directory per subproject
    val publishToStagingDirectory by tasks.registering {
        group = "publishing"
        description = "Publish artifacts to root staging directory for JReleaser"

        dependsOn(tasks.withType<PublishToMavenRepository>().matching {
            it.repository.name == "stagingDirectory"
        })
    }

    // Signing configuration deferred until after evaluation
    afterEvaluate {
        configure<SigningExtension> {
            val shouldSign = !project.version.toString().lowercase().contains("snapshot")
            setRequired(shouldSign && gradle.taskGraph.hasTask("publish"))

            val publishing = project.extensions.getByType<PublishingExtension>()

            if (project.name.endsWith("-bom")) {
                if (publishing.publications.names.contains("bomPublication")) {
                    sign(publishing.publications["bomPublication"])
                }
            } else {
                if (publishing.publications.names.contains("mavenJava")) {
                    sign(publishing.publications["mavenJava"])
                }
            }
        }
    }

    // set the project description after evaluation because it is not yet visible when the POM is first created
    afterEvaluate {
        project.extensions.configure<PublishingExtension> {
            publications.withType<MavenPublication> {
                pom {
                    if (description.orNull.isNullOrBlank()) {
                        description.set(project.description ?: "No description provided")
                    }
                }
            }
        }
    }
}

/////////////////////////////////////////////////////////////////////////////
// Root project tasks and JReleaser configuration
/////////////////////////////////////////////////////////////////////////////

// Aggregate all subprojects' publishToStagingDirectory tasks into a root-level task
tasks.register("publishToStagingDirectory") {
    group = "publishing"
    description = "Publish all subprojects' artifacts to root staging directory for JReleaser"

    dependsOn(subprojects.mapNotNull { it.tasks.findByName("publishToStagingDirectory") })
}

// add a task to create aggregate javadoc in the root projects build/docs/javadoc folder
tasks.register<Javadoc>("aggregateJavadoc") {
    group = "documentation"
    description = "Generates aggregated Javadoc for all subprojects"

    setDestinationDir(layout.buildDirectory.dir("docs/javadoc").get().asFile)
    setTitle("${rootProject.name} ${project.version} API")

    // Disable module path inference
    modularity.inferModulePath.set(false)

    // Configure the task to depend on all subprojects' javadoc tasks
    val filteredProjects = subprojects.filter {
        !it.name.endsWith("-bom") && !it.name.contains("samples")
    }

    dependsOn(filteredProjects.map { it.tasks.named("javadoc") })

    // Collect all Java source directories from subprojects, excluding module-info.java files
    source(filteredProjects.flatMap { project ->
        val sourceSets = project.extensions.getByType(SourceSetContainer::class.java)
        val main = sourceSets.findByName("main")
        main?.allJava?.filter { file ->
            !file.name.equals("module-info.java")
        } ?: files()
    })

    // Collect all classpaths from subprojects
    classpath = files(filteredProjects.flatMap { project ->
        val sourceSets = project.extensions.getByType(SourceSetContainer::class.java)
        val main = sourceSets.findByName("main")
        main?.compileClasspath ?: files()
    })

    // Add runtime classpath to ensure all dependencies are available
    classpath += files(filteredProjects.flatMap { project ->
        val sourceSets = project.extensions.getByType(SourceSetContainer::class.java)
        val main = sourceSets.findByName("main")
        main?.runtimeClasspath ?: files()
    })

    // Apply the same Javadoc options as in subprojects
    (options as StandardJavadocDocletOptions).apply {
        encoding = "UTF-8"
        addStringOption("Xdoclint:all,-missing/private")
        links("https://docs.oracle.com/en/java/javase/21/docs/api/")
        use(true)
        noTimestamp(true)
        windowTitle = "${rootProject.name} ${project.version} API"
        docTitle = "${rootProject.name} ${project.version} API"
        header = "${rootProject.name} ${project.version} API"
        // Set locale to English to ensure consistent language in generated documentation
        locale = "en_US"
        // Disable module path to avoid module-related errors
        addBooleanOption("module-path", false)
    }
}

jreleaser {
    project {
        name.set(rootProject.name)
        version.set(rootProject.libs.versions.projectVersion.get())
        group = Meta.GROUP
        authors.set(listOf(Meta.DEVELOPER_NAME))
        license.set(Meta.LICENSE_NAME)
        links {
            homepage.set(Meta.ORGANIZATION_URL)
        }
        inceptionYear.set(Meta.INCEPTION_YEAR)
        gitRootSearch.set(true)
    }

    signing {
        publicKey.set(System.getenv("SIGNING_PUBLIC_KEY"))
        secretKey.set(System.getenv("SIGNING_SECRET_KEY"))
        passphrase.set(System.getenv("SIGNING_PASSWORD"))
        active.set(org.jreleaser.model.Active.ALWAYS)
        armored.set(true)
    }

    deploy {
        maven {
            if (!isSnapshot) {
                println("adding release-deploy")
                mavenCentral {
                    create("release-deploy") {
                        active.set(org.jreleaser.model.Active.RELEASE)
                        url.set("https://central.sonatype.com/api/v1/publisher")
                        stagingRepositories.add("build/staging-deploy")
                        username.set(System.getenv("SONATYPE_USERNAME"))
                        password.set(System.getenv("SONATYPE_PASSWORD"))
                    }
                }
            } else {
                println("adding snapshot-deploy")
                nexus2 {
                    create("snapshot-deploy") {
                        active.set(org.jreleaser.model.Active.SNAPSHOT)
                        snapshotUrl.set("https://central.sonatype.com/repository/maven-snapshots/")
                        applyMavenCentralRules.set(true)
                        snapshotSupported.set(true)
                        closeRepository.set(true)
                        releaseRepository.set(true)
                        stagingRepositories.add("build/staging-deploy")
                        username.set(System.getenv("SONATYPE_USERNAME"))
                        password.set(System.getenv("SONATYPE_PASSWORD"))
                    }
                }
            }
        }
    }
}

/////////////////////////////////////////////////////////////////////////////
// Versions plugin configuration for all projects
/////////////////////////////////////////////////////////////////////////////

allprojects {
    fun isStable(version: String): Boolean {
        val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase().contains(it) }
        val regex = "[0-9,.v-]+-(rc|alpha|beta|b|M)(-?[0-9]*)?".toRegex()
        return stableKeyword || !regex.matches(version)
    }

    tasks.withType<DependencyUpdatesTask> {
        rejectVersionIf {
            !isStable(candidate.version)
        }
    }
}
