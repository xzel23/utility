package com.dua3.utility.lang;

/**
 * A record that stores detailed system information, including operating system details,
 * Java runtime details, and JavaFX version if available.
 *
 * @param osName        the name of the operating system
 * @param osVersion     the version of the operating system
 * @param osArch        the architecture of the operating system
 * @param javaVersion   the version of the Java runtime environment
 * @param javaVendor    the vendor of the Java runtime environment
 * @param javaHome      the installation directory of the Java runtime environment
 * @param javafxVersion the version of JavaFX, if available
 */
public record SystemInfo(
        String osName,
        String osVersion,
        String osArch,
        String javaVersion,
        String javaVendor,
        String javaHome,
        String javafxVersion
) {

    private static final String UNKNOWN = "Unknown";

    /**
     * Static method to retrieve system information and return an instance of SystemInfo.
     *
     * @return SystemInfo instance with collected system details.
     */
    public static SystemInfo getSystemInfo() {
        // Retrieve Operating System Information
        String osName = System.getProperty("os.name", UNKNOWN);
        String osVersion = System.getProperty("os.version", UNKNOWN);
        String osArch = System.getProperty("os.arch", UNKNOWN);

        // Retrieve JDK Information
        String javaVersion = System.getProperty("java.version", UNKNOWN);
        String javaVendor = System.getProperty("java.vendor", UNKNOWN);
        String javaHome = System.getProperty("java.home", UNKNOWN);

        // Retrieve JavaFX version (if available)
        String javafxVersion = System.getProperty("javafx.runtime.version", UNKNOWN);

        // Return a SystemInfo instance with retrieved values
        return new SystemInfo(osName, osVersion, osArch, javaVersion, javaVendor, javaHome, javafxVersion);
    }

    /**
     * Formats and returns a string containing detailed system information.
     *
     * @return a formatted string representation of the operating system details, 
     *         Java runtime details, and JavaFX version.
     */
    public String formatted() {
        return String.format("""
                        OS: %s %s (%s)
                        Java: %s %s (%s)
                        JavaFX: %s
                        """,
                osName, osVersion, osArch,
                javaVersion, javaVendor, javaHome,
                javafxVersion
        );
    }
}
