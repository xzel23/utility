description = "Java utilities (core)"

plugins {
    alias(libs.plugins.jmh)
}

jmh {
    jmhVersion = rootProject.libs.versions.jmh
    warmupIterations = 2
    iterations = 5
    fork = 1
}
