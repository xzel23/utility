package com.dua3.utility.lang;

/**
 * Represents a software version with major, minor, patch components and an optional suffix.
 * This class provides functionality to parse a version string, access version components, and format
 * the version as a string.
 * <p>
 * The class implements the Comparable interface. If two versions have the same major, minor, and
 * patch fields, their suffix fields are compared. Versions without a suffix are always considered
 * bigger than versions with a suffix. This makes sure that the final version, i.e., the version
 * without a suffix, is always considered the newest one.
 * <p>
 * <strong>This means that you must not add a suffix like {@literal '-final'} to the version for the
 * release version.</strong>
 *
 * @param major the major version
 * @param minor the minor version
 * @param patch the patch level
 * @param suffix the suffix
 */
public record Version(int major, int minor, int patch, String suffix) implements Comparable<Version> {

    /**
     * Parse a version string and return a Version object.
     * The format should be "major.minor.patch" or "major.minor.patch-suffix".
     *
     * @param versionString the string to parse
     * @return the parsed Version
     * @throws IllegalArgumentException if the string cannot be parsed as a valid version
     */
    public static Version valueOf(String versionString) {
        if (versionString.isBlank()) {
            throw new IllegalArgumentException("Version string cannot be empty");
        }

        String[] parts = versionString.split("-", 2);
        String versionPart = parts[0];
        String suffix = parts.length > 1 ? parts[1] : "";

        String[] versionComponents = versionPart.split("\\.");
        if (versionComponents.length != 3) {
            throw new IllegalArgumentException("Version must be in format 'major.minor.patch' or 'major.minor.patch-suffix'");
        }

        try {
            int major = Integer.parseInt(versionComponents[0]);
            int minor = Integer.parseInt(versionComponents[1]);
            int patch = Integer.parseInt(versionComponents[2]);

            return new Version(major, minor, patch, suffix);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Version components must be valid integers", e);
        }
    }

    @Override
    public String toString() {
        return major + "." + minor + "." + patch + (suffix.isEmpty() ? "" : "-" + suffix);
    }

    /**
     * Compares the major version of this object with the major version of the specified Version object.
     *
     * @param o the Version object to compare with
     * @return a negative integer, zero, or a positive integer if the major version of this object
     *         is less than, equal to, or greater than the major version of the specified object
     */
    public int compareMajorTo(Version o) {
        return Integer.compare(major, o.major);
    }

    @Override
    public int compareTo(Version o) {
        int result = Integer.compare(major, o.major);
        if (result != 0) {
            return result;
        }

        result = Integer.compare(minor, o.minor);
        if (result != 0) {
            return result;
        }

        result = Integer.compare(patch, o.patch);
        if (result != 0) {
            return result;
        }

        result = suffix.compareTo(o.suffix);
        if (result == 0) {
            return result;
        }
        if (suffix.isEmpty()) {
            return 1;
        }
        if (o.suffix.isEmpty()) {
            return -1;
        }
        return result;
    }

    /**
     * Determines whether this version is within the range of the specified versions, inclusive.
     *
     * @param fromInclusive the lower bound of the range (inclusive)
     * @param toInclusive the upper bound of the range (inclusive)
     * @return {@code true} if this version is greater than or equal to {@code fromInclusive}
     *         and less than or equal to {@code toInclusive}, otherwise {@code false}
     */
    public boolean isBetween(Version fromInclusive, Version toInclusive) {
        return compareTo(fromInclusive) >= 0 && compareTo(toInclusive) <= 0;
    }
}
