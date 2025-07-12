package com.dua3.utility.logging;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the {@link ConsoleHandler} class.
 */
class ConsoleHandlerTest {

    private ByteArrayOutputStream outContent;
    private PrintStream originalOut;
    private ConsoleHandler handler;
    private LogEntry testEntry;

    @BeforeEach
    void setUp() {
        // Save the original System.out
        originalOut = System.out;

        // Set up a new ByteArrayOutputStream to capture output
        outContent = new ByteArrayOutputStream();
        PrintStream testOut = new PrintStream(outContent);

        // Create a ConsoleHandler with the test PrintStream
        handler = new ConsoleHandler(testOut, true);

        // Create a test log entry
        testEntry = new SimpleLogEntry("Test message", "TestLogger", Instant.now(), LogLevel.INFO, "TEST_MARKER", null, "com.example.TestClass.testMethod(TestClass.java:123)");
    }

    @AfterEach
    void tearDown() {
        // Restore the original System.out
        System.setOut(originalOut);
    }

    @Test
    void testConstructor() {
        // Test that the constructor sets the colored flag correctly
        assertTrue(handler.isColored(), "Handler should be colored by default when constructed with colored=true");

        // Create a non-colored handler
        ConsoleHandler nonColoredHandler = new ConsoleHandler(new PrintStream(outContent), false);
        assertFalse(nonColoredHandler.isColored(), "Handler should not be colored when constructed with colored=false");
    }

    @Test
    void testHandleEntry() {
        // Handle the test entry
        handler.handleEntry(testEntry);

        // Get the output
        String output = outContent.toString();

        // Test that the output contains the expected parts
        assertTrue(output.contains("[INFO]"), "Output should contain the log level");
        assertTrue(output.contains("TestLogger"), "Output should contain the logger name");
        assertTrue(output.contains("Test message"), "Output should contain the message");
        assertTrue(output.contains("com.example.TestClass.testMethod(TestClass.java:123)"), "Output should contain the location");
    }

    @Test
    void testHandleEntryWithThrowable() {
        // Create a test entry with a throwable
        Throwable throwable = new RuntimeException("Test exception");
        LogEntry entryWithThrowable = new SimpleLogEntry("Test message with exception", "TestLogger", Instant.now(), LogLevel.ERROR, "TEST_MARKER", throwable, "com.example.TestClass.testMethod(TestClass.java:123)");

        // Handle the entry
        handler.handleEntry(entryWithThrowable);

        // Get the output
        String output = outContent.toString();

        // Test that the output contains the expected parts
        assertTrue(output.contains("[ERROR]"), "Output should contain the log level");
        assertTrue(output.contains("Test message with exception"), "Output should contain the message");
        assertTrue(output.contains("RuntimeException"), "Output should contain the exception class");
        assertTrue(output.contains("Test exception"), "Output should contain the exception message");
    }

    @Test
    void testSetAndIsColored() {
        // Test the default state
        assertTrue(handler.isColored(), "Handler should be colored by default");

        // Set colored to false
        handler.setColored(false);

        // Test the new state
        assertFalse(handler.isColored(), "Handler should not be colored after setColored(false)");

        // Set colored back to true
        handler.setColored(true);

        // Test the new state
        assertTrue(handler.isColored(), "Handler should be colored after setColored(true)");
    }

    @Test
    void testColoredOutput() {
        // Handle the test entry with colored output
        handler.setColored(true);
        handler.handleEntry(testEntry);

        // Get the colored output
        String coloredOutput = outContent.toString();
        outContent.reset();

        // Handle the test entry with non-colored output
        handler.setColored(false);
        handler.handleEntry(testEntry);

        // Get the non-colored output
        String nonColoredOutput = outContent.toString();

        // Test that the colored output is different from the non-colored output
        assertNotEquals(coloredOutput, nonColoredOutput, "Colored output should be different from non-colored output");
    }

    @Test
    void testSetAndGetFilter() {
        // Test the default filter
        LogEntryFilter defaultFilter = handler.getFilter();
        assertEquals(LogEntryFilter.allPass(), defaultFilter, "Default filter should be the all-pass filter");

        // Set a custom filter that only allows ERROR level entries
        LogEntryFilter customFilter = entry -> entry.level() == LogLevel.ERROR;
        handler.setFilter(customFilter);

        // Test the new filter
        assertEquals(customFilter, handler.getFilter(), "Filter should be the custom filter after setFilter");

        // Test that the filter is actually used in handleEntry

        // First, handle an INFO level entry (should be filtered out)
        outContent.reset(); // Clear previous output
        LogEntry infoEntry = new SimpleLogEntry("Info message", "TestLogger", Instant.now(), LogLevel.INFO, "TEST_MARKER", null, "com.example.TestClass.testMethod(TestClass.java:123)");
        handler.handleEntry(infoEntry);

        // The output should be empty because the INFO entry should be filtered out
        String infoOutput = outContent.toString();
        assertEquals("", infoOutput, "INFO level entry should be filtered out");

        // Now, handle an ERROR level entry (should pass through the filter)
        outContent.reset(); // Clear previous output
        LogEntry errorEntry = new SimpleLogEntry("Error message", "TestLogger", Instant.now(), LogLevel.ERROR, "TEST_MARKER", null, "com.example.TestClass.testMethod(TestClass.java:123)");
        handler.handleEntry(errorEntry);

        // The output should contain the error message because the ERROR entry should pass through the filter
        String errorOutput = outContent.toString();
        assertTrue(errorOutput.contains("Error message"), "ERROR level entry should pass through the filter");
    }
}
