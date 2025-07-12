package com.dua3.utility.logging.slf4j;

import com.dua3.utility.logging.LogEntryDispatcher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Unit tests for the {@link LogEntryDispatcherFactorySlf4j} class.
 */
@Execution(ExecutionMode.SAME_THREAD)
class LogEntryDispatcherFactorySlf4jTest {

    @Test
    void testConstructor() {
        // Just create an instance to ensure the constructor doesn't throw an exception
        LogEntryDispatcherFactorySlf4j factory = new LogEntryDispatcherFactorySlf4j();
        assertNotNull(factory, "Factory should not be null");
    }

    @Test
    void testGetFactory() {
        // Get the factory
        LoggerFactorySlf4j factory = LogEntryDispatcherFactorySlf4j.getFactory();

        // Verify that the factory is not null
        assertNotNull(factory, "Factory should not be null");

        // Verify that the factory is an instance of LoggerFactorySlf4j
        assertInstanceOf(LoggerFactorySlf4j.class, factory, "Factory should be an instance of LoggerFactorySlf4j");
    }

    @Test
    void testGetDispatcher() {
        // Create a factory
        LogEntryDispatcherFactorySlf4j factory = new LogEntryDispatcherFactorySlf4j();

        // Get the dispatcher
        LogEntryDispatcher dispatcher = factory.getDispatcher();

        // Verify that the dispatcher is not null
        assertNotNull(dispatcher, "Dispatcher should not be null");

        // Verify that the dispatcher is an instance of LoggerFactorySlf4j
        assertInstanceOf(LoggerFactorySlf4j.class, dispatcher, "Dispatcher should be an instance of LoggerFactorySlf4j");
    }
}