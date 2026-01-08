package com.dua3.utility.logging;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.function.BiPredicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the {@link StandardLogFilter} class.
 */
class DefaultLogEntryFilterTest {

    private DefaultLogEntryFilter filter;
    private LogEntry traceEntry;
    private LogEntry debugEntry;
    private LogEntry infoEntry;
    private LogEntry warnEntry;
    private LogEntry errorEntry;

    @BeforeEach
    void setUp() {
        // Create a default filter
        filter = new DefaultLogEntryFilter();

        // Create test log entries with different levels
        traceEntry = new SimpleLogEntry(Instant.now(), "com.example.TestLogger", LogLevel.TRACE, "TEST_MARKER", "Trace message", "", null);

        debugEntry = new SimpleLogEntry(Instant.now(), "com.example.TestLogger", LogLevel.DEBUG, "TEST_MARKER", "Debug message", "", null);

        infoEntry = new SimpleLogEntry(Instant.now(), "com.example.TestLogger", LogLevel.INFO, "TEST_MARKER", "Info message", "", null);

        warnEntry = new SimpleLogEntry(Instant.now(), "com.example.TestLogger", LogLevel.WARN, "TEST_MARKER", "Warn message", "", null);

        errorEntry = new SimpleLogEntry(Instant.now(), "com.example.TestLogger", LogLevel.ERROR, "TEST_MARKER", "Error message", "", null);
    }

    @Test
    void testDefaultConstructor() {
        // Test that the default constructor sets the level to TRACE and uses filters that pass all entries
        assertEquals(LogLevel.TRACE, filter.getLevel(), "Default level should be TRACE");
        assertTrue(filter.test(traceEntry), "Default filter should pass TRACE entries");
        assertTrue(filter.test(debugEntry), "Default filter should pass DEBUG entries");
        assertTrue(filter.test(infoEntry), "Default filter should pass INFO entries");
        assertTrue(filter.test(warnEntry), "Default filter should pass WARN entries");
        assertTrue(filter.test(errorEntry), "Default filter should pass ERROR entries");
    }

    @Test
    void testParameterizedConstructor() {
        // Create a filter with custom level and filters
        BiPredicate<String, LogLevel> loggerNameFilter = (name, level) -> name.startsWith("com.example");
        BiPredicate<String, LogLevel> textFilter = (text, level) -> text.contains("message");
        DefaultLogEntryFilter customFilter = new DefaultLogEntryFilter(LogLevel.INFO, loggerNameFilter, textFilter);

        // Test the level and filters
        assertEquals(LogLevel.INFO, customFilter.getLevel(), "Level should be INFO");
        assertEquals(loggerNameFilter, customFilter.getFilterLoggerName(), "Logger name filter should match");
        assertEquals(textFilter, customFilter.getFilterText(), "Text filter should match");

        // Test filtering by level
        assertFalse(customFilter.test(traceEntry), "Filter should not pass TRACE entries");
        assertFalse(customFilter.test(debugEntry), "Filter should not pass DEBUG entries");
        assertTrue(customFilter.test(infoEntry), "Filter should pass INFO entries");
        assertTrue(customFilter.test(warnEntry), "Filter should pass WARN entries");
        assertTrue(customFilter.test(errorEntry), "Filter should pass ERROR entries");

        // Test filtering by logger name
        LogEntry entryWithDifferentLogger = new SimpleLogEntry(Instant.now(), "org.other.Logger", LogLevel.INFO, "TEST_MARKER", "Info message", "", null);
        assertFalse(customFilter.test(entryWithDifferentLogger), "Filter should not pass entries with non-matching logger name");

        // Test filtering by message text
        LogEntry entryWithDifferentText = new SimpleLogEntry(Instant.now(), "com.example.TestLogger", LogLevel.INFO, "TEST_MARKER", "Different text", "", null);
        assertFalse(customFilter.test(entryWithDifferentText), "Filter should not pass entries with non-matching message text");
    }

    @Test
    void testSetAndGetLevel() {
        // Test the default level
        assertEquals(LogLevel.TRACE, filter.getLevel(), "Default level should be TRACE");

        // Set a new level
        filter.setLevel(LogLevel.INFO);

        // Test the new level
        assertEquals(LogLevel.INFO, filter.getLevel(), "Level should be INFO");

        // Test filtering by the new level
        assertFalse(filter.test(traceEntry), "Filter should not pass TRACE entries");
        assertFalse(filter.test(debugEntry), "Filter should not pass DEBUG entries");
        assertTrue(filter.test(infoEntry), "Filter should pass INFO entries");
        assertTrue(filter.test(warnEntry), "Filter should pass WARN entries");
        assertTrue(filter.test(errorEntry), "Filter should pass ERROR entries");
    }

    @Test
    void testSetAndGetFilterLoggerName() {
        // Test the default logger name filter
        BiPredicate<String, LogLevel> defaultFilter = filter.getFilterLoggerName();
        assertTrue(defaultFilter.test("any.logger.name", LogLevel.INFO), "Default logger name filter should pass all entries");

        // Set a new logger name filter
        BiPredicate<String, LogLevel> newFilter = (name, level) -> name.startsWith("com.example");
        filter.setFilterLoggerName(newFilter);

        // Test the new logger name filter
        assertEquals(newFilter, filter.getFilterLoggerName(), "Logger name filter should match");

        // Test filtering by the new logger name filter
        assertTrue(filter.test(infoEntry), "Filter should pass entries with matching logger name");

        LogEntry entryWithDifferentLogger = new SimpleLogEntry(Instant.now(), "org.other.Logger", LogLevel.INFO, "TEST_MARKER", "Info message", "", null);
        assertFalse(filter.test(entryWithDifferentLogger), "Filter should not pass entries with non-matching logger name");
    }

    @Test
    void testSetAndGetFilterText() {
        // Test the default text filter
        BiPredicate<String, LogLevel> defaultFilter = filter.getFilterText();
        assertTrue(defaultFilter.test("any message", LogLevel.INFO), "Default text filter should pass all entries");

        // Set a new text filter
        BiPredicate<String, LogLevel> newFilter = (text, level) -> text.contains("message");
        filter.setFilterText(newFilter);

        // Test the new text filter
        assertEquals(newFilter, filter.getFilterText(), "Text filter should match");

        // Test filtering by the new text filter
        assertTrue(filter.test(infoEntry), "Filter should pass entries with matching message text");

        LogEntry entryWithDifferentText = new SimpleLogEntry(Instant.now(), "com.example.TestLogger", LogLevel.INFO, "TEST_MARKER", "Different text", "", null);
        assertFalse(filter.test(entryWithDifferentText), "Filter should not pass entries with non-matching message text");
    }

    @Test
    void testCopy() {
        // Set custom values
        filter.setLevel(LogLevel.INFO);
        BiPredicate<String, LogLevel> loggerNameFilter = (name, level) -> name.startsWith("com.example");
        BiPredicate<String, LogLevel> textFilter = (text, level) -> text.contains("message");
        filter.setFilterLoggerName(loggerNameFilter);
        filter.setFilterText(textFilter);

        // Create a copy
        DefaultLogEntryFilter copy = filter.copy();

        // Test that the copy has the same values
        assertEquals(filter.getLevel(), copy.getLevel(), "Copy should have the same level");
        assertEquals(filter.getFilterLoggerName(), copy.getFilterLoggerName(), "Copy should have the same logger name filter");
        assertEquals(filter.getFilterText(), copy.getFilterText(), "Copy should have the same text filter");

        // Test that the copy is a different instance
        assertNotSame(filter, copy, "Copy should be a different instance");
    }

    @Test
    void testWithLevel() {
        // Create a filter with a different level
        DefaultLogEntryFilter newFilter = filter.withLevel(LogLevel.INFO);

        // Test that the new filter has the expected level
        assertEquals(LogLevel.INFO, newFilter.getLevel(), "New filter should have the specified level");

        // Test that the original filter is unchanged
        assertEquals(LogLevel.TRACE, filter.getLevel(), "Original filter should be unchanged");

        // Test that the new filter has the same filters
        assertEquals(filter.getFilterLoggerName(), newFilter.getFilterLoggerName(), "New filter should have the same logger name filter");
        assertEquals(filter.getFilterText(), newFilter.getFilterText(), "New filter should have the same text filter");
    }

    @Test
    void testWithFilterLoggerName() {
        // Create a filter with a different logger name filter
        BiPredicate<String, LogLevel> newLoggerNameFilter = (name, level) -> name.startsWith("com.example");
        DefaultLogEntryFilter newFilter = filter.withFilterLoggerName(newLoggerNameFilter);

        // Test that the new filter has the expected logger name filter
        assertEquals(newLoggerNameFilter, newFilter.getFilterLoggerName(), "New filter should have the specified logger name filter");

        // Test that the original filter is unchanged
        assertNotEquals(newLoggerNameFilter, filter.getFilterLoggerName(), "Original filter should be unchanged");

        // Test that the new filter has the same level and text filter
        assertEquals(filter.getLevel(), newFilter.getLevel(), "New filter should have the same level");
        assertEquals(filter.getFilterText(), newFilter.getFilterText(), "New filter should have the same text filter");
    }

    @Test
    void testWithFilterText() {
        // Create a filter with a different text filter
        BiPredicate<String, LogLevel> newTextFilter = (text, level) -> text.contains("message");
        DefaultLogEntryFilter newFilter = filter.withFilterText(newTextFilter);

        // Test that the new filter has the expected text filter
        assertEquals(newTextFilter, newFilter.getFilterText(), "New filter should have the specified text filter");

        // Test that the original filter is unchanged
        assertNotEquals(newTextFilter, filter.getFilterText(), "Original filter should be unchanged");

        // Test that the new filter has the same level and logger name filter
        assertEquals(filter.getLevel(), newFilter.getLevel(), "New filter should have the same level");
        assertEquals(filter.getFilterLoggerName(), newFilter.getFilterLoggerName(), "New filter should have the same logger name filter");
    }

    @Test
    void testKnownLoggersCaching() {
        // Create a counter to track how many times the logger name filter is called
        final int[] count = {0};
        BiPredicate<String, LogLevel> countingFilter = (name, level) -> {
            count[0]++;
            return true;
        };

        // Set the counting filter
        filter.setFilterLoggerName(countingFilter);

        // Test the filter multiple times with the same logger name
        for (int i = 0; i < 5; i++) {
            assertTrue(filter.test(infoEntry), "Filter should pass the entry");
        }

        // The filter should only be called once for the same logger name
        assertEquals(1, count[0], "Logger name filter should only be called once for the same logger name");

        // Test with a different logger name
        LogEntry entryWithDifferentLogger = new SimpleLogEntry(Instant.now(), "org.other.Logger", LogLevel.INFO, "TEST_MARKER", "Info message", "", null);
        assertTrue(filter.test(entryWithDifferentLogger), "Filter should pass the entry");

        // The filter should be called again for a different logger name
        assertEquals(2, count[0], "Logger name filter should be called again for a different logger name");

        // Change the filter
        filter.setFilterLoggerName((name, level) -> true);

        // The cache should be cleared
        count[0] = 0;
        filter.setFilterLoggerName(countingFilter);
        assertTrue(filter.test(infoEntry), "Filter should pass the entry");
        assertEquals(1, count[0], "Logger name filter should be called again after changing the filter");
    }
}