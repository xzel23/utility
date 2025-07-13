package com.dua3.utility.db;

import com.dua3.utility.options.Arguments;
import com.dua3.utility.options.ArgumentsParser;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for JdbcDriverInfo.
 */
class JdbcDriverInfoTest {

    @Test
    void testToString() {
        // Create a JdbcDriverInfo instance
        JdbcDriverInfo driverInfo = new JdbcDriverInfo(
                "Test Driver",
                "com.test.Driver",
                "jdbc:test:",
                "jdbc:test://${SERVER:type=string}:${PORT:type=integer}/${DATABASE}",
                "https://test-driver.example.com"
        );

        // Test toString method
        assertEquals("Test Driver", driverInfo.toString());
    }

    @Test
    void testDescription() {
        // Create a JdbcDriverInfo instance
        JdbcDriverInfo driverInfo = new JdbcDriverInfo(
                "Test Driver",
                "com.test.Driver",
                "jdbc:test:",
                "jdbc:test://${SERVER:type=string}:${PORT:type=integer}/${DATABASE}",
                "https://test-driver.example.com"
        );

        // Test description method
        String description = driverInfo.description();

        // Verify the description contains all the expected information
        assertTrue(description.contains("Test Driver"));
        assertTrue(description.contains("com.test.Driver"));
        assertTrue(description.contains("jdbc:test:"));
        assertTrue(description.contains("jdbc:test://${SERVER}:${PORT}/${DATABASE}"));
        assertTrue(description.contains("https://test-driver.example.com"));

        // Verify the format matches the expected format
        String expectedFormat = String.format(Locale.ROOT,
                "%s%n  driver class : %s%n  URL prefix   : %s%n  URL scheme   : %s%n  vendor link  : %s%n%s%n",
                "Test Driver",
                "com.test.Driver",
                "jdbc:test:",
                "jdbc:test://${SERVER}:${PORT}/${DATABASE}",
                "https://test-driver.example.com",
                driverInfo.options);

        assertEquals(expectedFormat, description);
    }

    @Test
    void testGetUrl() {
        // Create a JdbcDriverInfo instance with a URL scheme containing placeholders
        JdbcDriverInfo driverInfo = new JdbcDriverInfo(
                "Test Driver",
                "com.test.Driver",
                "jdbc:test:",
                "jdbc:test://${SERVER:type=string}:${PORT:type=integer}/${DATABASE}",
                "https://test-driver.example.com"
        );

        // Expected URL format
        String expectedUrl = "jdbc:test://localhost:5432/testdb";

        // Create an arguments parser and parse the options
        ArgumentsParser argParser = driverInfo.createArgumentsParser();

        Arguments args = argParser.parse(
                "--SERVER", "localhost",
                "--PORT", "5432",
                "--DATABASE", "testdb"
        );

        // make sure the result matches
        assertEquals(expectedUrl, driverInfo.getUrl(args));
    }

    @Test
    void testGetUrlWithMissingOption() {
        // Create a JdbcDriverInfo instance with a URL scheme containing placeholders
        JdbcDriverInfo driverInfo = new JdbcDriverInfo(
                "Test Driver",
                "com.test.Driver",
                "jdbc:test:",
                "jdbc:test://${SERVER:type=string}:${PORT:type=integer}/${DATABASE}",
                "https://test-driver.example.com"
        );

        // Create an arguments parser and parse the options with the argument for the port missing
        ArgumentsParser argParser = driverInfo.createArgumentsParser();

        Arguments args = argParser.parse(
                "--SERVER", "localhost",
                "--DATABASE", "testdb"
        );

        // We expect a NoSuchElementException when a required option is missing
        assertThrows(NoSuchElementException.class, () -> driverInfo.getUrl(args));
    }
}
