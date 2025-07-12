package com.dua3.utility.logging;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the {@link LogEntryFilter} interface.
 */
class LogEntryFilterTest {

    @Test
    void testAllPassFilter() {
        // Get the ALL_PASS_FILTER
        LogEntryFilter filter = LogEntryFilter.ALL_PASS_FILTER;

        // Create a test log entry
        LogEntry entry = new SimpleLogEntry("Test message", "TestLogger", Instant.now(), LogLevel.INFO, "TEST_MARKER", null, null);

        // Test that the filter passes all entries
        assertTrue(filter.test(entry), "ALL_PASS_FILTER should pass all entries");
    }

    @Test
    void testAllPassMethod() {
        // Get the filter from the allPass method
        LogEntryFilter filter = LogEntryFilter.allPass();

        // Create a test log entry
        LogEntry entry = new SimpleLogEntry("Test message", "TestLogger", Instant.now(), LogLevel.INFO, "TEST_MARKER", null, null);

        // Test that the filter passes all entries
        assertTrue(filter.test(entry), "allPass() should return a filter that passes all entries");

        // Test that the filter is the same as ALL_PASS_FILTER
        assertSame(LogEntryFilter.ALL_PASS_FILTER, filter, "allPass() should return ALL_PASS_FILTER");
    }

    @Test
    void testCustomFilter() {
        // Create a custom filter that only passes entries with level INFO or higher
        LogEntryFilter filter = entry -> entry.level().ordinal() >= LogLevel.INFO.ordinal();

        // Create test log entries with different levels
        LogEntry traceEntry = new SimpleLogEntry("Trace message", "TestLogger", Instant.now(), LogLevel.TRACE, "TEST_MARKER", null, null);

        LogEntry debugEntry = new SimpleLogEntry("Debug message", "TestLogger", Instant.now(), LogLevel.DEBUG, "TEST_MARKER", null, null);

        LogEntry infoEntry = new SimpleLogEntry("Info message", "TestLogger", Instant.now(), LogLevel.INFO, "TEST_MARKER", null, null);

        LogEntry warnEntry = new SimpleLogEntry("Warn message", "TestLogger", Instant.now(), LogLevel.WARN, "TEST_MARKER", null, null);

        LogEntry errorEntry = new SimpleLogEntry("Error message", "TestLogger", Instant.now(), LogLevel.ERROR, "TEST_MARKER", null, null);

        // Test the filter
        assertFalse(filter.test(traceEntry), "Filter should not pass TRACE entries");
        assertFalse(filter.test(debugEntry), "Filter should not pass DEBUG entries");
        assertTrue(filter.test(infoEntry), "Filter should pass INFO entries");
        assertTrue(filter.test(warnEntry), "Filter should pass WARN entries");
        assertTrue(filter.test(errorEntry), "Filter should pass ERROR entries");
    }
}