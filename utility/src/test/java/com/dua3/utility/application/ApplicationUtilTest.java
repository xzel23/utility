package com.dua3.utility.application;

import com.dua3.utility.i18n.I18N;
import com.dua3.utility.lang.Platform;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.prefs.Preferences;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApplicationUtilTest {

    @Test
    void testPreferencesShouldNotBeNull() {
        // Initialize with a valid Preferences instance
        assertNotNull(ApplicationUtil.preferences());
    }

    @Test
    void testInitApplicationPreferences() {
        Preferences prefs = EphemeralPreferences.createRoot();
        assertDoesNotThrow(() -> ApplicationUtil.initApplicationPreferences(prefs));
        assertDoesNotThrow(() -> ApplicationUtil.initApplicationPreferences(prefs));
    }

    @Test
    void testRecentlyUsedDocumentsShouldReturnSameInstance() {
        var first = ApplicationUtil.recentlyUsedDocuments();
        var second = ApplicationUtil.recentlyUsedDocuments();
        assertSame(first, second);
    }

    @Test
    void numericPreferenceHelpersReturnDefaultsForNaN() {
        Preferences preferences = EphemeralPreferences.createRoot();
        preferences.putDouble("double", Double.NaN);
        preferences.putFloat("float", Float.NaN);

        assertEquals(12.5, ApplicationUtil.getDoubleNoNaN(preferences, "double", 12.5));
        assertEquals(7.25f, ApplicationUtil.getFloatNoNaN(preferences, "float", 7.25f));

        preferences.putDouble("double", 4.5);
        preferences.putFloat("float", 3.5f);
        assertEquals(4.5, ApplicationUtil.getDoubleNoNaN(preferences, "double", 12.5));
        assertEquals(3.5f, ApplicationUtil.getFloatNoNaN(preferences, "float", 7.25f));
    }

    @Test
    void uiModeListenersReceiveChangesAndCanBeRemoved() {
        UiMode originalMode = ApplicationUtil.getUiMode();
        AtomicReference<UiMode> observedUiMode = new AtomicReference<>();
        AtomicReference<Boolean> observedDarkMode = new AtomicReference<>();

        Consumer<UiMode> uiModeListener = observedUiMode::set;
        Consumer<Boolean> darkModeListener = observedDarkMode::set;
        Consumer<UiMode> throwingUiModeListener = mode -> {
            throw new RuntimeException("Simulated UI mode exception");
        };
        Consumer<Boolean> throwingDarkModeListener = dark -> {
            throw new RuntimeException("Simulated dark mode exception");
        };

        ApplicationUtil.setUiMode(UiMode.LIGHT);
        ApplicationUtil.addUiModeListener(uiModeListener);
        ApplicationUtil.addDarkModeListener(darkModeListener);
        ApplicationUtil.addUiModeListener(throwingUiModeListener);
        ApplicationUtil.addDarkModeListener(throwingDarkModeListener);
        try {
            ApplicationUtil.setUiMode(UiMode.DARK);
            assertEquals(UiMode.DARK, observedUiMode.get());
            assertTrue(ApplicationUtil.isDarkMode());
            assertTrue(observedDarkMode.get());

            ApplicationUtil.removeUiModeListener(uiModeListener);
            ApplicationUtil.removeDarkModeListener(darkModeListener);
            ApplicationUtil.removeUiModeListener(throwingUiModeListener);
            ApplicationUtil.removeDarkModeListener(throwingDarkModeListener);
            ApplicationUtil.setUiMode(UiMode.LIGHT);

            assertEquals(UiMode.DARK, observedUiMode.get());
            assertTrue(observedDarkMode.get());
            assertFalse(ApplicationUtil.isDarkMode());
        } finally {
            ApplicationUtil.removeUiModeListener(uiModeListener);
            ApplicationUtil.removeDarkModeListener(darkModeListener);
            ApplicationUtil.removeUiModeListener(throwingUiModeListener);
            ApplicationUtil.removeDarkModeListener(throwingDarkModeListener);
            ApplicationUtil.setUiMode(originalMode);
        }
    }

    @Test
    void testSetUiModeNullDefaultsToSystemDefault() {
        UiMode originalMode = ApplicationUtil.getUiMode();
        try {
            ApplicationUtil.setUiMode(null);
            assertEquals(UiMode.SYSTEM_DEFAULT, ApplicationUtil.getUiMode());
        } finally {
            ApplicationUtil.setUiMode(originalMode);
        }
    }

    @Test
    void testUpdateWindowDecorations() {
        assertDoesNotThrow(ApplicationUtil::updateWindowDecorations);
    }

    @Test
    void testIsDarkModeDetectionSupported() {
        assertDoesNotThrow(ApplicationUtil::isDarkModeDetectionSupported);
    }

    @Test
    void testIsDesktopSupported() {
        assertDoesNotThrow(ApplicationUtil::isDesktopSupported);
    }

    @Test
    void showInFileManagerReturnsFalseForMissingPath() throws Exception {
        Path directory = Files.createTempDirectory("application-util-test");
        try {
            assertFalse(ApplicationUtil.showInFileManager(directory.resolve("missing")));
        } finally {
            Files.deleteIfExists(directory);
        }
    }

    @Test
    void localizedFileManagerNameIsAvailable() {
        I18N i18n = I18N.create("com.dua3.utility.messages", Locale.ENGLISH);

        String name = ApplicationUtil.getLocalizedFileManagerName(i18n);

        assertNotNull(name);
        assertFalse(name.isBlank());
        assertTrue(switch (Platform.currentPlatform()) {
            case WINDOWS -> name.equals("Explorer");
            case MACOS -> name.equals("Finder");
            case LINUX -> name.equals("File Manager");
            case UNKNOWN -> true;
        });
    }

}
