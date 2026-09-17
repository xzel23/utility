package com.dua3.utility.ui;

import com.dua3.utility.text.Font;
import com.dua3.utility.text.FontUtil;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VisualLineTest {

    @Test
    void testVisualLinePropertiesAndDefensiveCopy() {
        double[] boundaries = new double[]{0.0, 10.0, 25.0};
        VisualLine line = new VisualLine(0, 2, 10.0, 15.0, boundaries);

        assertEquals(0, line.start());
        assertEquals(2, line.end());
        assertEquals(10.0, line.top());
        assertEquals(15.0, line.height());
        assertEquals(2, line.length());
        assertEquals(0.0, line.minX());
        assertEquals(25.0, line.maxX());
        assertArrayEquals(new double[]{0.0, 10.0, 25.0}, line.boundaries());

        // Test mutating original array does not affect line
        boundaries[0] = 99.0;
        assertEquals(0.0, line.boundaries()[0]);
    }

    @Test
    void testVisualLineEmptyBoundaries() {
        VisualLine line = new VisualLine(5, 5, 0.0, 12.0, new double[0]);
        assertEquals(0, line.length());
        assertEquals(0.0, line.minX());
        assertEquals(0.0, line.maxX());
    }

    @Test
    void testVisualLineEqualsAndHashCode() {
        VisualLine l1 = new VisualLine(0, 2, 10.0, 15.0, new double[]{0.0, 10.0, 20.0});
        VisualLine l2 = new VisualLine(0, 2, 10.0, 15.0, new double[]{0.0, 10.0, 20.0});
        VisualLine l3 = new VisualLine(1, 2, 10.0, 15.0, new double[]{0.0, 10.0, 20.0});

        assertEquals(l1, l2);
        assertEquals(l1.hashCode(), l2.hashCode());
        assertNotEquals(l1, l3);
        assertNotEquals(null, l1);
        assertTrue(l1.toString().contains("VisualLine{start=0, end=2, top=10.0, height=15.0"));
    }

    @Test
    void testVisualLineCache() {
        Font font = FontUtil.getInstance().getDefaultFont();
        VisualLine line = new VisualLine(0, 1, 0.0, 12.0, new double[]{0.0, 10.0});
        VisualLineCache cache = new VisualLineCache(400.0, font, List.of(line));

        assertEquals(400.0, cache.widthKey());
        assertEquals(font, cache.font());
        assertEquals(List.of(line), cache.lines());
    }
}
