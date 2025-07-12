package com.dua3.utility.logging.slf4j;

import com.dua3.utility.logging.LogLevel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.slf4j.Marker;
import org.slf4j.event.Level;
import org.slf4j.helpers.BasicMarkerFactory;

import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Unit tests for the {@link LogEntrySlf4j} class.
 */
@Execution(ExecutionMode.SAME_THREAD)
class LogEntrySlf4jTest {

    @Test
    void testConstructorAndGetters() {
        // Create test data
        String loggerName = "testLogger";
        Level level = Level.INFO;
        String message = "Test message";
        Marker marker = new BasicMarkerFactory().getMarker("TEST");
        Throwable throwable = new RuntimeException("Test exception");
        Supplier<String> messageFormatter = () -> message;

        // Create a LogEntrySlf4j
        LogEntrySlf4j entry = new LogEntrySlf4j(loggerName, level, marker, messageFormatter, throwable);

        // Test getters
        assertEquals(loggerName, entry.loggerName(), "Logger name should match");
        assertEquals(LogLevel.INFO, entry.level(), "Level should be INFO");
        assertEquals(message, entry.message(), "Message should match");
        assertEquals("TEST", entry.marker(), "Marker should match");
        assertSame(throwable, entry.throwable(), "Throwable should be the same");
        assertNotNull(entry.time(), "Time should not be null");
        assertNull(entry.location(), "Location should be null");

        // Test toString
        assertNotNull(entry.toString(), "toString should not return null");
    }

    @Test
    void testMessageFormatterCalledOnce() {
        // Create a counter to track how many times the message formatter is called
        int[] counter = new int[1];
        Supplier<String> messageFormatter = () -> {
            counter[0]++;
            return "Test message";
        };

        // Create a LogEntrySlf4j
        LogEntrySlf4j entry = new LogEntrySlf4j("testLogger", Level.INFO, null, messageFormatter, null);

        // Call message() multiple times
        String message1 = entry.message();
        String message2 = entry.message();
        String message3 = entry.message();

        // Verify that the message formatter was called exactly once
        assertEquals(1, counter[0], "Message formatter should be called exactly once");

        // Verify that all calls to message() return the same value
        assertEquals("Test message", message1, "First call to message() should return the formatted message");
        assertEquals("Test message", message2, "Second call to message() should return the cached message");
        assertEquals("Test message", message3, "Third call to message() should return the cached message");
    }

    @Test
    void testNullMarker() {
        // Create a LogEntrySlf4j with null marker
        LogEntrySlf4j entry = new LogEntrySlf4j("testLogger", Level.INFO, null, () -> "Test message", null);

        // Verify that marker() returns an empty string
        assertEquals("", entry.marker(), "Marker should be an empty string when null is provided");
    }

    @Test
    void testNullThrowable() {
        // Create a LogEntrySlf4j with null throwable
        LogEntrySlf4j entry = new LogEntrySlf4j("testLogger", Level.INFO, null, () -> "Test message", null);

        // Verify that throwable() returns null
        assertNull(entry.throwable(), "Throwable should be null when null is provided");
    }

    @Test
    void testTranslateLevel() {
        // Test translating all SLF4J levels to LogLevel
        assertEquals(LogLevel.TRACE, createEntryWithLevel(Level.TRACE).level(), "TRACE should translate to TRACE");
        assertEquals(LogLevel.DEBUG, createEntryWithLevel(Level.DEBUG).level(), "DEBUG should translate to DEBUG");
        assertEquals(LogLevel.INFO, createEntryWithLevel(Level.INFO).level(), "INFO should translate to INFO");
        assertEquals(LogLevel.WARN, createEntryWithLevel(Level.WARN).level(), "WARN should translate to WARN");
        assertEquals(LogLevel.ERROR, createEntryWithLevel(Level.ERROR).level(), "ERROR should translate to ERROR");
    }

    private LogEntrySlf4j createEntryWithLevel(Level level) {
        return new LogEntrySlf4j("testLogger", level, null, () -> "Test message", null);
    }
}