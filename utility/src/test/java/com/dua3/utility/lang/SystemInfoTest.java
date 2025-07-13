package com.dua3.utility.lang;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SystemInfoTest {

    @Test
    void testConstructorAndGetters() {
        // Create a SystemInfo instance with known values
        SystemInfo info = new SystemInfo("TestOS", "1.0", "x64", "17.0.2", "TestVendor", "/path/to/java", "11.0.2");

        // Test getters
        assertEquals("TestOS", info.osName());
        assertEquals("1.0", info.osVersion());
        assertEquals("x64", info.osArch());
        assertEquals("17.0.2", info.javaVersion());
        assertEquals("TestVendor", info.javaVendor());
        assertEquals("/path/to/java", info.javaHome());
        assertEquals("11.0.2", info.javafxVersion());
    }

    @Test
    void testGetSystemInfo() {
        // Get system info
        SystemInfo info = SystemInfo.getSystemInfo();

        // Verify that all fields are populated (not null)
        assertNotNull(info.osName());
        assertNotNull(info.osVersion());
        assertNotNull(info.osArch());
        assertNotNull(info.javaVersion());
        assertNotNull(info.javaVendor());
        assertNotNull(info.javaHome());
        assertNotNull(info.javafxVersion());

        // Verify that values match system properties
        assertEquals(System.getProperty("os.name", "Unknown"), info.osName());
        assertEquals(System.getProperty("os.version", "Unknown"), info.osVersion());
        assertEquals(System.getProperty("os.arch", "Unknown"), info.osArch());
        assertEquals(System.getProperty("java.version", "Unknown"), info.javaVersion());
        assertEquals(System.getProperty("java.vendor", "Unknown"), info.javaVendor());
        assertEquals(System.getProperty("java.home", "Unknown"), info.javaHome());
        assertEquals(System.getProperty("javafx.runtime.version", "Unknown"), info.javafxVersion());
    }

    @Test
    void testFormatted() {
        // Create a SystemInfo instance with known values
        SystemInfo info = new SystemInfo("TestOS", "1.0", "x64", "17.0.2", "TestVendor", "/path/to/java", "11.0.2");

        // Get formatted string
        String formatted = info.formatted();

        // Verify that the formatted string contains all the information
        assertTrue(formatted.contains("TestOS"));
        assertTrue(formatted.contains("1.0"));
        assertTrue(formatted.contains("x64"));
        assertTrue(formatted.contains("17.0.2"));
        assertTrue(formatted.contains("TestVendor"));
        assertTrue(formatted.contains("/path/to/java"));
        assertTrue(formatted.contains("11.0.2"));

        // Verify the expected format
        String expected = """
                OS: TestOS 1.0 (x64)
                Java: 17.0.2 TestVendor (/path/to/java)
                JavaFX: 11.0.2
                """;
        assertEquals(expected, formatted);
    }
}