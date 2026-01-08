package com.dua3.utility.logging.slf4j;

import com.dua3.utility.logging.LogEntryHandler;
import com.dua3.utility.logging.LogHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.slf4j.Marker;
import org.slf4j.event.Level;
import org.slf4j.helpers.BasicMarkerFactory;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the {@link LoggerSlf4j} class.
 */
@Execution(ExecutionMode.SAME_THREAD)
class LoggerSlf4jTest {

    private LoggerSlf4j logger;
    private List<WeakReference<LogHandler>> handlers;
    private LogHandler handler;
    private AtomicBoolean handlerCalled;

    @BeforeEach
    void setUp() {
        // Reset the default level to INFO for each test
        LoggerSlf4j.setDefaultLevel(Level.INFO);

        handlers = new ArrayList<>();
        handlerCalled = new AtomicBoolean(false);
        handler = (LogEntryHandler) entry -> handlerCalled.set(true);
        handlers.add(new WeakReference<>(handler));

        logger = new LoggerSlf4j("testLogger", handlers);
    }

    @Test
    void testConstructor() {
        assertEquals("testLogger", logger.getName(), "Logger name should match");
    }

    @Test
    void testGetSetDefaultLevel() {
        // Test the default level
        assertEquals(Level.INFO, LoggerSlf4j.getDefaultLevel(), "Default level should be INFO");

        // Change the default level
        LoggerSlf4j.setDefaultLevel(Level.DEBUG);
        assertEquals(Level.DEBUG, LoggerSlf4j.getDefaultLevel(), "Default level should be DEBUG");
    }

    @Test
    void testGetSetLevel() {
        // Test the default level
        assertEquals(Level.INFO, logger.getLevel(), "Default level should be INFO");

        // Set a specific level
        logger.setLevel(Level.DEBUG);
        assertEquals(Level.DEBUG, logger.getLevel(), "Level should be DEBUG");
    }

    @Test
    void testIsEnabled() {
        // Set level to INFO
        logger.setLevel(Level.INFO);

        // Test various levels
        assertTrue(logger.isEnabled(Level.INFO), "INFO should be enabled");
        assertTrue(logger.isEnabled(Level.WARN), "WARN should be enabled");
        assertTrue(logger.isEnabled(Level.ERROR), "ERROR should be enabled");
        assertFalse(logger.isEnabled(Level.DEBUG), "DEBUG should not be enabled");
        assertFalse(logger.isEnabled(Level.TRACE), "TRACE should not be enabled");

        // Test with marker (should be the same as without marker)
        Marker marker = new BasicMarkerFactory().getMarker("TEST");
        assertTrue(logger.isEnabled(marker, Level.INFO), "INFO should be enabled with marker");
        assertFalse(logger.isEnabled(marker, Level.DEBUG), "DEBUG should not be enabled with marker");
    }

    @Test
    void testIsLevelEnabled() {
        // Set level to INFO
        logger.setLevel(Level.INFO);

        // Test various levels
        assertTrue(logger.isInfoEnabled(), "INFO should be enabled");
        assertTrue(logger.isWarnEnabled(), "WARN should be enabled");
        assertTrue(logger.isErrorEnabled(), "ERROR should be enabled");
        assertFalse(logger.isDebugEnabled(), "DEBUG should not be enabled");
        assertFalse(logger.isTraceEnabled(), "TRACE should not be enabled");

        // Test with marker (should be the same as without marker)
        Marker marker = new BasicMarkerFactory().getMarker("TEST");
        assertTrue(logger.isInfoEnabled(marker), "INFO should be enabled with marker");
        assertFalse(logger.isDebugEnabled(marker), "DEBUG should not be enabled with marker");
    }

    @Test
    void testHandleNormalizedLoggingCall() {
        // Call a logging method
        logger.info("Test message");

        // Verify that the handler was called
        assertTrue(handlerCalled.get(), "Handler should have been called");
    }

    @Test
    void testHandleNormalizedLoggingCallWithMarker() {
        // Create a marker
        Marker marker = new BasicMarkerFactory().getMarker("TEST");

        // Call a logging method with marker
        logger.info(marker, "Test message");

        // Verify that the handler was called
        assertTrue(handlerCalled.get(), "Handler should have been called");
    }

    @Test
    void testHandleNormalizedLoggingCallWithThrowable() {
        // Create a throwable
        Throwable throwable = new RuntimeException("Test exception");

        // Call a logging method with throwable
        logger.info("Test message", throwable);

        // Verify that the handler was called
        assertTrue(handlerCalled.get(), "Handler should have been called");
    }

    @Test
    void testHandleNormalizedLoggingCallWithArguments() {
        // Call a logging method with arguments
        logger.info("Test message with {} and {}", "arg1", "arg2");

        // Verify that the handler was called
        assertTrue(handlerCalled.get(), "Handler should have been called");
    }
}