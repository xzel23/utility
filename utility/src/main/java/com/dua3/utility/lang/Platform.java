package com.dua3.utility.lang;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Enumeration for the different Platforms/Operating systems.
 */
public enum Platform {
    /**
     * The Linux operating system.
     */
    LINUX,
    /**
     * The macOS operating system.
     */
    MACOS,
    /**
     * The Windows operating system.
     */
    WINDOWS {
        @Override
        public boolean isProcessBuilderQuotingNeeded(String s) {
            int len = s.length();
            if (len == 0) {
                // empty string has to be quoted on Windows
                return true;
            }

            for (int i = 0; i < len; i++) {
                switch (s.charAt(i)) {
                    case ' ', '\t', '\\', '"' -> {
                        return true;
                    }
                    default -> { /* do nothing */ }
                }
            }
            return false;
        }

        private static final Pattern PATTERN_DOUBLE_QUOTE = Pattern.compile("(\\\\*)\"");
        private static final Pattern PATTERN_EOS = Pattern.compile("(\\\\*)\\z");

        @Override
        public String quoteProcessBuilderArg(String s) {
            if (!isProcessBuilderQuotingNeeded(s)) {
                return s;
            }

            s = PATTERN_DOUBLE_QUOTE.matcher(s).replaceAll("$1$1\\\\\"");
            s = PATTERN_EOS.matcher(s).replaceAll("$1$1");
            return "\"" + s + "\"";
        }
    },
    /**
     * Unknown operating system.
     */
    UNKNOWN;

    private static final Logger LOG = LogManager.getLogger(Platform.class);
    private static final Platform DETECTED = determinePlatform();

    private static Platform determinePlatform() {
        final Platform platform;

        String os = System.getProperty("os.name", "generic").toLowerCase(Locale.ROOT);
        if ((os.contains("mac")) || (os.contains("darwin"))) {
            platform = MACOS;
        } else if (os.contains("windows")) {
            platform = WINDOWS;
        } else if (os.contains("linux")) {
            platform = LINUX;
        } else {
            platform = UNKNOWN;
        }

        LOG.debug("platform identified as: {}", platform);

        return platform;
    }

    /**
     * Get the detected platform that the program runs on.
     *
     * @return the detected platform
     */
    public static Platform currentPlatform() {
        return DETECTED;
    }

    /**
     * Check if current platform is Windows.
     *
     * @return true if currently running under a Windows operating system
     */
    public static boolean isWindows() {
        return DETECTED == WINDOWS;
    }

    /**
     * Check if current platform is Linux.
     *
     * @return true if currently running under a Linux operating system
     */
    public static boolean isLinux() {
        return DETECTED == LINUX;
    }

    /**
     * Check if current platform is macOS.
     *
     * @return true if currently running under a macOS operating system
     */
    public static boolean isMacOS() {
        return DETECTED == MACOS;
    }

    /**
     * Check if current platform is unknown.
     *
     * @return true if currently running under an unknown operating system
     */
    public static boolean isUnknown() {
        return DETECTED == UNKNOWN;
    }

    /**
     * Check if argument needs to be quoted before passing to {@link ProcessBuilder}.
     *
     * @param s the argument
     * @return true, if s needs to be quoted
     */
    public boolean isProcessBuilderQuotingNeeded(String s) {
        return false;
    }

    /**
     * Quote argument for {@link ProcessBuilder}.
     *
     * @param s the argument
     * @return quoted version of s if quoting needed, otherwise s
     */
    public String quoteProcessBuilderArg(String s) {
        return s;
    }

}
