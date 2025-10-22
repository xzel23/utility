package com.dua3.utility.application.imp;

import com.dua3.utility.application.DarkModeDetector;
import com.dua3.utility.lang.Platform;

/**
 * Singleton access point for obtaining an instance of the {@link DarkModeDetector}.
 * <p>
 * This class provides access to a default implementation of the {@link DarkModeDetector} interface.
 * The current implementation returned by this class does not support dark mode detection and
 * always behaves as if dark mode is not supported, returning {@code false} for detection capability
 * and dark mode status.
 * <p>
 * The actual instance returned by this class is managed by the {@link DarkModeDetectorImpUnsupported} class.
 */
public final class DarkModeDetectorInstance {

    private DarkModeDetectorInstance() {}

    /**
     * Retrieves an instance of {@link DarkModeDetector} appropriate for the current platform.
     * Depending on the detected platform, the method returns an implementation tailored to
     * handle dark mode detection or a fallback implementation if the platform is unsupported.
     *
     * @return an instance of {@link DarkModeDetector} specifically suited for the current platform
     */
    public static final DarkModeDetector get() {
        return switch (Platform.currentPlatform()) {
            case MACOS -> DarkModeDetectorImpMacOs.getInstance();
            case WINDOWS -> DarkModeDetectorWindows.getInstance();
            default -> DarkModeDetectorImpUnsupported.getInstance();
        };
    }

}
