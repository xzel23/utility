package com.dua3.utility.lang;

import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Build information record.
 */
public record BuildInfo(ZonedDateTime buildTime, int major, int minor, int patchLevel, String suffix) {

    private static final Logger LOG = Logger.getLogger(BuildInfo.class.getName());
    
    /**
     * Key to use for the build version in properties files.
     */
    public static final String KEY_BUILD_VERSION = "build.version";

    /**
     * Key to use for the build time in properties files.
     */
    public static final String KEY_BUILD_TIME = "build.time";

    /**
     * Create from version and timestamp
     * @param version the version string
     * @param zonedDateTimeBuild the timestamp, compatible with {@link ZonedDateTime#parse(CharSequence)}
     * @return BuildInfo instance
     */
    public static @NotNull BuildInfo create(@NotNull String version, @NotNull String zonedDateTimeBuild) {
        Pattern pattern = Pattern.compile("(?<major>\\d+)(\\.(?<minor>\\d+)(\\.(?<patch>\\d+))?)?(-(?<suffix>\\w+))?");
        Matcher m = pattern.matcher(version);
        
        if (!m.matches()) {
            throw new IllegalArgumentException("Version does not match pattern: "+version);
        }
        
        int major = Integer.parseInt(m.group("major"));
        int minor = group(m, "minor").map(Integer::parseInt).orElse(0);
        int patch = group(m, "patch").map(Integer::parseInt).orElse(0);
        String suffix = group(m, "suffix").orElse("");

        ZonedDateTime buildTime = ZonedDateTime.parse(zonedDateTimeBuild);

        BuildInfo buildInfo = new BuildInfo(buildTime, major, minor, patch, suffix);
        
        LOG.fine(() -> "BuildInfo: "+buildInfo);
        
        return buildInfo;
    }

    /**
     * Create instance from properties.
     * @param properties the properties instance; use keys as described above
     * @return BuildInfo instance
     */
    public static @NotNull BuildInfo create(@NotNull Properties properties) {
        String version = properties.getProperty(KEY_BUILD_VERSION, "0.0.1-SNAPSHOT");
        String buildTime = properties.getProperty(KEY_BUILD_TIME, "2000-01-01T00:00Z[UTC]");
        return create(version, buildTime);        
    }

    /**
     * Load properties from stream and return BuildInfo.
     * @param in {@link InputStream} to read the build info properties from
     * @return BuildInfo instance
     * @throws IllegalStateException if buildinfo could not be loaded
     */
    public static @NotNull BuildInfo create(@NotNull InputStream in) {
        try (in) {
            return create(LangUtil.loadProperties(in));
        } catch (Exception e) {
            LOG.log(Level.WARNING, "could not load build properties", e);
            throw new IllegalStateException("could not load build properties", e);
        }
    }

    /**
     * Load properties from resource and return BuildInfo.
     * @param cls the class used to load the properties
     * @param resource name of the resource file
     * @return BuildInfo instance
     * @throws IllegalStateException if buildinfo could not be loaded
     */
    public static @NotNull BuildInfo create(@NotNull Class<?> cls, @NotNull String resource) {
        return create(cls.getResourceAsStream(resource));
    }
    
    /**
     * Get version string. 
     * @return the version string
     */
    public @NotNull String version() {
        return "%d.%d.%d%s".formatted(major, minor, patchLevel, !suffix.isEmpty() ? "-"+suffix : "");
    }

    @Override
    public @NotNull String toString() {
        return "%s (%s)".formatted(version(), buildTime());
    }

    private static @NotNull Optional<String> group(@NotNull Matcher m, @NotNull String group) {
        return Optional.ofNullable(m.group(group));
    }

}
