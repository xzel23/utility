package com.dua3.utility.logging;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the {@link LogLevel} enum.
 */
class LogLevelTest {

    @Test
    void testEnumValues() {
        // Test that all expected enum values exist
        assertEquals(5, LogLevel.values().length, "There should be 5 log levels");

        // Test the order of enum values
        assertEquals(LogLevel.TRACE, LogLevel.values()[0], "TRACE should be the first enum value");
        assertEquals(LogLevel.DEBUG, LogLevel.values()[1], "DEBUG should be the second enum value");
        assertEquals(LogLevel.INFO, LogLevel.values()[2], "INFO should be the third enum value");
        assertEquals(LogLevel.WARN, LogLevel.values()[3], "WARN should be the fourth enum value");
        assertEquals(LogLevel.ERROR, LogLevel.values()[4], "ERROR should be the fifth enum value");
    }

    @Test
    void testColorize() {
        // Test colorize with colored=true
        String text = "Test message";
        for (LogLevel level : LogLevel.values()) {
            String colorized = level.colorize(text, true);
            assertNotEquals(text, colorized, "Colorized text should be different from original text for level " + level);
            assertTrue(colorized.contains(text), "Colorized text should contain the original text for level " + level);
            assertTrue(colorized.startsWith(level.escStart), "Colorized text should start with escStart for level " + level);
            assertTrue(colorized.endsWith(level.escEnd), "Colorized text should end with escEnd for level " + level);
        }

        // Test colorize with colored=false
        for (LogLevel level : LogLevel.values()) {
            String colorized = level.colorize(text, false);
            assertEquals(text, colorized, "Text should remain unchanged when colored=false for level " + level);
        }
    }

    @Test
    void testEscapeSequences() {
        // Test that escape sequences are not empty
        for (LogLevel level : LogLevel.values()) {
            assertNotNull(level.escStart, "escStart should not be null for level " + level);
            assertNotNull(level.escEnd, "escEnd should not be null for level " + level);
            assertFalse(level.escStart.isEmpty(), "escStart should not be empty for level " + level);
            assertFalse(level.escEnd.isEmpty(), "escEnd should not be empty for level " + level);
        }
    }
}