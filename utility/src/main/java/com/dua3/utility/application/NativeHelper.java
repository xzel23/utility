package com.dua3.utility.application;

/**
 * Interface for native helper methods.
 */
public interface NativeHelper {

    /**
     * Sets the window decorations mode for the application's user interface.
     *
     * @param dark a boolean flag indicating whether to use dark mode for window decorations.
     * @return {@code true} if successful, {@code false} otherwise
     */
    boolean setWindowDecorations(boolean dark);
}
