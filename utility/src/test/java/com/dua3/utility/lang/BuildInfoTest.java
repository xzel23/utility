package com.dua3.utility.lang;

import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

class BuildInfoTest {

    @Test
    public void testMajorOnly() {
        BuildInfo bi = BuildInfo.create("1", ZonedDateTime.now().toString());
        assertEquals(1, bi.major());
        assertEquals(0, bi.minor());
        assertEquals(0, bi.patchLevel());
        assertEquals("", bi.suffix());
        assertEquals("1.0.0", bi.version());
    }

    @Test
    public void testMajorMinor() {
        BuildInfo bi = BuildInfo.create("1.2", ZonedDateTime.now().toString());
        assertEquals(1, bi.major());
        assertEquals(2, bi.minor());
        assertEquals(0, bi.patchLevel());
        assertEquals("", bi.suffix());
        assertEquals("1.2.0", bi.version());
    }

    @Test
    public void testMajorMinorPatch() {
        BuildInfo bi = BuildInfo.create("1.2.3", ZonedDateTime.now().toString());
        assertEquals(1, bi.major());
        assertEquals(2, bi.minor());
        assertEquals(3, bi.patchLevel());
        assertEquals("", bi.suffix());
        assertEquals("1.2.3", bi.version());
    }

    @Test
    public void testMajorMinorPatchSuffix() {
        BuildInfo bi = BuildInfo.create("1.2.3a", ZonedDateTime.now().toString());
        assertEquals(1, bi.major());
        assertEquals(2, bi.minor());
        assertEquals(3, bi.patchLevel());
        assertEquals("a", bi.suffix());
        assertEquals("1.2.3a", bi.version());
    }

    @Test
    public void testMajorMinorPatchDashSuffix() {
        BuildInfo bi = BuildInfo.create("1.2.3-SNAPSHOT", ZonedDateTime.now().toString());
        assertEquals(1, bi.major());
        assertEquals(2, bi.minor());
        assertEquals(3, bi.patchLevel());
        assertEquals("a", bi.suffix());
        assertEquals("1.2.3-SNAPSHOT", bi.version());
    }
    
}
