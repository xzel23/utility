package com.dua3.utility.data;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("java:S5961")
class HSLColorTest {

    @Test
    void testValueOfAndArgbConversions() {
        // Test Red (0xFFFF0000)
        HSLColor red = HSLColor.valueOf(0xFFFF0000);
        assertEquals(0.0f, red.h(), 0.1f);
        assertEquals(1.0f, red.s(), 0.01f);
        assertEquals(0.5f, red.l(), 0.01f);
        assertEquals(1.0f, red.alpha(), 0.01f);
        assertEquals(0xFFFF0000, red.argb());

        // Test Green (0xFF00FF00)
        HSLColor green = HSLColor.valueOf(0xFF00FF00);
        assertEquals(120.0f, green.h(), 0.1f);
        assertEquals(1.0f, green.s(), 0.01f);
        assertEquals(0.5f, green.l(), 0.01f);
        assertEquals(0xFF00FF00, green.argb());

        // Test Blue (0xFF0000FF)
        HSLColor blue = HSLColor.valueOf(0xFF0000FF);
        assertEquals(240.0f, blue.h(), 0.1f);
        assertEquals(1.0f, blue.s(), 0.01f);
        assertEquals(0.5f, blue.l(), 0.01f);
        assertEquals(0xFF0000FF, blue.argb());

        // Test Gray (0xFF808080)
        HSLColor gray = HSLColor.valueOf(0xFF808080);
        assertEquals(0.0f, gray.h(), 0.1f);
        assertEquals(0.0f, gray.s(), 0.01f);
        assertEquals(0.5f, gray.l(), 0.02f);
        assertEquals(0xFF808080, gray.argb() & 0xFFFEFEFE); // slight rounding tolerance

        // Test Black (0xFF000000)
        HSLColor black = HSLColor.valueOf(0xFF000000);
        assertEquals(0.0f, black.h(), 0.1f);
        assertEquals(0.0f, black.s(), 0.01f);
        assertEquals(0.0f, black.l(), 0.01f);
        assertEquals(0xFF000000, black.argb());

        // Test White (0xFFFFFFFF)
        HSLColor white = HSLColor.valueOf(0xFFFFFFFF);
        assertEquals(0.0f, white.h(), 0.1f);
        assertEquals(0.0f, white.s(), 0.01f);
        assertEquals(1.0f, white.l(), 0.01f);
        assertEquals(0xFFFFFFFF, white.argb());

        // Test Yellow (0xFFFFFF00)
        HSLColor yellow = HSLColor.valueOf(0xFFFFFF00);
        assertEquals(60.0f, yellow.h(), 0.1f);
        assertEquals(1.0f, yellow.s(), 0.01f);
        assertEquals(0.5f, yellow.l(), 0.01f);

        // Test Cyan (0xFF00FFFF)
        HSLColor cyan = HSLColor.valueOf(0xFF00FFFF);
        assertEquals(180.0f, cyan.h(), 0.1f);
        assertEquals(1.0f, cyan.s(), 0.01f);
        assertEquals(0.5f, cyan.l(), 0.01f);

        // Test Magenta (0xFFFF00FF)
        HSLColor magenta = HSLColor.valueOf(0xFFFF00FF);
        assertEquals(300.0f, magenta.h(), 0.1f);
        assertEquals(1.0f, magenta.s(), 0.01f);
        assertEquals(0.5f, magenta.l(), 0.01f);
    }

    @Test
    void testAlphaMethods() {
        HSLColor color = new HSLColor(180, 0.5f, 0.5f, 1.0f);
        assertTrue(color.isOpaque());
        assertFalse(color.isTransparent());
        assertEquals(255, color.a());

        HSLColor halfAlpha = color.withAlpha(0.5);
        assertEquals(0.5f, halfAlpha.alpha(), 0.01f);
        assertEquals(128, halfAlpha.a());
        assertFalse(halfAlpha.isOpaque());
        assertFalse(halfAlpha.isTransparent());

        HSLColor zeroAlpha = color.withAlpha(0);
        assertTrue(zeroAlpha.isTransparent());
        assertFalse(zeroAlpha.isOpaque());

        HSLColor sameAlpha = color.withAlpha(255);
        assertSame(color, sameAlpha);

        HSLColor multiplied = color.multiplyAlpha(0.5);
        assertEquals(0.5f, multiplied.alpha(), 0.01f);
    }

    @Test
    void testTransformations() {
        HSLColor color = new HSLColor(180, 0.5f, 0.5f, 0.8f);
        assertSame(color, color.toHSLColor());

        HSVColor hsv = color.toHSVColor();
        assertNotNull(hsv);
        assertEquals(180, hsv.h(), 0.1f);

        Color brighter = color.brighter();
        assertTrue(brighter.toHSLColor().l() > color.l());

        Color darker = color.darker();
        assertTrue(darker.toHSLColor().l() < color.l());

        assertEquals(color.toCss(), color.toString());
    }
}
