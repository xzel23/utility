package com.dua3.utility.lang;

import com.dua3.cabe.annotations.NotNull;

import java.util.Locale;
import java.util.logging.Logger;

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

    private static final Logger LOG = Logger.getLogger(Platform.class.getName());
    private static final Platform DETECTED = determinePlatform();
    
    private static Platform determinePlatform() {
        final Platform platform;
        
        String os = System.getProperty("os.name", "generic").toLowerCase(Locale.ROOT);
        if ((os.contains("mac")) || (os.contains("darwin"))) {
            platform = Platform.MACOS;
        } else if (os.contains("windows")) {
            platform = Platform.WINDOWS;
        } else if (os.contains("linux")) {
            platform = Platform.LINUX;
        } else {
            platform = Platform.UNKNOWN;
        }
        
        LOG.info(() -> "platform identified as: "+platform);
        
        return platform;
    }

    /**
     * Get the detected platform that the program runs on.
     * @return the detected platform
     */
    public static Platform getCurrentPlatform() {
        return DETECTED;
    }

    /**
     * Check if current platform is Windows.
     * @return true if currently running under a Windows operating system
     */
    public static boolean isWindows() {
        return DETECTED == WINDOWS;
    }

    /**
     * Check if current platform is Linux.
     * @return true if currently running under a Linux operating system
     */
    public static boolean isLinux() {
        return DETECTED == LINUX;
    }

    /**
     * Check if current platform is MacOS.
     * @return true if currently running under a MacOS operating system
     */
    public static boolean isMacOS() {
        return DETECTED == MACOS;
    }

    /**
     * Check if current platform is unknown.
     * @return true if currently running under an unknown operating system
     */
    public static boolean isUnknown() {
        return DETECTED == UNKNOWN;
    }
}
