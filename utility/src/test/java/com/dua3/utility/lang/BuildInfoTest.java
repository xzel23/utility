package com.dua3.utility.lang;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BuildInfoTest {

    @Test
    void testMajorOnly() {
        BuildInfo bi = BuildInfo.create("1.0.0", ZonedDateTime.now().toString());
        assertEquals(1, bi.version().major());
        assertEquals(0, bi.version().minor());
        assertEquals(0, bi.version().patch());
        assertEquals("", bi.version().suffix());
        assertEquals("1.0.0", bi.version().toString());
    }

    @Test
    void testMajorMinor() {
        BuildInfo bi = BuildInfo.create("1.2.0", ZonedDateTime.now().toString());
        assertEquals(1, bi.version().major());
        assertEquals(2, bi.version().minor());
        assertEquals(0, bi.version().patch());
        assertEquals("", bi.version().suffix());
        assertEquals("1.2.0", bi.version().toString());
    }

    @Test
    void testMajorMinorPatch() {
        BuildInfo bi = BuildInfo.create("1.2.3", ZonedDateTime.now().toString());
        assertEquals(1, bi.version().major());
        assertEquals(2, bi.version().minor());
        assertEquals(3, bi.version().patch());
        assertEquals("", bi.version().suffix());
        assertEquals("1.2.3", bi.version().toString());
    }

    @Test
    void testMajorMinorPatchSuffix() {
        BuildInfo bi = BuildInfo.create("1.2.3-alpha", ZonedDateTime.now().toString());
        assertEquals(1, bi.version().major());
        assertEquals(2, bi.version().minor());
        assertEquals(3, bi.version().patch());
        assertEquals("alpha", bi.version().suffix());
        assertEquals("1.2.3-alpha", bi.version().toString());
    }

    @Test
    void testMajorMinorPatchDashSuffix() {
        BuildInfo bi = BuildInfo.create("1.2.3-SNAPSHOT", ZonedDateTime.now().toString());
        assertEquals(1, bi.version().major());
        assertEquals(2, bi.version().minor());
        assertEquals(3, bi.version().patch());
        assertEquals("SNAPSHOT", bi.version().suffix());
        assertEquals("1.2.3-SNAPSHOT", bi.version().toString());
    }

    @Test
    void testCreateFromProperties() {
        Properties props = new Properties();
        props.setProperty(BuildInfo.KEY_BUILD_VERSION, "2.3.4-beta");
        props.setProperty(BuildInfo.KEY_BUILD_TIME, "2023-01-01T12:00:00Z[UTC]");

        BuildInfo bi = BuildInfo.create(props);

        assertEquals(2, bi.version().major());
        assertEquals(3, bi.version().minor());
        assertEquals(4, bi.version().patch());
        assertEquals("beta", bi.version().suffix());
        assertEquals("2.3.4-beta", bi.version().toString());

        // ZonedDateTime.toString() formats the time as "2023-01-01T12:00Z[UTC]"
        assertEquals("2023-01-01T12:00Z[UTC]", bi.buildTime().toString());
    }

    @Test
    void testDefaultPropertiesValues() {
        Properties props = new Properties();

        BuildInfo bi = BuildInfo.create(props);

        assertEquals(0, bi.version().major());
        assertEquals(0, bi.version().minor());
        assertEquals(1, bi.version().patch());
        assertEquals("SNAPSHOT", bi.version().suffix());
        assertEquals("0.0.1-SNAPSHOT", bi.version().toString());
    }

    @Test
    void testToString() {
        ZonedDateTime buildTime = ZonedDateTime.parse("2023-01-01T12:00:00Z[UTC]");
        Version version = Version.valueOf("1.2.3-beta");
        BuildInfo bi = new BuildInfo(buildTime, version);

        assertEquals("1.2.3-beta (2023-01-01T12:00Z[UTC])", bi.toString());
    }

    @Test
    void testInvalidVersionFormat() {
        assertThrows(IllegalArgumentException.class, () ->
                BuildInfo.create("1.2", ZonedDateTime.now().toString())
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
    }

    @Test
    void testCreateFromNonExistentResource() {
        // Test loading from a non-existent resource
        assertThrows(NullPointerException.class, () ->
                BuildInfo.create(BuildInfoTest.class, "non-existent.properties")
        );
    }
}
