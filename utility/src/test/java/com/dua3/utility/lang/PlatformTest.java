package com.dua3.utility.lang;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlatformTest {

    @Test
    void testCurrentPlatform() {
        Platform current = Platform.currentPlatform();
        assertNotNull(current);

        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            assertEquals(Platform.WINDOWS, current);
            assertTrue(Platform.isWindows());
            assertFalse(Platform.isLinux());
            assertFalse(Platform.isMacOS());
        } else if (os.contains("mac")) {
            assertEquals(Platform.MACOS, current);
            assertTrue(Platform.isMacOS());
            assertFalse(Platform.isLinux());
            assertFalse(Platform.isWindows());
        } else if (os.contains("linux")) {
            assertEquals(Platform.LINUX, current);
            assertTrue(Platform.isLinux());
            assertFalse(Platform.isWindows());
            assertFalse(Platform.isMacOS());
        }
    }

    @Test
    void testIsNativeImage() {
        // In standard JVM test runner, this should be false
        assertFalse(Platform.isNativeImage());
    }

    @Test
    void testLinuxAndMacProcessBuilderQuoting() {
        assertFalse(Platform.LINUX.isProcessBuilderQuotingNeeded("hello world"));
        assertEquals("hello world", Platform.LINUX.quoteProcessBuilderArg("hello world"));

        assertFalse(Platform.MACOS.isProcessBuilderQuotingNeeded("hello world"));
        assertEquals("hello world", Platform.MACOS.quoteProcessBuilderArg("hello world"));
    }

    @Test
    void testWindowsProcessBuilderQuoting() {
        // Empty string
        assertTrue(Platform.WINDOWS.isProcessBuilderQuotingNeeded(""));
        assertEquals("\"\"", Platform.WINDOWS.quoteProcessBuilderArg(""));

        // No quoting needed
        assertFalse(Platform.WINDOWS.isProcessBuilderQuotingNeeded("simple"));
        assertEquals("simple", Platform.WINDOWS.quoteProcessBuilderArg("simple"));

        // Quoting with spaces, tabs, slashes, quotes
        assertTrue(Platform.WINDOWS.isProcessBuilderQuotingNeeded("hello world"));
        assertEquals("\"hello world\"", Platform.WINDOWS.quoteProcessBuilderArg("hello world"));

        assertTrue(Platform.WINDOWS.isProcessBuilderQuotingNeeded("tab\there"));
        assertEquals("\"tab\there\"", Platform.WINDOWS.quoteProcessBuilderArg("tab\there"));

        assertTrue(Platform.WINDOWS.isProcessBuilderQuotingNeeded("C:\\path\\to\\dir"));
        assertEquals("\"C:\\path\\to\\dir\"", Platform.WINDOWS.quoteProcessBuilderArg("C:\\path\\to\\dir"));

        assertTrue(Platform.WINDOWS.isProcessBuilderQuotingNeeded("say \"hello\""));
        assertEquals("\"say \\\"hello\\\"\"", Platform.WINDOWS.quoteProcessBuilderArg("say \"hello\""));

        assertTrue(Platform.WINDOWS.isProcessBuilderQuotingNeeded("trailing\\"));
        assertEquals("\"trailing\\\\\"", Platform.WINDOWS.quoteProcessBuilderArg("trailing\\"));
    }
}
