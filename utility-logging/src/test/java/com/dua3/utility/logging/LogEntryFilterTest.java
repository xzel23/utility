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
        LogEntry entry = new SimpleLogEntry(Instant.now(), "TestLogger", LogLevel.INFO, "TEST_MARKER", "Test message", "", null);

        // Test that the filter passes all entries
        assertTrue(filter.test(entry), "ALL_PASS_FILTER should pass all entries");
    }

    @Test
    void testAllPassMethod() {
        // Get the filter from the allPass method
        LogEntryFilter filter = LogEntryFilter.allPass();

        // Create a test log entry
        LogEntry entry = new SimpleLogEntry(Instant.now(), "TestLogger", LogLevel.INFO, "TEST_MARKER", "Test message", "", null);

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
        LogEntry traceEntry = new SimpleLogEntry(Instant.now(), "TestLogger", LogLevel.TRACE, "TEST_MARKER", "Trace message", "", null);

        LogEntry debugEntry = new SimpleLogEntry(Instant.now(), "TestLogger", LogLevel.DEBUG, "TEST_MARKER", "Debug message", "", null);

        LogEntry infoEntry = new SimpleLogEntry(Instant.now(), "TestLogger", LogLevel.INFO, "TEST_MARKER", "Info message", "", null);

        LogEntry warnEntry = new SimpleLogEntry(Instant.now(), "TestLogger", LogLevel.WARN, "TEST_MARKER", "Warn message", "", null);

        LogEntry errorEntry = new SimpleLogEntry(Instant.now(), "TestLogger", LogLevel.ERROR, "TEST_MARKER", "Error message", "", null);

        // Test the filter
        assertFalse(filter.test(traceEntry), "Filter should not pass TRACE entries");
        assertFalse(filter.test(debugEntry), "Filter should not pass DEBUG entries");
        assertTrue(filter.test(infoEntry), "Filter should pass INFO entries");
        assertTrue(filter.test(warnEntry), "Filter should pass WARN entries");
        assertTrue(filter.test(errorEntry), "Filter should pass ERROR entries");
    }
}