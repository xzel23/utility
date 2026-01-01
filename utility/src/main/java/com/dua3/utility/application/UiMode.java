package com.dua3.utility.application;

import com.dua3.utility.I18NInstance;
import com.dua3.utility.lang.Localized;

import java.util.Locale;

/**
 * Enum representing the UI mode of an application.
 * <p>
 * The UI mode can be one of the following:
 * - SYSTEM_DEFAULT: Automatically follows the system's UI mode setting.
 * - LIGHT: Explicitly sets the application to use a light UI theme.
 * - DARK: Explicitly sets the application to use a dark UI theme.
 */
public enum UiMode implements Localized {
    /**
     * Represents the default UI mode that follows the system's UI mode setting.
     */
    SYSTEM_DEFAULT("System Default"),
    /**
     * Represents the light UI mode.
     */
    LIGHT("Light"),
    /**
     * Represents the dark UI mode.
     */
    DARK("Dark");

    private final String text;
    private final String i18nKey;

    UiMode(String text) {
        this.text = text;
        this.i18nKey = "dua3.ui.mode." + name().toLowerCase(Locale.ROOT).replace('_', '.');
    }

    @Override
    public String toString() {
        return text;
    }

    /**
     * Returns the localized string representation of this UI mode.
     * The localization is based on the internal internationalization (i18n) key
     * associated with the current instance of the enum.
     *
     * @return the localized string corresponding to the UI mode.
     */
    @Override
    public String toLocalizedString() {
        return I18NInstance.get().get(i18nKey);
    }
}
