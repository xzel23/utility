package com.dua3.utility.application;

/**
 * Enum representing the UI mode of an application.
 * <p>
 * The UI mode can be one of the following:
 * - SYSTEM_DEFAULT: Automatically follows the system's UI mode setting.
 * - LIGHT: Explicitly sets the application to use a light UI theme.
 * - DARK: Explicitly sets the application to use a dark UI theme.
 */
public enum UiMode {
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

    UiMode(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}
