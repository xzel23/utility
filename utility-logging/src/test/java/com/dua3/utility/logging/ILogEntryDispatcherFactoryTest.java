package com.dua3.utility.logging;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Unit tests for the {@link ILogEntryDispatcherFactory} interface.
 * Since ILogEntryDispatcherFactory is a functional interface, we create mock implementations for testing.
 */
class ILogEntryDispatcherFactoryTest {

    @Test
    void testFactoryReturningDispatcher() {
        // Create a factory that returns a dispatcher
        MockLogEntryDispatcher dispatcher = new MockLogEntryDispatcher();
        ILogEntryDispatcherFactory factory = () -> dispatcher;

        // Test that the factory returns the expected dispatcher
        LogEntryDispatcher returnedDispatcher = factory.getDispatcher();
        assertNotNull(returnedDispatcher, "Factory should return a non-null dispatcher");
        assertSame(dispatcher, returnedDispatcher, "Factory should return the expected dispatcher");
    }

    @Test
    void testFactoryReturningNull() {
        // Create a factory that returns null
        ILogEntryDispatcherFactory factory = () -> null;

        // Test that the factory returns null
        LogEntryDispatcher returnedDispatcher = factory.getDispatcher();
        assertNull(returnedDispatcher, "Factory should return null");
    }

    @Test
    void testLambdaImplementation() {
        // Create a factory using a lambda expression
        ILogEntryDispatcherFactory factory = MockLogEntryDispatcher::new;

        // Test that the factory returns a non-null dispatcher
        LogEntryDispatcher returnedDispatcher = factory.getDispatcher();
        assertNotNull(returnedDispatcher, "Factory should return a non-null dispatcher");
        assertInstanceOf(MockLogEntryDispatcher.class, returnedDispatcher, "Factory should return an instance of MockLogEntryDispatcher");
    }

    /**
     * A mock implementation of LogEntryDispatcher for testing.
     */
    private static class MockLogEntryDispatcher implements LogEntryDispatcher {
        @Override
        public void addLogEntryHandler(LogEntryHandler handler) {
            // Not needed for this test
        }

        @Override
        public void removeLogEntryHandler(LogEntryHandler handler) {
            // Not needed for this test
        }

        @Override
        public void setFilter(LogEntryFilter filter) {
            // Not needed for this test
        }

        @Override
        public LogEntryFilter getFilter() {
            return LogEntryFilter.allPass();
        }

        @Override
        public java.util.Collection<LogEntryHandler> getLogEntryHandlers() {
            return java.util.Collections.emptyList();
        }
    }
}