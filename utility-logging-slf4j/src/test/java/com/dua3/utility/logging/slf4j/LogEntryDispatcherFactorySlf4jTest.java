package com.dua3.utility.logging.slf4j;

import com.dua3.utility.logging.LogDispatcher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Unit tests for the {@link LogDispatcherFactorySlf4j} class.
 */
@Execution(ExecutionMode.SAME_THREAD)
class LogDispatcherFactorySlf4jTest {

    @Test
    void testConstructor() {
        // Just create an instance to ensure the constructor doesn't throw an exception
        LogDispatcherFactorySlf4j factory = new LogDispatcherFactorySlf4j();
        assertNotNull(factory, "Factory should not be null");
    }

    @Test
    void testGetFactory() {
        // Get the factory
        LoggerFactorySlf4j factory = LogDispatcherFactorySlf4j.getFactory();

        // Verify that the factory is not null
        assertNotNull(factory, "Factory should not be null");

        // Verify that the factory is an instance of LoggerFactorySlf4j
        assertInstanceOf(LoggerFactorySlf4j.class, factory, "Factory should be an instance of LoggerFactorySlf4j");
    }

    @Test
    void testGetDispatcher() {
        // Create a factory
        LogDispatcherFactorySlf4j factory = new LogDispatcherFactorySlf4j();

        // Get the dispatcher
        LogDispatcher dispatcher = factory.getDispatcher();

        // Verify that the dispatcher is not null
        assertNotNull(dispatcher, "Dispatcher should not be null");

        // Verify that the dispatcher is an instance of LoggerFactorySlf4j
        assertInstanceOf(LoggerFactorySlf4j.class, dispatcher, "Dispatcher should be an instance of LoggerFactorySlf4j");
    }
}