package com.dua3.utility.lang;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VersionTest {

    @Test
    void testValueOf_ValidVersions() {
        // Test basic version without suffix
        Version v1 = Version.valueOf("1.2.3");
        assertEquals(1, v1.major());
        assertEquals(2, v1.minor());
        assertEquals(3, v1.patch());
        assertEquals("", v1.suffix());

        // Test version with suffix
        Version v2 = Version.valueOf("4.5.6-alpha");
        assertEquals(4, v2.major());
        assertEquals(5, v2.minor());
        assertEquals(6, v2.patch());
        assertEquals("alpha", v2.suffix());

        // Test version with complex suffix
        Version v3 = Version.valueOf("7.8.9-beta.1");
        assertEquals(7, v3.major());
        assertEquals(8, v3.minor());
        assertEquals(9, v3.patch());
        assertEquals("beta.1", v3.suffix());
    }

    @Test
    void testValueOf_InvalidVersions() {
        // Test null input
        // Note: When assertions are enabled, an AssertionError is thrown for null input
        // before our explicit null check can throw an IllegalArgumentException
        assertThrows(AssertionError.class, () -> Version.valueOf(null));

        // Test empty input
        assertThrows(IllegalArgumentException.class, () -> Version.valueOf(""));

        // Test incorrect format (too few components)
        assertThrows(IllegalArgumentException.class, () -> Version.valueOf("1.2"));

        // Test incorrect format (too many components)
        assertThrows(IllegalArgumentException.class, () -> Version.valueOf("1.2.3.4"));

        // Test non-integer components
        assertThrows(IllegalArgumentException.class, () -> Version.valueOf("a.2.3"));
        assertThrows(IllegalArgumentException.class, () -> Version.valueOf("1.b.3"));
        assertThrows(IllegalArgumentException.class, () -> Version.valueOf("1.2.c"));
    }

    @Test
    void testRoundTrip() {
        // Test that valueOf(toString()) returns the original version
        Version original = new Version(1, 2, 3, "alpha");
        String versionString = original.toString();
        Version parsed = Version.valueOf(versionString);

        assertEquals(original.major(), parsed.major());
        assertEquals(original.minor(), parsed.minor());
        assertEquals(original.patch(), parsed.patch());
        assertEquals(original.suffix(), parsed.suffix());
    }

    @Test
    void testCompareTo_DifferentMajor() {
        // Test comparing versions with different major versions
        Version v1 = new Version(1, 0, 0, "");
        Version v2 = new Version(2, 0, 0, "");

        assertTrue(v1.compareTo(v2) < 0);
        assertTrue(v2.compareTo(v1) > 0);
    }

    @Test
    void testCompareTo_DifferentMinor() {
        // Test comparing versions with same major but different minor versions
        Version v1 = new Version(1, 0, 0, "");
        Version v2 = new Version(1, 1, 0, "");

        assertTrue(v1.compareTo(v2) < 0);
        assertTrue(v2.compareTo(v1) > 0);
    }

    @Test
    void testCompareTo_DifferentPatch() {
        // Test comparing versions with same major and minor but different patch versions
        Version v1 = new Version(1, 0, 0, "");
        Version v2 = new Version(1, 0, 1, "");

        assertTrue(v1.compareTo(v2) < 0);
        assertTrue(v2.compareTo(v1) > 0);
    }

    @Test
    void testCompareTo_DifferentSuffix() {
        // Test comparing versions with same major, minor, and patch but different suffixes
        Version v1 = new Version(1, 0, 0, "alpha");
        Version v2 = new Version(1, 0, 0, "beta");

        assertTrue(v1.compareTo(v2) < 0);
        assertTrue(v2.compareTo(v1) > 0);
    }

    @Test
    void testCompareTo_WithAndWithoutSuffix() {
        // Test comparing versions with and without suffixes
        // Versions without a suffix should be considered greater
        Version v1 = new Version(1, 0, 0, "alpha");
        Version v2 = new Version(1, 0, 0, "");

        assertTrue(v1.compareTo(v2) < 0);
        assertTrue(v2.compareTo(v1) > 0);
    }

    @Test
    void testCompareTo_Equal() {
        // Test comparing equal versions
        Version v1 = new Version(1, 2, 3, "alpha");
        Version v2 = new Version(1, 2, 3, "alpha");

        assertEquals(0, v1.compareTo(v2));
        assertEquals(0, v2.compareTo(v1));
    }

    @Test
    void testCompareMajorTo_DifferentMajor() {
        // Test comparing versions with different major versions
        Version v1 = new Version(1, 9, 9, "");
        Version v2 = new Version(2, 0, 0, "");

        assertTrue(v1.compareMajorTo(v2) < 0);
        assertTrue(v2.compareMajorTo(v1) > 0);
    }

    @Test
    void testCompareMajorTo_SameMajor() {
        // Test comparing versions with same major version but different minor/patch/suffix
        Version v1 = new Version(1, 0, 0, "");
        Version v2 = new Version(1, 9, 9, "beta");

        assertEquals(0, v1.compareMajorTo(v2));
        assertEquals(0, v2.compareMajorTo(v1));
    }
}
