package com.dua3.utility.text;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AlignmentTest {

    @Test
    void testAlignmentValues() {
        assertEquals(5, Alignment.values().length);
        assertNotNull(Alignment.valueOf("LEFT"));
        assertNotNull(Alignment.valueOf("CENTER"));
        assertNotNull(Alignment.valueOf("RIGHT"));
        assertNotNull(Alignment.valueOf("DISTRIBUTE"));
        assertNotNull(Alignment.valueOf("JUSTIFY"));
    }

    @Test
    void testVerticalAlignmentValues() {
        assertEquals(4, VerticalAlignment.values().length);
        assertNotNull(VerticalAlignment.valueOf("TOP"));
        assertNotNull(VerticalAlignment.valueOf("MIDDLE"));
        assertNotNull(VerticalAlignment.valueOf("BOTTOM"));
        assertNotNull(VerticalAlignment.valueOf("DISTRIBUTED"));
    }
}
