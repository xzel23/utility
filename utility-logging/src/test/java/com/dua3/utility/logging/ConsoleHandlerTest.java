package com.dua3.utility.logging;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.function.Supplier;

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
        PrintStream testOut = new PrintStream(outContent, true, StandardCharsets.UTF_8);

        // Create a ConsoleHandler with the test PrintStream
        handler = new ConsoleHandler("ConsoleHandlerTest", testOut, true);

        // Create a test log entry
        testEntry = new SimpleLogEntry(Instant.now(), "TestLogger", LogLevel.INFO, "TEST_MARKER", "Test message", "com.example.TestClass.testMethod(TestClass.java:123)", null);
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
        ConsoleHandler nonColoredHandler = new ConsoleHandler("NonColoredHandler", new PrintStream(outContent, true, StandardCharsets.UTF_8), false);
        assertFalse(nonColoredHandler.isColored(), "Handler should not be colored when constructed with colored=false");
    }

    @Test
    void testHandle() {
        // Handle the test entry
        handler.handle(
                testEntry.time(),
                testEntry.loggerName(),
                testEntry.level(),
                testEntry.marker(),
                testEntry::message,
                testEntry.location(),
                testEntry.throwable()
        );

        // Get the output
        String output = outContent.toString(StandardCharsets.UTF_8);

        // Test that the output contains the expected parts
        assertTrue(output.contains("[INFO]"), "Output should contain the log level");
        assertTrue(output.contains("TestLogger"), "Output should contain the logger name");
        assertTrue(output.contains("Test message"), "Output should contain the message");
        assertTrue(output.contains("com.example.TestClass.testMethod(TestClass.java:123)"), "Output should contain the location");
    }

    @Test
    void testHandleWithThrowable() {
        // Create a test entry with a throwable
        Throwable throwable = new RuntimeException("Test exception");
        LogEntry entryWithThrowable = new SimpleLogEntry(Instant.now(), "TestLogger", LogLevel.ERROR, "TEST_MARKER", "Test message with exception", "com.example.TestClass.testMethod(TestClass.java:123)", throwable);

        // Handle the entry
        handler.handle(
                entryWithThrowable.time(),
                entryWithThrowable.loggerName(),
                entryWithThrowable.level(),
                entryWithThrowable.marker(),
                entryWithThrowable::message,
                entryWithThrowable.location(),
                entryWithThrowable.throwable()
        );

        // Get the output
        String output = outContent.toString(StandardCharsets.UTF_8);

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
        handler.handle(
                testEntry.time(),
                testEntry.loggerName(),
                testEntry.level(),
                testEntry.marker(),
                testEntry::message,
                testEntry.location(),
                testEntry.throwable()
        );

        // Get the colored output
        String coloredOutput = outContent.toString(StandardCharsets.UTF_8);
        outContent.reset();

        // Handle the test entry with non-colored output
        handler.setColored(false);
        handler.handle(
                testEntry.time(),
                testEntry.loggerName(),
                testEntry.level(),
                testEntry.marker(),
                testEntry::message,
                testEntry.location(),
                testEntry.throwable()
        );

        // Get the non-colored output
        String nonColoredOutput = outContent.toString(StandardCharsets.UTF_8);

        // Test that the colored output is different from the non-colored output
        assertNotEquals(coloredOutput, nonColoredOutput, "Colored output should be different from non-colored output");
    }

    @Test
    void testSetFormatLog4j() {
        // Set a Log4j-style format
        String format = "%p %c - %m%n";
        handler.setFormat(format);
        assertEquals(format, handler.getFormat());

        handler.handle(
                testEntry.time(),
                testEntry.loggerName(),
                testEntry.level(),
                testEntry.marker(),
                testEntry::message,
                testEntry.location(),
                testEntry.throwable()
        );

        // Get the output
        String output = outContent.toString(StandardCharsets.UTF_8);
        assertEquals("INFO TestLogger - Test message" + System.lineSeparator(), output);
    }

    @Test
    void testGetFormatReconstruction() {
        // Test various formats and their reconstruction
        String[] formats = {
                "%p %c %marker %m %l %ex %Cstart %Cend %n",
                "%-5p %.10c %10.20m",
                "%d %d{HH:mm:ss,SSS} %p",
                "[%level] %logger %msg %exception",
                "%% %p %%"
        };

        String[] expectedReconstructed = {
                "%p %c %marker %m %l %ex %Cstart %Cend %n",
                "%-5p %.10c %10.20m",
                "%d %d{HH:mm:ss,SSS} %p",
                "[%p] %c %m %ex", // Note: aliases like %level are converted to preferred ones like %p
                "%% %p %%"
        };

        for (int i = 0; i < formats.length; i++) {
            handler.setFormat(formats[i]);
            assertEquals(expectedReconstructed[i], handler.getFormat(), "Reconstructed format mismatch for: " + formats[i]);
        }
    }

    @Test
    void testSetFormatLog4jWithPadding() {
        // Set a Log4j-style format with padding
        handler.setFormat("%-5p %c - %m%n");
        handler.handle(
                testEntry.time(),
                testEntry.loggerName(),
                testEntry.level(),
                testEntry.marker(),
                testEntry::message,
                testEntry.location(),
                testEntry.throwable()
        );

        // Get the output
        String output = outContent.toString(StandardCharsets.UTF_8);
        assertEquals("INFO  TestLogger - Test message" + System.lineSeparator(), output);
    }

    @Test
    void testDefaultFormat() {
        // Handle the test entry with default format and colors disabled
        handler.setColored(false);
        handler.handle(
                testEntry.time(),
                testEntry.loggerName(),
                testEntry.level(),
                testEntry.marker(),
                testEntry::message,
                testEntry.location(),
                testEntry.throwable()
        );

        // Get the output
        String output = outContent.toString(StandardCharsets.UTF_8);
        // Default format: "%Cstart[%p] %c %marker %m %l %ex%Cend%n"
        // Since colored is false, Cstart and Cend are empty.
        // Also marker is "TEST_MARKER", level is "INFO", etc.
        assertTrue(output.contains("[INFO]"), "Output should contain the log level");
        assertTrue(output.contains("TestLogger"), "Output should contain the logger name");
        assertTrue(output.contains("TEST_MARKER"), "Output should contain the marker");
        assertTrue(output.contains("Test message"), "Output should contain the message");
        assertTrue(output.contains("com.example.TestClass.testMethod(TestClass.java:123)"), "Output should contain the location");
    }

    @Test
    void testSetAndGetFilter() {
        // Test the default filter
        LogFilter defaultFilter = handler.getFilter();
        assertEquals(LogFilter.allPass(), defaultFilter, "Default filter should be the all-pass filter");

        // Set a custom filter that only allows ERROR level entries
        LogFilter customFilter = new LogFilter() {
            @Override
            public String name() {
                return "error only";
            }

            @Override
            public boolean test(Instant instant, String loggerName, LogLevel lvl, String mrk, Supplier<String> msg, String location, Throwable t) {
                return lvl == LogLevel.ERROR;
            }
        };
        handler.setFilter(customFilter);

        // Test the new filter
        assertEquals(customFilter, handler.getFilter(), "Filter should be the custom filter after setFilter");

        // Test that the filter is actually used in handleEntry

        // First, handle an INFO level entry (should be filtered out)
        outContent.reset(); // Clear previous output
        LogEntry infoEntry = new SimpleLogEntry(Instant.now(), "TestLogger", LogLevel.INFO, "TEST_MARKER", "Info message", "com.example.TestClass.testMethod(TestClass.java:123)", null);
        handler.handle(
                infoEntry.time(),
                infoEntry.loggerName(),
                infoEntry.level(),
                infoEntry.marker(),
                infoEntry::message,
                infoEntry.location(),
                infoEntry.throwable()
        );

        // The output should be empty because the INFO entry should be filtered out
        String infoOutput = outContent.toString(StandardCharsets.UTF_8);
        assertEquals("", infoOutput, "INFO level entry should be filtered out");

        // Now, handle an ERROR level entry (should pass through the filter)
        outContent.reset(); // Clear previous output
        LogEntry errorEntry = new SimpleLogEntry(Instant.now(), "TestLogger", LogLevel.ERROR, "TEST_MARKER", "Error message", "com.example.TestClass.testMethod(TestClass.java:123)", null);
        handler.handle(
                errorEntry.time(),
                errorEntry.loggerName(),
                errorEntry.level(),
                errorEntry.marker(),
                errorEntry::message,
                errorEntry.location(),
                errorEntry.throwable()
        );

        // The output should contain the error message because the ERROR entry should pass through the filter
        String errorOutput = outContent.toString(StandardCharsets.UTF_8);
        assertTrue(errorOutput.contains("Error message"), "ERROR level entry should pass through the filter");
    }
}
