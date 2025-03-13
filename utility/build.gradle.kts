description = "Java utilities (core)"

plugins {
    alias(libs.plugins.jmh)
}

jmh {
    // Common settings for JMH benchmarks
    warmupIterations = 2
    iterations = 5
    fork = 1
}
