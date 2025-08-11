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
import java.util.Objects;
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

    public static BuildInfo create(String versionString, String key, String commit) {
        BuildInfo buildInfo = new BuildInfo(
                Version.valueOf(versionString),
                ZonedDateTime.now(),
                key,
                commit,
                SystemInfo.getSystemInfo().getOsInfo()
        );

        LOG.debug("BuildInfo: {}", buildInfo);

        return buildInfo;
    }

    /**
     * Creates a new instance of {@code BuildInfo} using the provided version string, build timestamp, key, commit, and system.
     *
     * @param versionString      the version string to parse into a {@code Version} object, in the format "major.minor.patch" or "major.minor.patch-suffix"
     * @param buildTime          the build time and date
     * @param key                the key associated with the build information
     * @param commit             the commit information
     * @param system             the system information
     * @return a new {@code BuildInfo} instance containing the parsed build time, version, key, commit, and system
     * @throws IllegalArgumentException if the version string is invalid or the build timestamp cannot be parsed
     */
    public static BuildInfo create(String versionString, ZonedDateTime buildTime, String key, String commit, String system) {
        Version version = Version.valueOf(versionString);

        BuildInfo buildInfo = new BuildInfo(version, buildTime, key, commit, system);

        LOG.debug("BuildInfo: {}", buildInfo);

        return buildInfo;
    }
    
    /**
     * Creates a new instance of {@code BuildInfo} using the provided version string, build timestamp, and key.
     * Uses empty strings for commit and system.
     *
     * @param versionString      the version string to parse into a {@code Version} object, in the format "major.minor.patch" or "major.minor.patch-suffix"
     * @param buildTime the string representing the build timestamp, to be parsed as a {@code ZonedDateTime}
     * @param key                the key associated with the build information
     * @return a new {@code BuildInfo} instance containing the parsed build time, version, and key
     * @throws IllegalArgumentException if the version string is invalid or the build timestamp cannot be parsed
     */
    public static BuildInfo create(String versionString, ZonedDateTime buildTime, String key) {
        return create(versionString, buildTime, key, "", "");
    }

    /**
     * Create instance from properties.
     *
     * @param properties the properties instance; use keys as described above
     * @return BuildInfo instance
     */
    public static BuildInfo create(Properties properties) {
        String version = properties.getProperty(KEY_BUILD_VERSION, "0.0.1-SNAPSHOT");
        String buildTime = properties.getProperty(KEY_BUILD_TIME, "2000-01-01T00:00Z[UTC]");
        String key = Objects.requireNonNull(properties.getProperty(KEY_PUBLIC_KEY), "missing public key ('" + KEY_PUBLIC_KEY + "')");
        String commit = properties.getProperty(KEY_COMMIT, "");
        String system = properties.getProperty(KEY_SYSTEM, "");
        return create(version, ZonedDateTime.parse(buildTime), key, commit, system);
    }

    /**
     * Load properties from resource and return BuildInfo.
     *
     * @param cls      the class used to load the properties; typically the class whose package
     *                 contains the resource
     * @param resource the name of the resource file to load the build information from
     * @return BuildInfo instance
     * @throws IOException if buildinfo could not be loaded due to an I/O error
     * @throws IllegalStateException if buildinfo could not be loaded due an invalid resource
     */
    public static BuildInfo create(Class<?> cls, String resource) throws IOException {
        try (InputStream in = cls.getResourceAsStream(resource)) {
            return create(LangUtil.loadProperties(Objects.requireNonNull(in, () -> "resource not found: " + resource)));
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("%s (%s)".formatted(version(), buildTime()));
        
        if (commit != null && !commit.isEmpty()) {
            sb.append(" commit: ").append(commit);
        }
        
        if (system != null && !system.isEmpty()) {
            sb.append(" system: ").append(system);
        }
        
        return sb.toString();
    }

    /**
     * Computes the SHA-256 hash of the concatenated string representation of the version and build time.
     *
     * @return a byte array containing the SHA-256 digest of the string representation of this {@code BuildInfo} object
     * @throws IllegalStateException if the SHA-256 algorithm is not available
     */
    public byte[] digest() {
        try {
            return MessageDigest.getInstance("SHA-256").digest(toString().getBytes(StandardCharsets.UTF_8));
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
