package com.dua3.utility.lang;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.IOException;
import java.io.InputStream;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.Properties;

/**
 * A record class representing build information, including build time and version components.
 *
 * @param buildTime  the build timestamp
 * @param version    the version
 */
public record BuildInfo(ZonedDateTime buildTime, Version version) {

    /**
     * Key to use for the build version in properties files.
     */
    public static final String KEY_BUILD_VERSION = "build.version";
    /**
     * Key to use for the build time in properties files.
     */
    public static final String KEY_BUILD_TIME = "build.time";

    private static final Logger LOG = LogManager.getLogger(BuildInfo.class);

    /**
     * Create from a version and timestamp
     *
     * @param versionString      the version string
     * @param zonedDateTimeBuild the timestamp, compatible with {@link ZonedDateTime#parse(CharSequence)}
     * @return BuildInfo instance
     */
    public static BuildInfo create(CharSequence versionString, CharSequence zonedDateTimeBuild) {
        Version version = Version.valueOf(versionString.toString());
        ZonedDateTime buildTime = ZonedDateTime.parse(zonedDateTimeBuild);

        BuildInfo buildInfo = new BuildInfo(buildTime, version);

        LOG.debug("BuildInfo: {}", buildInfo);

        return buildInfo;
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
        return create(version, buildTime);
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
        return "%s (%s)".formatted(version(), buildTime());
    }

}
