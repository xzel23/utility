package com.dua3.utility.logging;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the {@link SimpleLogEntry} class and {@link LogEntry} interface.
 */
class SimpleLogEntryTest {

    @Test
    void testConstructorAndGetters() {
        // Create test data
        String message = "Test message";
        String loggerName = "TestLogger";
        Instant time = Instant.now();
        LogLevel level = LogLevel.INFO;
        String marker = "TEST_MARKER";
        Throwable throwable = new RuntimeException("Test exception");
        String location = "com.example.TestClass.testMethod(TestClass.java:123)";

        // Create a SimpleLogEntry
        SimpleLogEntry entry = new SimpleLogEntry(time, loggerName, level, marker, message, location, throwable);

        // Test getters
        assertEquals(message, entry.message(), "message should match");
        assertEquals(loggerName, entry.loggerName(), "loggerName should match");
        assertEquals(time, entry.time(), "time should match");
        assertEquals(level, entry.level(), "level should match");
        assertEquals(marker, entry.marker(), "marker should match");
        assertEquals(throwable, entry.throwable(), "throwable should match");
        assertEquals(location, entry.location(), "location should match");
    }

    @Test
    void testConstructorWithNullValues() {
        // Create a SimpleLogEntry with null values where allowed
        SimpleLogEntry entry = new SimpleLogEntry(Instant.now(), "TestLogger", LogLevel.INFO, "", "Test message",
                "", null // null throwable
        );

        // Test getters with null values
        assertNull(entry.throwable(), "throwable should be null");
        assertEquals("", entry.location(), "location should be empty string");
        assertEquals("", entry.marker(), "marker should be empty string");
    }

    @Test
    void testFormat() {
        // Create a SimpleLogEntry
        Instant time = Instant.now();
        SimpleLogEntry entry = new SimpleLogEntry(time, "TestLogger", LogLevel.INFO, "TEST_MARKER", "Test message", "com.example.TestClass.testMethod(TestClass.java:123)", null);

        // Test format with empty prefix and suffix
        String formatted = entry.format("", "");
        assertTrue(formatted.contains("[INFO]"), "Formatted text should contain the log level");
        assertTrue(formatted.contains(time.toString()), "Formatted text should contain the time");
        assertTrue(formatted.contains("TestLogger"), "Formatted text should contain the logger name");
        assertTrue(formatted.contains("Test message"), "Formatted text should contain the message");
        assertTrue(formatted.contains("com.example.TestClass.testMethod(TestClass.java:123)"), "Formatted text should contain the location");

        // Test format with custom prefix and suffix
        String customFormatted = entry.format("PREFIX_", "_SUFFIX");
        assertTrue(customFormatted.startsWith("PREFIX_"), "Formatted text should start with the prefix");
        assertTrue(customFormatted.endsWith("_SUFFIX"), "Formatted text should end with the suffix");
    }

    @Test
    void testFormatWithThrowable() {
        // Create a SimpleLogEntry with a throwable
        Throwable throwable = new RuntimeException("Test exception");
        SimpleLogEntry entry = new SimpleLogEntry(Instant.now(), "TestLogger", LogLevel.ERROR, "TEST_MARKER", "Test message", "com.example.TestClass.testMethod(TestClass.java:123)", throwable);

        // Test format
        String formatted = entry.format("", "");
        assertTrue(formatted.contains("Test message"), "Formatted text should contain the message");
        assertTrue(formatted.contains("Test exception"), "Formatted text should contain the exception message");
        assertTrue(formatted.contains("RuntimeException"), "Formatted text should contain the exception class");
    }
}
