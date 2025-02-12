package com.dua3.utility.lang;

public record SystemInfo(
        String osName,
        String osVersion,
        String osArch,
        String javaVersion,
        String javaVendor,
        String javaHome,
        String javafxVersion
) {

    /**
     * Static method to retrieve system information and return an instance of SystemInfo.
     *
     * @return SystemInfo instance with collected system details.
     */
    public static SystemInfo getSystemInfo() {
        // Retrieve Operating System Information
        String osName = System.getProperty("os.name", "Unknown");
        String osVersion = System.getProperty("os.version", "Unknown");
        String osArch = System.getProperty("os.arch", "Unknown");

        // Retrieve JDK Information
        String javaVersion = System.getProperty("java.version", "Unknown");
        String javaVendor = System.getProperty("java.vendor", "Unknown");
        String javaHome = System.getProperty("java.home", "Unknown");

        // Retrieve JavaFX version (if available)
        String javafxVersion = System.getProperty("javafx.runtime.version", "Unknown");

        // Return a SystemInfo instance with retrieved values
        return new SystemInfo(osName, osVersion, osArch, javaVersion, javaVendor, javaHome, javafxVersion);
    }

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
