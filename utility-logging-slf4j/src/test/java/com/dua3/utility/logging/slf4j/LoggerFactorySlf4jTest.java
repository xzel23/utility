package com.dua3.utility.logging.slf4j;

import com.dua3.utility.logging.LogEntryFilter;
import com.dua3.utility.logging.LogEntryHandler;
import com.dua3.utility.logging.LogHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.slf4j.Logger;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the {@link LoggerFactorySlf4j} class.
 */
@Execution(ExecutionMode.SAME_THREAD)
class LoggerFactorySlf4jTest {

    @Test
    void testConstructor() {
        // Just create an instance to ensure the constructor doesn't throw an exception
        LoggerFactorySlf4j factory = new LoggerFactorySlf4j();
        assertNotNull(factory, "Factory should not be null");
    }

    @Test
    void testGetLogger() {
        LoggerFactorySlf4j factory = new LoggerFactorySlf4j();
        Logger logger = factory.getLogger("testLogger");

        assertNotNull(logger, "Logger should not be null");
        assertInstanceOf(LoggerSlf4j.class, logger, "Logger should be an instance of LoggerSlf4j");
        assertEquals("testLogger", logger.getName(), "Logger name should match");
    }

    @Test
    void testAddRemoveLogHandler() {
        LoggerFactorySlf4j factory = new LoggerFactorySlf4j();

        // Create a mock handler
        LogEntryHandler handler = entry -> {};

        // Add the handler
        factory.addLogHandler(handler);

        // Verify that the handler was added
        Collection<LogHandler> handlers = factory.getLogHandlers();
        assertTrue(handlers.contains(handler), "Handler should be in the list");

        // Remove the handler
        factory.removeLogHandler(handler);

        // Verify that the handler was removed
        handlers = factory.getLogHandlers();
        assertFalse(handlers.contains(handler), "Handler should not be in the list");
    }

    @Test
    void testSetGetFilter() {
        LoggerFactorySlf4j factory = new LoggerFactorySlf4j();

        // Create a filter that always returns true
        LogEntryFilter trueFilter = entry -> true;

        // Set the filter
        factory.setFilter(trueFilter);

        // Verify that the filter was set
        assertSame(trueFilter, factory.getFilter(), "Filter should be the same");
    }

    @Test
    void testGetDefaultHandler() {
        LoggerFactorySlf4j factory = new LoggerFactorySlf4j();

        // The default handler might be null or not, depending on the configuration
        // Just call the method to ensure it doesn't throw an exception
        assertDoesNotThrow(factory::getDefaultHandler);
    }
}