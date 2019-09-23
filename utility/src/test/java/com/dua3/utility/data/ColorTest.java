// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.dua3.utility.data.Color;

public class ColorTest {

    @Test
    public void testToStringAndValueOfWithHex() {
        for (Color c : Color.values()) {
            String hex = c.toString();
            assertTrue(hex.matches("#[a-f0-9]{8}"));

            Color d = Color.valueOf(hex);
            assertEquals(c, d);
        }
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
            Color expected = c;

            int r = c.r();
            int g = c.g();
            int b = c.b();
            int a = c.a();

            if (a == 0xFF) {
                String text = String.format("rgb(%d,%d,%d)", r, g, b);
                Color actual = Color.valueOf(text);
                assertEquals(expected, actual);
            }
        }
    }

    @Test
    public void testValueOfWithRgba() {
        for (Color c : Color.values()) {
            Color expected = c;

            int r = c.r();
            int g = c.g();
            int b = c.b();
            int a = c.a();
            String text = String.format("rgba(%d,%d,%d,%d)", r, g, b, a);
            Color actual = Color.valueOf(text);

            assertEquals(expected, actual);
        }
    }

}
