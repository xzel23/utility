package com.dua3.utility.logging;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the {@link LogEntryDispatcher} interface.
 * Since LogEntryDispatcher is an interface, we create a mock implementation for testing.
 */
class LogEntryDispatcherTest {

    private MockLogEntryDispatcher dispatcher;
    private LogEntry testEntry;

    @BeforeEach
    void setUp() {
        dispatcher = new MockLogEntryDispatcher();
        testEntry = new SimpleLogEntry("Test message", "TestLogger", Instant.now(), LogLevel.INFO, "TEST_MARKER", null, null);
    }

    @Test
    void testAddAndRemoveLogEntryHandler() {
        // Create a test handler
        AtomicInteger count = new AtomicInteger(0);
        LogEntryHandler handler = entry -> count.incrementAndGet();

        // Add the handler
        dispatcher.addLogEntryHandler(handler);

        // Test that the handler was added
        Collection<LogEntryHandler> handlers = dispatcher.getLogEntryHandlers();
        assertEquals(1, handlers.size(), "There should be 1 handler");
        assertTrue(handlers.contains(handler), "The handler should be in the collection");

        // Dispatch a log entry
        dispatcher.dispatch(testEntry);

        // Test that the handler was called
        assertEquals(1, count.get(), "The handler should have been called once");

        // Remove the handler
        dispatcher.removeLogEntryHandler(handler);

        // Test that the handler was removed
        handlers = dispatcher.getLogEntryHandlers();
        assertEquals(0, handlers.size(), "There should be 0 handlers");
        assertFalse(handlers.contains(handler), "The handler should not be in the collection");

        // Dispatch another log entry
        dispatcher.dispatch(testEntry);

        // Test that the handler was not called
        assertEquals(1, count.get(), "The handler should not have been called again");
    }

    @Test
    void testSetAndGetFilter() {
        // Test the default filter
        LogEntryFilter defaultFilter = dispatcher.getFilter();
        assertEquals(LogEntryFilter.allPass(), defaultFilter, "Default filter should be the all-pass filter");

        // Create a test handler
        AtomicInteger count = new AtomicInteger(0);
        LogEntryHandler handler = entry -> count.incrementAndGet();
        dispatcher.addLogEntryHandler(handler);

        // Dispatch a log entry with the default filter
        dispatcher.dispatch(testEntry);

        // Test that the handler was called
        assertEquals(1, count.get(), "The handler should have been called once");

        // Set a filter that blocks all entries
        LogEntryFilter blockAllFilter = entry -> false;
        dispatcher.setFilter(blockAllFilter);

        // Test that the filter was set
        assertEquals(blockAllFilter, dispatcher.getFilter(), "Filter should be the block-all filter");

        // Dispatch another log entry
        dispatcher.dispatch(testEntry);

        // Test that the handler was not called
        assertEquals(1, count.get(), "The handler should not have been called again");

        // Set a filter that only passes entries with level INFO or higher
        LogEntryFilter infoOrHigherFilter = entry -> entry.level().ordinal() >= LogLevel.INFO.ordinal();
        dispatcher.setFilter(infoOrHigherFilter);

        // Test that the filter was set
        assertEquals(infoOrHigherFilter, dispatcher.getFilter(), "Filter should be the info-or-higher filter");

        // Dispatch an INFO entry
        dispatcher.dispatch(testEntry);

        // Test that the handler was called
        assertEquals(2, count.get(), "The handler should have been called again");

        // Dispatch a TRACE entry
        LogEntry traceEntry = new SimpleLogEntry("Trace message", "TestLogger", Instant.now(), LogLevel.TRACE, "TEST_MARKER", null, null);
        dispatcher.dispatch(traceEntry);

        // Test that the handler was not called
        assertEquals(2, count.get(), "The handler should not have been called for a TRACE entry");
    }

    @Test
    void testGetLogEntryHandlers() {
        // Test with no handlers
        Collection<LogEntryHandler> handlers = dispatcher.getLogEntryHandlers();
        assertNotNull(handlers, "Handlers collection should not be null");
        assertTrue(handlers.isEmpty(), "Handlers collection should be empty");

        // Add some handlers
        LogEntryHandler handler1 = entry -> {};
        LogEntryHandler handler2 = entry -> {};
        dispatcher.addLogEntryHandler(handler1);
        dispatcher.addLogEntryHandler(handler2);

        // Test with handlers
        handlers = dispatcher.getLogEntryHandlers();
        assertEquals(2, handlers.size(), "There should be 2 handlers");
        assertTrue(handlers.contains(handler1), "The first handler should be in the collection");
        assertTrue(handlers.contains(handler2), "The second handler should be in the collection");

        // Test that the collection is a copy
        handlers.clear();
        Collection<LogEntryHandler> handlersAfterClear = dispatcher.getLogEntryHandlers();
        assertEquals(2, handlersAfterClear.size(), "There should still be 2 handlers");
    }

    /**
     * A mock implementation of LogEntryDispatcher for testing.
     */
    private static class MockLogEntryDispatcher implements LogEntryDispatcher {
        private final List<LogEntryHandler> handlers = new ArrayList<>();
        private LogEntryFilter filter = LogEntryFilter.allPass();

        @Override
        public void addLogEntryHandler(LogEntryHandler handler) {
            handlers.add(handler);
        }

        @Override
        public void removeLogEntryHandler(LogEntryHandler handler) {
            handlers.remove(handler);
        }

        /**
         * Dispatches a log entry to all registered handlers if it passes the filter.
         *
         * @param entry the log entry to dispatch
         */
        public void dispatch(LogEntry entry) {
            if (filter.test(entry)) {
                for (LogEntryHandler handler : handlers) {
                    handler.handleEntry(entry);
                }
            }
        }        @Override
        public void setFilter(LogEntryFilter filter) {
            this.filter = filter;
        }

        @Override
        public LogEntryFilter getFilter() {
            return filter;
        }

        @Override
        public Collection<LogEntryHandler> getLogEntryHandlers() {
            return new ArrayList<>(handlers);
        }


    }
}