package com.dua3.utility.lang;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.IOException;
import java.io.InputStream;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Build information record.
 */
public record BuildInfo(ZonedDateTime buildTime, int major, int minor, int patchLevel, String separator,
                        String suffix) {

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
     * @param version            the version string
     * @param zonedDateTimeBuild the timestamp, compatible with {@link ZonedDateTime#parse(CharSequence)}
     * @return BuildInfo instance
     */
    public static BuildInfo create(CharSequence version, CharSequence zonedDateTimeBuild) {
        Pattern pattern = Pattern.compile("(?<major>\\d+)(\\.(?<minor>\\d+)(\\.(?<patch>\\d+))?)?(?<separator>[-_.])?(?<suffix>\\w+)?");
        Matcher m = pattern.matcher(version);

        if (!m.matches()) {
            throw new IllegalArgumentException("Version does not match pattern: " + version);
        }

        int major = Integer.parseInt(m.group("major"));
        int minor = group(m, "minor").map(Integer::parseInt).orElse(0);
        int patch = group(m, "patch").map(Integer::parseInt).orElse(0);
        String separator = group(m, "separator").orElse("");
        String suffix = group(m, "suffix").orElse("");

        ZonedDateTime buildTime = ZonedDateTime.parse(zonedDateTimeBuild);

        BuildInfo buildInfo = new BuildInfo(buildTime, major, minor, patch, separator, suffix);

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
     * @param cls      the class used to load the properties
     * @param resource name of the resource file
     * @return BuildInfo instance
     * @throws IllegalStateException if buildinfo could not be loaded
     */
    public static BuildInfo create(Class<?> cls, String resource) throws IOException {
        try (InputStream in = cls.getResourceAsStream(resource)) {
            return create(LangUtil.loadProperties(Objects.requireNonNull(in, () -> "resource not found: " + resource)));
        }
    }

    private static Optional<String> group(Matcher m, String group) {
        return Optional.ofNullable(m.group(group));
    }

    /**
     * Get version string.
     *
     * @return the version string
     */
    public String version() {
        return "%d.%d.%d%s%s".formatted(major, minor, patchLevel, separator, suffix);
    }

    @Override
    public String toString() {
        return "%s (%s)".formatted(version(), buildTime());
    }

}
