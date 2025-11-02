package com.dua3.utility.application;

import java.util.function.Consumer;

/**
 * Interface for detecting and handling dark mode state changes.
 * <p>
 * This interface provides methods to query dark mode support and status,
 * as well as to register and unregister listeners for dark mode state changes.
 */
public interface DarkModeDetector {
    /**
     * Checks if dark mode detection is supported on the current platform.
     *
     * @return true if the platform supports detecting dark mode state, false otherwise
     */
    boolean isDarkModeDetectionSupported();

    /**
     * Checks if the application is currently in dark mode.
     *
     * @return true if the application is in dark mode, otherwise false.
     */
    boolean isDarkMode();

    /**
     * Registers a listener to observe changes in the dark mode state.
     * <p>
     * This method allows adding a listener that will be notified whenever the
     * dark mode state changes. The listener receives a Boolean parameter indicating
     * whether dark mode is active.
     *
     * @param listener the listener to be notified of dark mode state changes;
     *                 the parameter is true if dark mode is active, false otherwise
     */
    void addListener(Consumer<Boolean> listener);

    /**
     * Removes a previously registered listener for dark mode state changes.
     *
     * @param listener the Consumer to remove, which was previously registered to listen for dark mode state changes
     */
    void removeListener(Consumer<Boolean> listener);
}
