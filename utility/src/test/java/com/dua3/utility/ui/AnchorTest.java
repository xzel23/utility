package com.dua3.utility.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AnchorTest {

    @Test
    void testHAnchor() {
        assertEquals(HAnchor.LEFT, HAnchor.valueOf("LEFT"));
        assertEquals(HAnchor.RIGHT, HAnchor.valueOf("RIGHT"));
        assertEquals(HAnchor.CENTER, HAnchor.valueOf("CENTER"));
        assertArrayEquals(new HAnchor[]{HAnchor.LEFT, HAnchor.RIGHT, HAnchor.CENTER}, HAnchor.values());
    }

    @Test
    void testVAnchor() {
        assertEquals(VAnchor.TOP, VAnchor.valueOf("TOP"));
        assertEquals(VAnchor.BOTTOM, VAnchor.valueOf("BOTTOM"));
        assertEquals(VAnchor.BASELINE, VAnchor.valueOf("BASELINE"));
        assertEquals(VAnchor.MIDDLE, VAnchor.valueOf("MIDDLE"));
        assertArrayEquals(new VAnchor[]{VAnchor.TOP, VAnchor.BOTTOM, VAnchor.BASELINE, VAnchor.MIDDLE}, VAnchor.values());
    }
}
