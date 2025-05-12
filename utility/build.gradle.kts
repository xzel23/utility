description = "Java utilities (core)"

// Define Java 24 source set
sourceSets {
    create("java24") {
        java {
            srcDir("src/main/java24")
        }
    }

    // Define Java 24 test source set
    create("testJava24") {
        java {
            srcDir("src/test/java24")
        }
        compileClasspath += sourceSets.test.get().output + sourceSets.main.get().output + sourceSets.getByName("java24").output
        runtimeClasspath += sourceSets.test.get().output + sourceSets.main.get().output + sourceSets.getByName("java24").output
    }
}

// Add dependencies for java24 source set
dependencies {
    "java24Implementation"(sourceSets.main.get().output)
    // Add the same dependencies as the main source set
    configurations.implementation.get().allDependencies.forEach {
        "java24Implementation"(it)
    }

    // Add dependencies for testJava24 source set
    "testJava24Implementation"(sourceSets.getByName("java24").output)
    "testJava24Implementation"(sourceSets.main.get().output)
    "testJava24Implementation"(sourceSets.test.get().output)

    // Add the same dependencies as the test source set
    configurations.testImplementation.get().allDependencies.forEach {
        "testJava24Implementation"(it)
    }

    // Explicitly add JUnit dependencies for testJava24
    "testJava24Implementation"("org.junit.jupiter:junit-jupiter-api:5.12.2")
    "testJava24RuntimeOnly"("org.junit.jupiter:junit-jupiter-engine:5.12.2")
}


// Configure Java 24 compilation
tasks.named<JavaCompile>("compileJava24Java") {
    // Use Java 24 toolchain for this task only
    javaCompiler.set(javaToolchains.compilerFor {
        languageVersion.set(JavaLanguageVersion.of(24))
    })

    // Make sure the Java 24 compilation can access the original module info
    options.compilerArgs.addAll(listOf(
        "--patch-module", "com.dua3.utility=${sourceSets.main.get().output.asPath}"
    ))

    // Set the release flag to 24
    options.release.set(24)
}

// Configure Java 24 test compilation
tasks.named<JavaCompile>("compileTestJava24Java") {
    // Use Java 24 toolchain for this task only
    javaCompiler.set(javaToolchains.compilerFor {
        languageVersion.set(JavaLanguageVersion.of(24))
    })

    // Make sure the Java 24 test compilation can access the original module info
    options.compilerArgs.addAll(listOf(
        "--patch-module", "com.dua3.utility=${sourceSets.main.get().output.asPath}:${sourceSets.getByName("java24").output.asPath}"
    ))

    // Set the release flag to 24
    options.release.set(24)

    // Enable preview features (in case Gatherer API is still in preview)
    options.compilerArgs.add("--enable-preview")
}

// Make sure java24 compilation happens after main compilation
tasks.named("compileJava24Java") {
    dependsOn(tasks.compileJava)
}

// Make sure testJava24 compilation happens after java24 compilation
tasks.named("compileTestJava24Java") {
    dependsOn(tasks.named("compileJava24Java"))
    dependsOn(tasks.compileTestJava)
}

// Create a task to run the Java 24 tests
val testJava24 = tasks.register<Test>("testJava24") {
    description = "Runs tests from the testJava24 source set"
    group = "verification"

    testClassesDirs = sourceSets.getByName("testJava24").output.classesDirs
    classpath = sourceSets.getByName("testJava24").runtimeClasspath

    // Use the Java 24 toolchain
    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(24))
    })

    jvmArgs("-Djava.awt.headless=true")

    // Configure JUnit Platform for running tests
    useJUnitPlatform()
}

// Make the main test task depend on the Java 24 test task
tasks.test {
    dependsOn(testJava24)
}

// Configure JAR task to include Java 24 classes as a multi-release JAR
tasks.named<Jar>("jar") {
    // Include Java 24 classes in the JAR
    from(sourceSets.getByName("java24").output) {
        into("META-INF/versions/24")
    }

    // Mark as a multi-release JAR
    manifest {
        attributes(
            "Multi-Release" to "true"
        )
    }

    // Make sure java24 compilation happens before JAR creation
    dependsOn(tasks.named("compileJava24Java"))
}

// Make sure the jar task runs before testJava24 task
tasks.named("testJava24") {
    dependsOn(tasks.jar)
}

// Disable forbiddenapis for java24 source set
tasks.matching { it.name == "forbiddenApisJava24" }.configureEach {
    enabled = false
}
