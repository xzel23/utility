package com.dua3.utility.logging.slf4j;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.slf4j.ILoggerFactory;
import org.slf4j.IMarkerFactory;
import org.slf4j.helpers.BasicMarkerFactory;
import org.slf4j.helpers.NOPMDCAdapter;
import org.slf4j.spi.MDCAdapter;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Unit tests for the {@link LoggingServiceProviderSlf4j} class.
 */
@Execution(ExecutionMode.SAME_THREAD)
class LoggingServiceProviderSlf4jTest {

    @Test
    void testConstructor() {
        // Just create an instance to ensure the constructor doesn't throw an exception
        LoggingServiceProviderSlf4j provider = new LoggingServiceProviderSlf4j();
        assertNotNull(provider, "Provider should not be null");
    }

    @Test
    void testGetLoggerFactory() {
        LoggingServiceProviderSlf4j provider = new LoggingServiceProviderSlf4j();

        // Get the logger factory
        ILoggerFactory factory = provider.getLoggerFactory();

        // Verify that the factory is not null
        assertNotNull(factory, "Logger factory should not be null");

        // Verify that the factory is an instance of LoggerFactorySlf4j
        assertInstanceOf(LoggerFactorySlf4j.class, factory, "Logger factory should be an instance of LoggerFactorySlf4j");
    }

    @Test
    void testGetMarkerFactory() {
        LoggingServiceProviderSlf4j provider = new LoggingServiceProviderSlf4j();

        // Get the marker factory
        IMarkerFactory factory = provider.getMarkerFactory();

        // Verify that the factory is not null
        assertNotNull(factory, "Marker factory should not be null");

        // Verify that the factory is an instance of BasicMarkerFactory
        assertInstanceOf(BasicMarkerFactory.class, factory, "Marker factory should be an instance of BasicMarkerFactory");
    }

    @Test
    void testGetMDCAdapter() {
        LoggingServiceProviderSlf4j provider = new LoggingServiceProviderSlf4j();

        // Get the MDC adapter
        MDCAdapter adapter = provider.getMDCAdapter();

        // Verify that the adapter is not null
        assertNotNull(adapter, "MDC adapter should not be null");

        // Verify that the adapter is an instance of NOPMDCAdapter
        assertInstanceOf(NOPMDCAdapter.class, adapter, "MDC adapter should be an instance of NOPMDCAdapter");
    }

    @Test
    void testGetRequestedApiVersion() {
        LoggingServiceProviderSlf4j provider = new LoggingServiceProviderSlf4j();

        // Get the requested API version
        String version = provider.getRequestedApiVersion();

        // Verify that the version is not null or empty
        assertNotNull(version, "Requested API version should not be null");
        assertFalse(version.isEmpty(), "Requested API version should not be empty");
    }

    @Test
    void testInitialize() {
        LoggingServiceProviderSlf4j provider = new LoggingServiceProviderSlf4j();

        // Just call the method to ensure it doesn't throw an exception
        assertDoesNotThrow(provider::initialize, "Initialize should not throw an exception");
    }
}