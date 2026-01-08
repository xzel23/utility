package com.dua3.utility.logging;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the {@link LogDispatcher} interface.
 * Since LogDispatcher is an interface, we create a mock implementation for testing.
 */
class LogDispatcherTest {

    private MockLogDispatcher dispatcher;
    private LogEntry testEntry;

    @BeforeEach
    void setUp() {
        dispatcher = new MockLogDispatcher();
        testEntry = new SimpleLogEntry(Instant.now(), "TestLogger", LogLevel.INFO, "TEST_MARKER", "Test message", "", null);
    }

    @Test
    void testAddAndRemoveLogHandler() {
        // Create a test handler
        AtomicInteger count = new AtomicInteger(0);
        LogHandler handler = new LogHandler() {
            @Override
            public String name() {
                return "test";
            }

            @Override
            public void handle(Instant instant, String loggerName, LogLevel lvl, String mrk, Supplier<String> msg, String location, Throwable t) {
                count.incrementAndGet();
            }

            @Override
            public void setFilter(LogFilter filter) {
            }

            @Override
            public LogFilter getFilter() {
                return LogFilter.allPass();
            }
        };

        // Add the handler
        dispatcher.addLogHandler(handler);

        // Test that the handler was added
        Collection<LogHandler> handlers = dispatcher.getLogHandlers();
        assertEquals(1, handlers.size(), "There should be 1 handler");
        assertTrue(handlers.contains(handler), "The handler should be in the collection");

        // Dispatch a log entry
        dispatcher.dispatch(testEntry);

        // Test that the handler was called
        assertEquals(1, count.get(), "The handler should have been called once");

        // Remove the handler
        dispatcher.removeLogHandler(handler);

        // Test that the handler was removed
        handlers = dispatcher.getLogHandlers();
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
        LogFilter defaultFilter = dispatcher.getFilter();
        assertEquals(LogFilter.allPass(), defaultFilter, "Default filter should be the all-pass filter");

        // Create a test handler
        AtomicInteger count = new AtomicInteger(0);
        LogHandler handler = new LogHandler() {
            @Override
            public String name() {
                return "test";
            }

            @Override
            public void handle(Instant instant, String loggerName, LogLevel lvl, String mrk, Supplier<String> msg, String location, Throwable t) {
                count.incrementAndGet();
            }

            @Override
            public void setFilter(LogFilter filter) {
            }

            @Override
            public LogFilter getFilter() {
                return LogFilter.allPass();
            }
        };
        dispatcher.addLogHandler(handler);

        // Dispatch a log entry with the default filter
        dispatcher.dispatch(testEntry);

        // Test that the handler was called
        assertEquals(1, count.get(), "The handler should have been called once");

        // Set a filter that blocks all entries
        LogFilter blockAllFilter = new LogFilter() {
            @Override
            public String name() {
                return "block all";
            }

            @Override
            public boolean test(Instant instant, String loggerName, LogLevel lvl, String mrk, Supplier<String> msg, String location, Throwable t) {
                return false;
            }
        };
        dispatcher.setFilter(blockAllFilter);

        // Test that the filter was set
        assertEquals(blockAllFilter, dispatcher.getFilter(), "Filter should be the block-all filter");

        // Dispatch another log entry
        dispatcher.dispatch(testEntry);

        // Test that the handler was not called
        assertEquals(1, count.get(), "The handler should not have been called again");

        // Set a filter that only passes entries with level INFO or higher
        LogFilter infoOrHigherFilter = new LogFilter() {
            @Override
            public String name() {
                return "info or higher";
            }

            @Override
            public boolean test(Instant instant, String loggerName, LogLevel lvl, String mrk, Supplier<String> msg, String location, Throwable t) {
                return lvl.ordinal() >= LogLevel.INFO.ordinal();
            }
        };
        dispatcher.setFilter(infoOrHigherFilter);

        // Test that the filter was set
        assertEquals(infoOrHigherFilter, dispatcher.getFilter(), "Filter should be the info-or-higher filter");

        // Dispatch an INFO entry
        dispatcher.dispatch(testEntry);

        // Test that the handler was called
        assertEquals(2, count.get(), "The handler should have been called again");

        // Dispatch a TRACE entry
        LogEntry traceEntry = new SimpleLogEntry(Instant.now(), "TestLogger", LogLevel.TRACE, "TEST_MARKER", "Trace message", "", null);
        dispatcher.dispatch(traceEntry);

        // Test that the handler was not called
        assertEquals(2, count.get(), "The handler should not have been called for a TRACE entry");
    }

    @Test
    void testGetLogHandlers() {
        // Test with no handlers
        Collection<LogHandler> handlers = dispatcher.getLogHandlers();
        assertNotNull(handlers, "Handlers collection should not be null");
        assertTrue(handlers.isEmpty(), "Handlers collection should be empty");

        // Add some handlers
        LogHandler handler1 = new LogHandler() {
            @Override
            public String name() {
                return "test1";
            }

            @Override
            public void handle(Instant instant, String loggerName, LogLevel lvl, String mrk, Supplier<String> msg, String location, Throwable t) {
            }

            @Override
            public void setFilter(LogFilter filter) {
            }

            @Override
            public LogFilter getFilter() {
                return LogFilter.allPass();
            }
        };
        LogHandler handler2 = new LogHandler() {
            @Override
            public String name() {
                return "test2";
            }

            @Override
            public void handle(Instant instant, String loggerName, LogLevel lvl, String mrk, Supplier<String> msg, String location, Throwable t) {
            }

            @Override
            public void setFilter(LogFilter filter) {
            }

            @Override
            public LogFilter getFilter() {
                return LogFilter.allPass();
            }
        };
        dispatcher.addLogHandler(handler1);
        dispatcher.addLogHandler(handler2);

        // Test with handlers
        handlers = dispatcher.getLogHandlers();
        assertEquals(2, handlers.size(), "There should be 2 handlers");
        assertTrue(handlers.contains(handler1), "The first handler should be in the collection");
        assertTrue(handlers.contains(handler2), "The second handler should be in the collection");

        // Test that the collection is a copy
        handlers.clear();
        Collection<LogHandler> handlersAfterClear = dispatcher.getLogHandlers();
        assertEquals(2, handlersAfterClear.size(), "There should still be 2 handlers");
    }

    /**
     * A mock implementation of LogDispatcher for testing.
     */
    private static class MockLogDispatcher implements LogDispatcher {
        private final List<LogHandler> handlers = new ArrayList<>();
        private LogFilter filter = LogFilter.allPass();

        @Override
        public void addLogHandler(LogHandler handler) {
            handlers.add(handler);
        }

        @Override
        public void removeLogHandler(LogHandler handler) {
            handlers.remove(handler);
        }

        /**
         * Dispatches a log entry to all registered handlers if it passes the filter.
         *
         * @param entry the log entry to dispatch
         */
        public void dispatch(LogEntry entry) {
            if (filter.test(entry.time(), entry.loggerName(), entry.level(), entry.marker(), entry::message, entry.location(), entry.throwable())) {
                for (LogHandler handler : handlers) {
                    handler.handle(entry.time(), entry.loggerName(), entry.level(), entry.marker(), entry::message, entry.location(), entry.throwable());
                }
            }
        }

        @Override
        public void setFilter(LogFilter filter) {
            this.filter = filter;
        }

        @Override
        public LogFilter getFilter() {
            return filter;
        }

        @Override
        public Collection<LogHandler> getLogHandlers() {
            return new ArrayList<>(handlers);
        }
    }
}