description = "Java utilities (core)"

plugins {
    // other plugins...
    id("me.champeau.jmh") version "0.7.3"
}

// Add JMH configuration if you need to customize settings
jmh {
    // Common settings for JMH benchmarks
    warmupIterations = 2
    iterations = 5
    fork = 1
}
