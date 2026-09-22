// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.data;

import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ColorTest {

    @Test
    @SuppressWarnings("java:S9142")
    void testToStringAndValueOfWithHex() {
        for (Color c : Color.values()) {
            String hex = c.toCss();
            assertTrue(hex.matches("#[a-f0-9]{6,8}"));

            Color d = Color.valueOf(hex);
            assertEquals(c, d);

            assertEquals((c.isOpaque() ? 7 : 9), c.toCss().length());
        }

        // illegal char in hax
        assertThrows(IllegalArgumentException.class, () -> Color.valueOf("#1234567g"));
        // negative value
        assertThrows(IllegalArgumentException.class, () -> Color.valueOf("rgb(0,0,-1)"));
        // three components expected
        assertThrows(IllegalArgumentException.class, () -> Color.valueOf("rgb(0,0,0,0)"));
        // 256 is out of range
        assertThrows(IllegalArgumentException.class, () -> Color.valueOf("rgba(1,5,256,128)"));
        // four components expected
        assertThrows(IllegalArgumentException.class, () -> Color.valueOf("rgba(1,5,255)"));
        // gibberish text
        assertThrows(IllegalArgumentException.class, () -> Color.valueOf("foobar"));
    }

    @Test
    void testValueOfWithName() {
        for (Map.Entry<String, Color> entry : Color.palette().entrySet()) {
            String name = entry.getKey();
            Color expected = entry.getValue();
            Color colorByName = Color.valueOf(name);

            assertEquals(expected, colorByName);
        }
    }

    @Test
    void testValueOfWithRgb() {
        for (Color c : Color.values()) {
            RGBColor rgb = c.toRGBColor();

            int r = rgb.r();
            int g = rgb.g();
            int b = rgb.b();
            int a = rgb.a();

            if (a == 0xFF) {
                String text = String.format(Locale.ROOT, "rgb(%d,%d,%d)", r, g, b);
                Color actual = Color.valueOf(text);
                assertEquals(c.argb(), actual.argb());
            }
        }
    }

    @Test
    void testValueOfWithRgba() {
        for (Color c : Color.values()) {
            RGBColor rgb = c.toRGBColor();

            int r = rgb.r();
            int g = rgb.g();
            int b = rgb.b();
            int a = rgb.a();

            String text = String.format(Locale.ROOT, "rgba(%d,%d,%d,%d)", r, g, b, a);
            Color actual = Color.valueOf(text);

            assertEquals(c.argb(), actual.argb());
        }
    }

    @Test
    void testColorConversionAndLuminance() {
        Color white = Color.WHITE;
        assertEquals("#ffffffff", white.toArgb());
        assertEquals("#ffffffff", white.toRgba());
        assertEquals(1.0, white.luminance(), 0.01);
        org.junit.jupiter.api.Assertions.assertArrayEquals(new byte[]{(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff}, white.toByteArray());
        org.junit.jupiter.api.Assertions.assertArrayEquals(new byte[]{(byte) 0xff, (byte) 0xff, (byte) 0xff}, white.toByteArrayRGB());

        Color black = Color.BLACK;
        assertEquals(0.0, black.luminance(), 0.01);

        Color red = Color.RED;
        org.junit.jupiter.api.Assertions.assertNotNull(red.toHSLColor());
        org.junit.jupiter.api.Assertions.assertNotNull(red.toHSVColor());
    }

}
