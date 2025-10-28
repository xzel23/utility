package com.dua3.utlity.application;

import com.dua3.utility.application.ApplicationUtil;
import com.dua3.utility.application.DarkModeDetector;
import com.dua3.utility.application.imp.DarkModeDetectorImpUnsupported;
import com.dua3.utility.lang.Platform;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestReporter;

import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ApplicationUtilTestJava25 {

    @Test
    void darkModeDetector_returnsInstanceOnJava25(TestReporter reporter) {
        DarkModeDetector detector = ApplicationUtil.darkModeDetector();
        assertNotNull(detector, "ApplicationUtil.darkModeDetector() should return a non-null instance");

        List<Platform> supportedPlatforms = List.of(Platform.MACOS, Platform.WINDOWS, Platform.LINUX);
        boolean isSupported = !(detector instanceof DarkModeDetectorImpUnsupported);
        assertEquals(
                supportedPlatforms.contains(Platform.currentPlatform()), isSupported,
                "ApplicationUtil.darkModeDetector() should return an instance for the current platform if it is supported"
        );
        System.out.format(
                "%-20s: %s%n%-20s: %s%n%-20s: %s%n%-20s: %s%n",
                "Platform", Platform.currentPlatform(),
                "isSuppored", isSupported,
                "implementing class", detector.getClass(),
                "dark mode enabled", detector.isDarkMode()
        );
    }

    @Test
    void darkModeDetector_methodsCallableWithoutExceptionsOnJava25() {
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