package com.dua3.utility.lang;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.ZonedDateTime;
import java.util.Properties;

/**
 * A record class representing build information, including build time and version components.
 *
 * @param version   the version
 * @param buildTime the build timestamp
 * @param key       the key associated with the build information
 * @param commit    the commit information
 * @param system    the system information
 */
public record BuildInfo(Version version, ZonedDateTime buildTime, String key, String commit, String system) {

    /**
     * Canonical record constructor.
     *
     * @param version   the version
     * @param buildTime the build timestamp
     * @param key       the key associated with the build information
     * @param commit    the commit information
     * @param system    the system information
     */
    public BuildInfo {
        LOG.debug("BuildInfo: {}", this);
    }

    /**
     * Key to use for the build version in properties files.
     */
    public static final String KEY_BUILD_VERSION = "build.version";
    /**
     * Key to use for the build time in properties files.
     */
    public static final String KEY_BUILD_TIME = "build.time";
    /**
     * Key to use for the key in properties files.
     */
    public static final String KEY_PUBLIC_KEY = "build.key";
    /**
     * Key to use for the commit in properties files.
     */
    public static final String KEY_COMMIT = "build.commit";
    /**
     * Key to use for the system in properties files.
     */
    public static final String KEY_SYSTEM = "build.system";

    private static final Logger LOG = LogManager.getLogger(BuildInfo.class);

    /**
     * Creates a new {@code BuildInfo} instance using the given version string, key, and commit.
     * The current timestamp and system information are automatically generated.
     * <p>
     * <strong>Note:</strong> This method is intended to be called from the build script.
     *
     * @param versionString the version string to be parsed into a {@code Version} object.
     *                      The format should be "major.minor.patch" or "major.minor.patch-suffix".
     * @param key           the key associated with the build information.
     * @param commit        the commit information associated with the build.
     * @return a new {@code BuildInfo} instance containing the parsed version, current timestamp,
     * the provided key, the provided commit, and the current system information.
     * @throws IllegalArgumentException if the version string is invalid.
     */
    public static BuildInfo create(String versionString, String key, String commit) {
        return new BuildInfo(
                Version.valueOf(versionString),
                ZonedDateTime.now(),
                key,
                commit,
                SystemInfo.getSystemInfo().getOsInfo()
        );
    }

    /**
     * Creates a new instance of {@code BuildInfo} using the provided version string, build timestamp, key, commit, and system.
     *
     * @param versionString the version string to parse into a {@code Version} object, in the format "major.minor.patch" or "major.minor.patch-suffix"
     * @param buildTime     the build time and date
     * @param key           the key associated with the build information
     * @param commit        the commit information
     * @param system        the system information
     * @return a new {@code BuildInfo} instance containing the build time, version, key, commit, and system
     * @throws IllegalArgumentException if the version string is invalid
     */
    public static BuildInfo create(String versionString, ZonedDateTime buildTime, String key, String commit, String system) {
        Version version = Version.valueOf(versionString);
        return new BuildInfo(version, buildTime, key, commit, system);
    }

    /**
     * Creates a new instance of {@code BuildInfo} using the provided version string, build timestamp, and key.
     * Uses empty strings for commit and system.
     *
     * @param versionString the version string to parse into a {@code Version} object, in the format "major.minor.patch" or "major.minor.patch-suffix"
     * @param buildTime     the build timestamp
     * @param key           the key associated with the build information
     * @return a new {@code BuildInfo} instance containing the parsed build time, version, and key
     * @throws IllegalArgumentException if the version string is invalid
     */
    public static BuildInfo create(String versionString, ZonedDateTime buildTime, String key) {
        return create(versionString, buildTime, key, "", "");
    }

    /**
     * Create an instance from properties.
     * <p>
     * The following keys are required:
     * <ul>
     * <li>"build.version" - The version number in format major.minor.patch or major.minor.patch-suffix
     * <li>"build.time" - The build timestamp
     * <li>"build.key" - The public key associated with this build
     * <li>"build.commit" - The version control commit identifier
     * <li>"build.system" - The system information where the build was created
     * </ul>
     *
     * @param properties the properties instance; use keys as described above
     * @return BuildInfo instance
     * @throws IllegalStateException if the BuildInfo instance cannot be created from the supplied properties
     */
    public static BuildInfo create(Properties properties) {
        String version = requireProperty(properties, KEY_BUILD_VERSION);
        String buildTime = requireProperty(properties, KEY_BUILD_TIME);
        String key = requireProperty(properties, KEY_PUBLIC_KEY);
        String commit = requireProperty(properties, KEY_COMMIT);
        String system = requireProperty(properties, KEY_SYSTEM);
        return create(version, ZonedDateTime.parse(buildTime), key, commit, system);
    }

    /**
     * Retrieves the value of a specified property from the provided {@code Properties} object.
     * Throws an {@code IllegalStateException} if the property with the given key is not found.
     *
     * @param properties the {@code Properties} object from which the property value is to be retrieved
     * @param key        the key of the property to be retrieved
     * @return the value of the property associated with the specified key
     * @throws IllegalStateException if the specified property key is not found in the {@code Properties} object
     */
    private static String requireProperty(Properties properties, String key) {
        String value = properties.getProperty(key);
        if (value == null) {
            throw new IllegalStateException("missing required property: " + key);
        }
        return value;
    }

    /**
     * Load properties from a resource and return BuildInfo.
     *
     * @param cls      the class used to load the properties; typically the class whose package
     *                 contains the resource
     * @param resource the name of the resource file to load the build information from
     * @return BuildInfo instance
     * @throws java.util.MissingResourceException  if the resource can not be found
     * @throws IOException                         if the BuildInfo could not be loaded due to an I/O error
     * @throws IllegalStateException               if the BuildInfo could not be loaded due an invalid resource
     */
    public static BuildInfo create(Class<?> cls, String resource) throws IOException {
        try (InputStream in = LangUtil.getResourceURL(cls, resource).openStream()) {
            return create(LangUtil.loadProperties(in));
        }
    }

    /**
     * Generates a string representation of the {@code BuildInfo} object.
     * The representation includes version and build time in the format "version (buildTime)".
     * Optional fields, such as commit and system, are included if they are not blank.
     *
     * @return a string representing the build information, including the version, build time,
     * and optionally the commit and system details if provided.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("%s (%s)".formatted(version(), buildTime()));

        if (!commit.isBlank()) {
            sb.append(" commit: ").append(commit);
        }

        if (!system.isBlank()) {
            sb.append(" system: ").append(system);
        }

        return sb.toString();
    }

    /**
     * Computes the SHA-256 hash of the concatenated string representation of all fields of this instance.
     *
     * @return a byte array containing the SHA-256 digest of the string representation of this {@code BuildInfo} object
     * @throws IllegalStateException if the SHA-256 algorithm is not available
     */
    public byte[] digest() {
        try {
            String s = """
                    version: %s
                    build time: %s
                    key: %s
                    commit: %s
                    system: %s
                    """
                    .formatted(
                            version(),
                            buildTime(),
                            key(),
                            commit(),
                            system()
                    );
            return MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Writes the build information as properties to the specified file destination.
     * Each field of the build information, such as version, build time, key, commit, and system,
     * is written as a key-value pair into the generated properties file.
     *
     * @param destination the path where the properties file should be written
     * @throws IOException if an I/O error occurs while writing to the specified destination
     */
    public void writeToPropertiesFile(Path destination) throws IOException {
        Properties properties = new Properties();
        properties.setProperty(KEY_BUILD_VERSION, version().toString());
        properties.setProperty(KEY_BUILD_TIME, buildTime().toString());
        properties.setProperty(KEY_PUBLIC_KEY, key());
        properties.setProperty(KEY_COMMIT, commit());
        properties.setProperty(KEY_SYSTEM, system());

        try (var out = java.nio.file.Files.newOutputStream(destination)) {
            properties.store(out, null);
        }
    }
}
