// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.data;

import java.util.Locale;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ColorTest {

    @Test
    public void testToStringAndValueOfWithHex() {
        for (Color c : Color.values()) {
            String hex = c.toString();
            assertTrue(hex.matches("#[a-f0-9]{8}"));

            Color d = Color.valueOf(hex);
            assertEquals(c, d);
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
    public void testValueOfWithName() {
        for (Map.Entry<String, Color> entry : Color.palette().entrySet()) {
            String name = entry.getKey();
            Color expected = entry.getValue();
            Color colorByName = Color.valueOf(name);

            assertEquals(expected, colorByName);
        }
    }

    @Test
    public void testValueOfWithRgb() {
        for (Color c : Color.values()) {
            int r = c.r();
            int g = c.g();
            int b = c.b();
            int a = c.a();

            if (a == 0xFF) {
                String text = String.format(Locale.ROOT,"rgb(%d,%d,%d)", r, g, b);
                Color actual = Color.valueOf(text);
                assertEquals(c, actual);
            }
        }
    }

    @Test
    public void testValueOfWithRgba() {
        for (Color c : Color.values()) {
            int r = c.r();
            int g = c.g();
            int b = c.b();
            int a = c.a();
            String text = String.format(Locale.ROOT,"rgba(%d,%d,%d,%d)", r, g, b, a);
            Color actual = Color.valueOf(text);

            assertEquals(c, actual);
        }
    }

}
