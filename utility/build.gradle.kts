description = "Java utilities (core)"

// Define Java 24 source set
sourceSets {
    create("java24") {
        java {
            srcDir("src/main/java24")
        }
    }
}

// Add dependencies for java24 source set
dependencies {
    "java24Implementation"(sourceSets.main.get().output)
    // Add the same dependencies as the main source set
    configurations.implementation.get().allDependencies.forEach {
        "java24Implementation"(it)
    }
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

// Make sure java24 compilation happens after main compilation
tasks.named("compileJava24Java") {
    dependsOn(tasks.compileJava)
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

// Disable forbiddenapis for java24 source set
tasks.matching { it.name == "forbiddenApisJava24" }.configureEach {
    enabled = false
}
