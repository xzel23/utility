package com.dua3.utility.application;

import com.dua3.utility.i18n.I18N;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UiModeTest {

    @Test
    void testToLocalizedString() {
        // We can't easily change the global I18N instance locale once initialized,
        // so we check if the keys are present in the bundles.
        
        I18N i18nEn = I18N.create("com.dua3.utility.application.messages", Locale.ENGLISH);
        assertEquals("System Default", i18nEn.get("dua3.ui.mode.system_default"));
        assertEquals("Light", i18nEn.get("dua3.ui.mode.light"));
        assertEquals("Dark", i18nEn.get("dua3.ui.mode.dark"));

        I18N i18nDe = I18N.create("com.dua3.utility.application.messages", Locale.GERMAN);
        assertEquals("Systemstandard", i18nDe.get("dua3.ui.mode.system_default"));
        assertEquals("Hell", i18nDe.get("dua3.ui.mode.light"));
        assertEquals("Dunkel", i18nDe.get("dua3.ui.mode.dark"));

        I18N i18nFr = I18N.create("com.dua3.utility.application.messages", Locale.FRENCH);
        assertEquals("Système par défaut", i18nFr.get("dua3.ui.mode.system_default"));
        assertEquals("Clair", i18nFr.get("dua3.ui.mode.light"));
        assertEquals("Sombre", i18nFr.get("dua3.ui.mode.dark"));

        I18N i18nEs = I18N.create("com.dua3.utility.application.messages", Locale.forLanguageTag("es"));
        assertEquals("Predeterminado del sistema", i18nEs.get("dua3.ui.mode.system_default"));
        assertEquals("Claro", i18nEs.get("dua3.ui.mode.light"));
        assertEquals("Oscuro", i18nEs.get("dua3.ui.mode.dark"));
    }

    @Test
    void testToLocalizedStringViaUiMode() {
        // This will use the global I18N instance, which should have the bundle merged.
        // Since we don't know the default locale, we just check if it returns one of the expected values.
        String localized = UiMode.LIGHT.toLocalizedString();
        assertTrue(localized.equals("Light") || localized.equals("Hell"), "Localized string was: " + localized);
    }
}
