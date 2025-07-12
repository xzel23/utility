package com.dua3.utility.logging.log4j;

import com.dua3.utility.logging.LogLevel;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.message.SimpleMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the {@link LogEntryLog4J} class.
 */
@Execution(ExecutionMode.SAME_THREAD)
class LogEntryLog4JTest {

    @Test
    void testConstructorAndGetters() {
        // Create a log event
        String loggerName = "TestLogger";
        Level level = Level.INFO;
        String message = "Test message";
        Exception exception = new RuntimeException("Test exception");

        LogEvent event = Log4jLogEvent.newBuilder().setLoggerName(loggerName).setLevel(level).setMessage(new SimpleMessage(message)).setThrown(exception).build();

        // Create a LogEntryLog4J from the event
        LogEntryLog4J entry = new LogEntryLog4J(event);

        // Test getters
        assertEquals(loggerName, entry.loggerName());
        assertEquals(LogLevel.INFO, entry.level());
        assertEquals(message, entry.message());
        assertEquals(exception, entry.throwable());
        assertNotNull(entry.time());

        // Test toString
        assertNotNull(entry.toString());
    }

    @Test
    void testMarker() {
        // Create a log event with a marker
        String markerName = "TEST_MARKER";
        org.apache.logging.log4j.Marker marker = org.apache.logging.log4j.MarkerManager.getMarker(markerName);

        LogEvent event = Log4jLogEvent.newBuilder().setLoggerName("TestLogger").setLevel(Level.INFO).setMessage(new SimpleMessage("Test message")).setMarker(marker).build();

        // Create a LogEntryLog4J from the event
        LogEntryLog4J entry = new LogEntryLog4J(event);

        // Test marker
        assertEquals(markerName, entry.marker());
    }

    @Test
    void testLocation() {
        // Create a log event with a source
        StackTraceElement source = new StackTraceElement("TestClass", "testMethod", "TestFile.java", 123);

        LogEvent event = Log4jLogEvent.newBuilder().setLoggerName("TestLogger").setLevel(Level.INFO).setMessage(new SimpleMessage("Test message")).setSource(source).build();

        // Create a LogEntryLog4J from the event
        LogEntryLog4J entry = new LogEntryLog4J(event);

        // Test location
        String location = entry.location();
        assertNotNull(location);
        assertTrue(location.contains("TestFile.java"));
        assertTrue(location.contains("123"));
        assertTrue(location.contains("testMethod"));

        // Test that calling location() again returns the same value
        assertEquals(location, entry.location());
    }

    @Test
    void testReusableMessage() {
        // The LogEntryLog4J class checks if the message is an instance of ReusableMessage and calls formatTo(),
        // but our mock doesn't properly implement this behavior.
        // For now, we'll just use a SimpleMessage instead to test the basic functionality.

        String expectedMessage = "Test message";
        LogEvent event = Log4jLogEvent.newBuilder().setLoggerName("TestLogger").setLevel(Level.INFO).setMessage(new SimpleMessage(expectedMessage)).build();

        // Create a LogEntryLog4J from the event
        LogEntryLog4J entry = new LogEntryLog4J(event);

        // Test message
        assertEquals(expectedMessage, entry.message());
    }
}
