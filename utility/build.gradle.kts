project.description = "Java utilities (core)"

// Define Java 25 source set
sourceSets {
    create("java25") {
        java {
            srcDir("src/main/java25")
        }
    }

    // Define Java 25 test source set
    create("testJava25") {
        java {
            srcDir("src/test/java25")
        }
        compileClasspath += sourceSets.test.get().output + sourceSets.main.get().output + sourceSets.getByName("java25").output
        runtimeClasspath += sourceSets.test.get().output + sourceSets.main.get().output + sourceSets.getByName("java25").output
    }

    // Define javaTestUtil source set for common test utilities
    create("javaTestUtil") {
        java {
            srcDir("src/testUtil/java")
        }
        resources {
            srcDir("src/testUtil/resources")
        }
        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().output
    }
}

dependencies {
    compileOnly(rootProject.libs.bouncycastle.provider)
    compileOnly(rootProject.libs.bouncycastle.pkix)
    testImplementation(rootProject.libs.bouncycastle.provider)
    testImplementation(rootProject.libs.bouncycastle.pkix)
}

// Add dependencies for java25 source set
dependencies {
    "java25Implementation"(sourceSets.main.get().output)
    // Add the same dependencies as the main source set
    configurations.implementation.get().allDependencies.forEach {
        "java25Implementation"(it)
    }

    // Add dependencies for testJava25 source set
    "testJava25Implementation"(sourceSets.getByName("java25").output)
    "testJava25Implementation"(sourceSets.main.get().output)
    "testJava25Implementation"(sourceSets.test.get().output)

    // Add the same dependencies as the test source set
    configurations.testImplementation.get().allDependencies.forEach {
        "testJava25Implementation"(it)
    }

    // Explicitly add JUnit dependencies for testJava25
    "testJava25Implementation"(platform(rootProject.libs.junit.bom))
    "testJava25Implementation"(rootProject.libs.junit.jupiter.api)
    "testJava25RuntimeOnly"(rootProject.libs.junit.jupiter.engine)
    "testJava25RuntimeOnly"(rootProject.libs.junit.platform.launcher)

    // Add dependencies for javaTestUtil source set
    "javaTestUtilImplementation"(sourceSets.main.get().output)
    "javaTestUtilImplementation"(platform(rootProject.libs.junit.bom))
    "javaTestUtilImplementation"(rootProject.libs.junit.jupiter.api)
}

// Configure Java 25 compilation
tasks.named<JavaCompile>("compileJava25Java") {
    // Use Java 25 toolchain for this task only
    javaCompiler.set(javaToolchains.compilerFor {
        languageVersion.set(JavaLanguageVersion.of(25))
    })

    // Make sure the Java 25 compilation can access the original module info
    options.compilerArgs.addAll(
        listOf(
            "--patch-module", "com.dua3.utility=${sourceSets.main.get().output.asPath}"
        )
    )

    // Set the release flag to 25
    options.release.set(25)
}

// Configure Java 25 test compilation
tasks.named<JavaCompile>("compileTestJava25Java") {
    // Use Java 25 toolchain for this task only
    javaCompiler.set(javaToolchains.compilerFor {
        languageVersion.set(JavaLanguageVersion.of(25))
    })

    // Make sure the Java 25 test compilation can access the original module info
    options.compilerArgs.addAll(
        listOf(
            "--patch-module",
            "com.dua3.utility=${sourceSets.main.get().output.asPath}:${sourceSets.getByName("java25").output.asPath}"
        )
    )

    // Set the release flag to 25
    options.release.set(25)
}

// Make sure the java25 compilation happens after the main compilation
tasks.named("compileJava25Java") {
    dependsOn(tasks.compileJava)
}

// Make sure the testJava25 compilation happens after the java25 compilation
tasks.named("compileTestJava25Java") {
    dependsOn(tasks.named("compileJava25Java"))
    dependsOn(tasks.compileTestJava)
}

// Create a task to run the Java 25 tests
val testJava25 = tasks.register<Test>("testJava25") {
    description = "Runs tests from the testJava25 source set"
    group = "verification"

    testClassesDirs = sourceSets.getByName("testJava25").output.classesDirs
    classpath = sourceSets.getByName("testJava25").runtimeClasspath

    // Use the Java 25 toolchain
    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(25))
    })

    jvmArgs("-Djava.awt.headless=true")

    // Configure JUnit Platform for running tests
    useJUnitPlatform()
}

// Make the main test task depend on the Java 25 test task
tasks.test {
    dependsOn(testJava25)
}

// Configure the JAR task to include Java 25 classes as a multi-release JAR
tasks.named<Jar>("jar") {
    // Include Java 25 classes in the JAR
    from(sourceSets.getByName("java25").output) {
        into("META-INF/versions/25")
    }

    // Mark as a multi-release JAR
    manifest {
        attributes(
            "Multi-Release" to "true"
        )
    }

    // Make sure java25 compilation happens before JAR creation
    dependsOn(tasks.named("compileJava25Java"))
}

// Create a JAR task for the javaTestUtil source set
val javaTestUtilJar = tasks.register<Jar>("javaTestUtilJar") {
    description = "Assembles a jar archive containing the javaTestUtil classes"
    group = "build"

    archiveBaseName.set("utility-test-util")
    from(sourceSets.getByName("javaTestUtil").output)

    // Make sure javaTestUtil compilation happens before JAR creation
    dependsOn(tasks.named("compileJavaTestUtilJava"))
}

// Create a configuration for the javaTestUtil JAR
configurations.create("javaTestUtil") {
    isCanBeConsumed = true
    isCanBeResolved = false
}

// Add the javaTestUtil JAR to the configuration
artifacts {
    add("javaTestUtil", javaTestUtilJar)
}

// Make sure the javaTestUtil JAR is built when the project is built
tasks.named("build") {
    dependsOn(javaTestUtilJar)
}

// Make sure the jar task runs before testJava25 task
tasks.named("testJava25") {
    dependsOn(tasks.jar)
}

// Disable forbiddenapis for some source sets
tasks.matching {
    listOf(
        "forbiddenApisJava25",
        "forbiddenApisJmh",
        "forbiddenApisJavaTestUtil",
        "forbiddenApisTest"
    ).contains(it.name)
}.configureEach {
    enabled = false
}
