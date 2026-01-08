package com.dua3.utility.logging;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Unit tests for the {@link LogDispatcherFactory} interface.
 * Since ILogDispatcherFactory is a functional interface, we create mock implementations for testing.
 */
class ILogDispatcherFactoryTest {

    @Test
    void testFactoryReturningDispatcher() {
        // Create a factory that returns a dispatcher
        MockLogDispatcher dispatcher = new MockLogDispatcher();
        LogDispatcherFactory factory = () -> dispatcher;

        // Test that the factory returns the expected dispatcher
        LogDispatcher returnedDispatcher = factory.getDispatcher();
        assertNotNull(returnedDispatcher, "Factory should return a non-null dispatcher");
        assertSame(dispatcher, returnedDispatcher, "Factory should return the expected dispatcher");
    }

    @Test
    void testFactoryReturningNull() {
        // Create a factory that returns null
        LogDispatcherFactory factory = () -> null;

        // Test that the factory returns null
        LogDispatcher returnedDispatcher = factory.getDispatcher();
        assertNull(returnedDispatcher, "Factory should return null");
    }

    @Test
    void testLambdaImplementation() {
        // Create a factory using a lambda expression
        LogDispatcherFactory factory = MockLogDispatcher::new;

        // Test that the factory returns a non-null dispatcher
        LogDispatcher returnedDispatcher = factory.getDispatcher();
        assertNotNull(returnedDispatcher, "Factory should return a non-null dispatcher");
        assertInstanceOf(MockLogDispatcher.class, returnedDispatcher, "Factory should return an instance of MockLogDispatcher");
    }

    /**
     * A mock implementation of LogDispatcher for testing.
     */
    private static class MockLogDispatcher implements LogDispatcher {
        @Override
        public void addLogHandler(LogHandler handler) {
            // Not needed for this test
        }

        @Override
        public void removeLogHandler(LogHandler handler) {
            // Not needed for this test
        }

        @Override
        public void setFilter(LogFilter filter) {
            // Not needed for this test
        }

        @Override
        public LogFilter getFilter() {
            return LogFilter.allPass();
        }

        @Override
        public java.util.Collection<LogHandler> getLogHandlers() {
            return java.util.Collections.emptyList();
        }
    }
}