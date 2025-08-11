package com.dua3.utility.lang;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.MissingResourceException;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BuildInfoTest {

    @Test
    void testDigestConsistency() {
        BuildInfo bi = BuildInfo.create("1.2.3", ZonedDateTime.parse("2023-01-01T12:00:00Z[UTC]"), "key123", "abc123", "test-system");
        byte[] digest1 = bi.digest();
        byte[] digest2 = bi.digest();
        assertArrayEquals(digest1, digest2, "Digest should be consistent for the same BuildInfo object.");
    }

    @Test
    void testDigestUniqueness() {
        BuildInfo bi1 = BuildInfo.create("1.2.3", ZonedDateTime.parse("2023-01-01T12:00:00Z[UTC]"), "key123", "abc123", "test-system");
        BuildInfo bi2 = BuildInfo.create("1.2.4", ZonedDateTime.parse("2023-01-01T12:00:00Z[UTC]"), "key123", "abc123", "test-system");
        byte[] digest1 = bi1.digest();
        byte[] digest2 = bi2.digest();
        assertThrows(AssertionError.class, () -> assertArrayEquals(digest1, digest2));
    }

    @Test
    void testWriteToPropertiesFile() throws IOException {
        Path tempFile = java.nio.file.Files.createTempFile("build-info-", ".properties");
        BuildInfo bi = BuildInfo.create("1.2.3", ZonedDateTime.parse("2023-01-01T12:00:00Z[UTC]"), "key123", "abc123", "test-system");

        bi.writeToPropertiesFile(tempFile);

        Properties properties = new Properties();
        try (var in = java.nio.file.Files.newInputStream(tempFile)) {
            properties.load(in);
        }

        assertEquals("1.2.3", properties.getProperty(BuildInfo.KEY_BUILD_VERSION));
        assertEquals("2023-01-01T12:00Z[UTC]", properties.getProperty(BuildInfo.KEY_BUILD_TIME));
        assertEquals("key123", properties.getProperty(BuildInfo.KEY_PUBLIC_KEY));
        assertEquals("abc123", properties.getProperty(BuildInfo.KEY_COMMIT));
        assertEquals("test-system", properties.getProperty(BuildInfo.KEY_SYSTEM));

        BuildInfo bi2 = BuildInfo.create(properties);
        assertEquals(bi, bi2);

        assertArrayEquals(bi.digest(), bi2.digest());
    }

    @Test
    void testWriteToPropertiesFileDefaultValues() throws IOException {
        Path tempFile = java.nio.file.Files.createTempFile("build-info-default-", ".properties");
        BuildInfo bi = BuildInfo.create("0.0.1-SNAPSHOT", ZonedDateTime.parse("2000-01-01T00:00Z[UTC]"), "key123");

        bi.writeToPropertiesFile(tempFile);

        Properties properties = new Properties();
        try (var in = java.nio.file.Files.newInputStream(tempFile)) {
            properties.load(in);
        }

        assertEquals("0.0.1-SNAPSHOT", properties.getProperty(BuildInfo.KEY_BUILD_VERSION));
        assertEquals("2000-01-01T00:00Z[UTC]", properties.getProperty(BuildInfo.KEY_BUILD_TIME));
        assertEquals("key123", properties.getProperty(BuildInfo.KEY_PUBLIC_KEY));
        assertEquals("", properties.getProperty(BuildInfo.KEY_COMMIT));
        assertEquals("", properties.getProperty(BuildInfo.KEY_SYSTEM));
    }

    @Test
    void testMajorOnly() {
        BuildInfo bi = BuildInfo.create("1.0.0", ZonedDateTime.now(), "key123");
        assertEquals(1, bi.version().major());
        assertEquals(0, bi.version().minor());
        assertEquals(0, bi.version().patch());
        assertEquals("", bi.version().suffix());
        assertEquals("1.0.0", bi.version().toString());
        assertEquals("key123", bi.key());
        assertEquals("", bi.commit());
        assertEquals("", bi.system());
    }

    @Test
    void testMajorMinor() {
        BuildInfo bi = BuildInfo.create("1.2.0", ZonedDateTime.now(), "key123");
        assertEquals(1, bi.version().major());
        assertEquals(2, bi.version().minor());
        assertEquals(0, bi.version().patch());
        assertEquals("", bi.version().suffix());
        assertEquals("1.2.0", bi.version().toString());
        assertEquals("key123", bi.key());
        assertEquals("", bi.commit());
        assertEquals("", bi.system());
    }

    @Test
    void testMajorMinorPatch() {
        BuildInfo bi = BuildInfo.create("1.2.3", ZonedDateTime.now(), "key123");
        assertEquals(1, bi.version().major());
        assertEquals(2, bi.version().minor());
        assertEquals(3, bi.version().patch());
        assertEquals("", bi.version().suffix());
        assertEquals("1.2.3", bi.version().toString());
        assertEquals("key123", bi.key());
        assertEquals("", bi.commit());
        assertEquals("", bi.system());
    }

    @Test
    void testMajorMinorPatchSuffix() {
        BuildInfo bi = BuildInfo.create("1.2.3-alpha", ZonedDateTime.now(), "key123");
        assertEquals(1, bi.version().major());
        assertEquals(2, bi.version().minor());
        assertEquals(3, bi.version().patch());
        assertEquals("alpha", bi.version().suffix());
        assertEquals("1.2.3-alpha", bi.version().toString());
        assertEquals("key123", bi.key());
        assertEquals("", bi.commit());
        assertEquals("", bi.system());
    }

    @Test
    void testMajorMinorPatchDashSuffix() {
        BuildInfo bi = BuildInfo.create("1.2.3-SNAPSHOT", ZonedDateTime.now(), "key123");
        assertEquals(1, bi.version().major());
        assertEquals(2, bi.version().minor());
        assertEquals(3, bi.version().patch());
        assertEquals("SNAPSHOT", bi.version().suffix());
        assertEquals("1.2.3-SNAPSHOT", bi.version().toString());
        assertEquals("key123", bi.key());
        assertEquals("", bi.commit());
        assertEquals("", bi.system());
    }

    @Test
    void testCreateWithCommitAndSystem() {
        BuildInfo bi = BuildInfo.create("1.2.3", ZonedDateTime.now(), "key123", "abc123", "test-system");
        assertEquals(1, bi.version().major());
        assertEquals(2, bi.version().minor());
        assertEquals(3, bi.version().patch());
        assertEquals("", bi.version().suffix());
        assertEquals("1.2.3", bi.version().toString());
        assertEquals("key123", bi.key());
        assertEquals("abc123", bi.commit());
        assertEquals("test-system", bi.system());
    }

    @Test
    void testCreateFromProperties() {
        Properties props = new Properties();
        props.setProperty(BuildInfo.KEY_BUILD_VERSION, "2.3.4-beta");
        props.setProperty(BuildInfo.KEY_BUILD_TIME, "2023-01-01T12:00:00Z[UTC]");
        props.setProperty(BuildInfo.KEY_PUBLIC_KEY, "key123");
        props.setProperty(BuildInfo.KEY_COMMIT, "abcdef");
        props.setProperty(BuildInfo.KEY_SYSTEM, "OS 123");

        BuildInfo bi = BuildInfo.create(props);

        assertEquals(2, bi.version().major());
        assertEquals(3, bi.version().minor());
        assertEquals(4, bi.version().patch());
        assertEquals("beta", bi.version().suffix());
        assertEquals("2.3.4-beta", bi.version().toString());
        assertEquals("key123", bi.key());
        assertEquals("abcdef", bi.commit());
        assertEquals("OS 123", bi.system());

        // ZonedDateTime.toString() formats the time as "2023-01-01T12:00Z[UTC]"
        assertEquals("2023-01-01T12:00Z[UTC]", bi.buildTime().toString());
    }

    @Test
    void testCreateFromPropertiesWithCommitAndSystem() {
        Properties props = new Properties();
        props.setProperty(BuildInfo.KEY_BUILD_VERSION, "2.3.4-beta");
        props.setProperty(BuildInfo.KEY_BUILD_TIME, "2023-01-01T12:00:00Z[UTC]");
        props.setProperty(BuildInfo.KEY_PUBLIC_KEY, "key123");
        props.setProperty(BuildInfo.KEY_COMMIT, "abc123");
        props.setProperty(BuildInfo.KEY_SYSTEM, "test-system");

        BuildInfo bi = BuildInfo.create(props);

        assertEquals(2, bi.version().major());
        assertEquals(3, bi.version().minor());
        assertEquals(4, bi.version().patch());
        assertEquals("beta", bi.version().suffix());
        assertEquals("2.3.4-beta", bi.version().toString());
        assertEquals("key123", bi.key());
        assertEquals("abc123", bi.commit());
        assertEquals("test-system", bi.system());

        // ZonedDateTime.toString() formats the time as "2023-01-01T12:00Z[UTC]"
        assertEquals("2023-01-01T12:00Z[UTC]", bi.buildTime().toString());
    }

    @Test
    void testToString() {
        ZonedDateTime buildTime = ZonedDateTime.parse("2023-01-01T12:00:00Z[UTC]");
        Version version = Version.valueOf("1.2.3-beta");
        BuildInfo bi = new BuildInfo(version, buildTime, "key123", "", "");

        assertEquals("1.2.3-beta (2023-01-01T12:00Z[UTC])", bi.toString());
    }

    @Test
    void testToStringWithCommitAndSystem() {
        ZonedDateTime buildTime = ZonedDateTime.parse("2023-01-01T12:00:00Z[UTC]");
        Version version = Version.valueOf("1.2.3-beta");
        BuildInfo bi = new BuildInfo(version, buildTime, "key123", "abc123", "test-system");

        assertEquals("1.2.3-beta (2023-01-01T12:00Z[UTC]) commit: abc123 system: test-system", bi.toString());
    }

    @Test
    void testInvalidVersionFormat() {
        assertThrows(IllegalArgumentException.class, () ->
                BuildInfo.create("1.2", ZonedDateTime.now(), "key123")
        );
    }

    @Test
    void testCreateFromResource() throws IOException {
        // Test loading from an existing resource
        BuildInfo bi = BuildInfo.create(BuildInfoTest.class, "buildinfo.properties");

        assertEquals(3, bi.version().major());
        assertEquals(4, bi.version().minor());
        assertEquals(5, bi.version().patch());
        assertEquals("test", bi.version().suffix());
        assertEquals("3.4.5-test", bi.version().toString());
        assertEquals("2023-02-02T14:30Z[UTC]", bi.buildTime().toString());
        assertEquals("key123", bi.key());
        assertEquals("abc123", bi.commit());
        assertEquals("test-system", bi.system());
    }

    @Test
    void testCreateFromNonExistentResource() {
        // Test loading from a non-existent resource
        assertThrows(MissingResourceException.class, () ->
                BuildInfo.create(BuildInfoTest.class, "non-existent.properties")
        );
    }
}