package com.dua3.utility.application.imp;

import com.dua3.utility.application.DarkModeDetector;

/**
 * Singleton access point for obtaining an instance of the {@link DarkModeDetector}.
 *
 * This class provides access to a default implementation of the {@link DarkModeDetector} interface.
 * The current implementation returned by this class does not support dark mode detection and
 * always behaves as if dark mode is not supported, returning {@code false} for detection capability
 * and dark mode status.
 *
 * The actual instance returned by this class is managed by the {@link DarkModeDetectorImpUnsupported} class.
 */
public class DarkModeDetectorInstance {

    public static final DarkModeDetector get() {
        return DarkModeDetectorImpUnsupported.getInstance();
    }

}
