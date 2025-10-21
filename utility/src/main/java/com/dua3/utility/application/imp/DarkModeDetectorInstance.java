package com.dua3.utility.application.imp;

import com.dua3.utility.application.DarkModeDetector;

/**
 * Singleton access point for obtaining an instance of the {@link DarkModeDetector}.
 *
 * This class provides access to a default implementation of the {@link DarkModeDetector} interface.
 * This class is the fallback for pre-Java 25 codebases and returns an instance that cannot detect
 * whether dark mode is enabled on the system. For Java 25+, a system specific class is returned
 * instead.
 */
public final class DarkModeDetectorInstance {

    /**
     * Utility class constructor.
     */
    private DarkModeDetectorInstance() {
        // nothing to do
    }

    /**
     * Retrieves the instance of the {@link DarkModeDetector}.
     *
     * This method provides access to a the implementation of the {@link DarkModeDetector} interface.
     * The implementation returned by this method is system dependent and may or may not support dark mode detection.
     *
     * @return an instance of the {@link DarkModeDetector}
     */
    public static final DarkModeDetector get() {
        return DarkModeDetectorImpUnsupported.getInstance();
    }

}
