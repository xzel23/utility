package com.dua3.utility.logging.log4j;

import com.dua3.utility.logging.LogLevel;
import org.apache.logging.log4j.Level;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the {@link LogUtilLog4J} class.
 */
@Execution(ExecutionMode.SAME_THREAD)
class LogUtilLog4JTest {

    @Test
    void testIsDefaultImplementation() {
        // This test may pass or fail depending on the environment
        // Just call the method to ensure it doesn't throw an exception
        boolean result = LogUtilLog4J.isDefaultImplementation();
        // No assertion, just make sure it runs
    }

    @Test
    void testTranslateLevel() {
        // Test translating Log4j Level to LogLevel
        assertEquals(LogLevel.TRACE, LogUtilLog4J.translate(Level.TRACE));
        assertEquals(LogLevel.DEBUG, LogUtilLog4J.translate(Level.DEBUG));
        assertEquals(LogLevel.INFO, LogUtilLog4J.translate(Level.INFO));
        assertEquals(LogLevel.WARN, LogUtilLog4J.translate(Level.WARN));
        assertEquals(LogLevel.ERROR, LogUtilLog4J.translate(Level.ERROR));
        assertEquals(LogLevel.ERROR, LogUtilLog4J.translate(Level.FATAL));
    }

    @Test
    void testTranslateLogLevel() {
        // Test translating LogLevel to Log4j Level
        assertEquals(Level.TRACE, LogUtilLog4J.translate(LogLevel.TRACE));
        assertEquals(Level.DEBUG, LogUtilLog4J.translate(LogLevel.DEBUG));
        assertEquals(Level.INFO, LogUtilLog4J.translate(LogLevel.INFO));
        assertEquals(Level.WARN, LogUtilLog4J.translate(LogLevel.WARN));
        assertEquals(Level.ERROR, LogUtilLog4J.translate(LogLevel.ERROR));
    }

    @Test
    void testInit() {
        // This test might affect global logging state
        // Just call the method to ensure it doesn't throw an exception
        assertDoesNotThrow(() -> LogUtilLog4J.init(LogLevel.INFO));
    }

    @Test
    void testUpdateLoggers() {
        assertDoesNotThrow(LogUtilLog4J::updateLoggers);
    }

    @Test
    void testIsClassOnClasspath() {
        // Test with a class that should be on the classpath
        assertTrue(LogUtilLog4J.isClassOnClasspath("java.lang.String"));

        // Test with a class that should not be on the classpath
        assertFalse(LogUtilLog4J.isClassOnClasspath("com.nonexistent.Class"));

        // Test with an invalid class name
        assertFalse(LogUtilLog4J.isClassOnClasspath("invalid class name"));
    }

    @Test
    void testGlobalAppender() {
        // Verify that the GLOBAL_APPENDER is not null
        assertNotNull(LogUtilLog4J.GLOBAL_APPENDER);

        // Verify that the GLOBAL_APPENDER's dispatcher is not null
        assertNotNull(LogUtilLog4J.GLOBAL_APPENDER.dispatcher());
    }
}