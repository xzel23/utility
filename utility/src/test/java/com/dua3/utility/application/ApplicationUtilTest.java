package com.dua3.utility.application;

import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class ApplicationUtilTest {

    @Test
    void testPreferencesShouldNotBeNull() {
        // Initialize with a valid Preferences instance
        assertNotNull(ApplicationUtil.preferences());
    }

    @Test
    void testRecentlyUsedDocumentsShouldReturnSameInstance() {
        var first = ApplicationUtil.recentlyUsedDocuments();
        var second = ApplicationUtil.recentlyUsedDocuments();
        assertSame( first, second);
    }

    @Test
    void darkModeDetector_returnsInstance() {
        DarkModeDetector detector = ApplicationUtil.darkModeDetector();
        assertNotNull(detector, "ApplicationUtil.darkModeDetector() should return a non-null instance");
    }

    @Test
    void darkModeDetector_methodsCallableWithoutExceptions() {
        DarkModeDetector detector = ApplicationUtil.darkModeDetector();
        assertNotNull(detector);

        // simple listener
        Consumer<Boolean> listener = b -> { /* no-op */ };

        // none of the following calls should throw
        assertDoesNotThrow(detector::isDarkModeDetectionSupported, "Calling isDarkModeDetectionSupported should not throw");
        assertDoesNotThrow(detector::isDarkMode, "Calling isDarkMode should not throw");
        assertDoesNotThrow(() -> detector.addListener(listener), "Adding a listener should not throw");
        assertDoesNotThrow(() -> detector.removeListener(listener), "Removing a listener should not throw");

        // removing again should also not throw
        assertDoesNotThrow(() -> detector.removeListener(listener), "Removing an already removed listener should not throw");
    }
}