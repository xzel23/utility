package com.dua3.utility.fx.controls;

/**
 * An enumeration representing modes used for displaying prompts.
 * <p>
 * This enum provides two modes for user input prompts:
 * - TEXT: Used for standard text input.
 * - PASSWORD: Used for masked input, typically for passwords or sensitive data.
 */
public enum PromptMode {
    /**
     * Represents the mode used for standard text input in a user input prompt.
     * This mode is intended for collecting non-sensitive information where visibility
     * of entered characters is preferred.
     */
    TEXT,
    /**
     * Used to represent a mode for masked input, typically for passwords or sensitive information.
     * When this mode is selected, user input is obscured for security purposes and copy and cut
     * operations are disabled.
     */
    PASSWORD
}
