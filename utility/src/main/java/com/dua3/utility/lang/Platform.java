package com.dua3.utility.lang;

import java.util.Locale;

/**
 * Enumeration for the different Platforms/Operating systems.
 */
public enum Platform {
    /** The Linux operating system. */
    LINUX,
    /** The macOS operating system. */
    MACOS,
    /** The Windows operating system. */
    WINDOWS,
    /** Unknown operating system. */
    UNKNOWN;

    private static final Platform detected = determinePlatform();
    
    private static Platform determinePlatform() {
        String os = System.getProperty("os.name", "generic").toLowerCase(Locale.ROOT);
        if ((os.contains("mac")) || (os.contains("darwin"))) {
            return Platform.MACOS;
        }
        if (os.contains("windows")) {
            return Platform.WINDOWS;
        }
        if (os.contains("linux")) {
            return Platform.LINUX;
        }
        return Platform.UNKNOWN;
    }

    /**
     * Get the detected platform that the program runs on.
     * @return the detected platform
     */
    public static Platform getCurrentPlatform() {
        return detected;
    }

    /**
     * Check if current platform is Windows.
     * @return true if currently running under a Windows operating system
     */
    public static boolean isWindows() {
        return detected==WINDOWS;
    }

    /**
     * Check if current platform is Linux.
     * @return true if currently running under a Linux operating system
     */
    public static boolean isLinux() {
        return detected==LINUX;
    }

    /**
     * Check if current platform is MacOS.
     * @return true if currently running under a MacOS operating system
     */
    public static boolean isMacOS() {
        return detected==MACOS;
    }

    /**
     * Check if current platform is unknown.
     * @return true if currently running under an unknown operating system
     */
    public static boolean isUnknown() {
        return detected==UNKNOWN;
    }
}
