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
    void testRecentlyUsedDocumentsShouldReturnSameInstance() {
        var first = ApplicationUtil.recentlyUsedDocuments();
        var second = ApplicationUtil.recentlyUsedDocuments();
        assertSame( first, second);
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

        ApplicationUtil.setUiMode(UiMode.LIGHT);
        ApplicationUtil.addUiModeListener(uiModeListener);
        ApplicationUtil.addDarkModeListener(darkModeListener);
        try {
            ApplicationUtil.setUiMode(UiMode.DARK);
            assertEquals(UiMode.DARK, observedUiMode.get());
            assertTrue(ApplicationUtil.isDarkMode());
            assertTrue(observedDarkMode.get());

            ApplicationUtil.removeUiModeListener(uiModeListener);
            ApplicationUtil.removeDarkModeListener(darkModeListener);
            ApplicationUtil.setUiMode(UiMode.LIGHT);

            assertEquals(UiMode.DARK, observedUiMode.get());
            assertTrue(observedDarkMode.get());
            assertFalse(ApplicationUtil.isDarkMode());
        } finally {
            ApplicationUtil.removeUiModeListener(uiModeListener);
            ApplicationUtil.removeDarkModeListener(darkModeListener);
            ApplicationUtil.setUiMode(originalMode);
        }
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
