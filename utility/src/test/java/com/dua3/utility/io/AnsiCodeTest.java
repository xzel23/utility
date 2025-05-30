// Copyright (c) 2023 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.io;

import com.dua3.utility.data.Color;
import com.dua3.utility.data.RGBColor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test the AnsiCode class.
 */
class AnsiCodeTest {

    /**
     * Test the esc method.
     */
    @Test
    void testEsc() {
        assertEquals("\033[0m", AnsiCode.esc(0));
        assertEquals("\033[1m", AnsiCode.esc(1));
        assertEquals("\033[1;2;3m", AnsiCode.esc(1, 2, 3));
    }

    /**
     * Test the fg method with RGB values.
     */
    @Test
    void testFgRGB() {
        assertEquals("\033[38;2;255;0;0m", AnsiCode.fg(255, 0, 0)); // Red
        assertEquals("\033[38;2;0;255;0m", AnsiCode.fg(0, 255, 0)); // Green
        assertEquals("\033[38;2;0;0;255m", AnsiCode.fg(0, 0, 255)); // Blue
    }

    /**
     * Test the bg method with RGB values.
     */
    @Test
    void testBgRGB() {
        assertEquals("\033[48;2;255;0;0m", AnsiCode.bg(255, 0, 0)); // Red
        assertEquals("\033[48;2;0;255;0m", AnsiCode.bg(0, 255, 0)); // Green
        assertEquals("\033[48;2;0;0;255m", AnsiCode.bg(0, 0, 255)); // Blue
    }

    /**
     * Test the fg method with Color object.
     */
    @Test
    void testFgColor() {
        Color red = new RGBColor(255, 0, 0);
        Color green = new RGBColor(0, 255, 0);
        Color blue = new RGBColor(0, 0, 255);

        assertEquals("\033[38;2;255;0;0m", AnsiCode.fg(red));
        assertEquals("\033[38;2;0;255;0m", AnsiCode.fg(green));
        assertEquals("\033[38;2;0;0;255m", AnsiCode.fg(blue));
    }

    /**
     * Test the bg method with RGBColor object.
     */
    @Test
    void testBgRGBColor() {
        RGBColor red = new RGBColor(255, 0, 0);
        RGBColor green = new RGBColor(0, 255, 0);
        RGBColor blue = new RGBColor(0, 0, 255);

        assertEquals("\033[48;2;255;0;0m", AnsiCode.bg(red));
        assertEquals("\033[48;2;0;255;0m", AnsiCode.bg(green));
        assertEquals("\033[48;2;0;0;255m", AnsiCode.bg(blue));
    }

    /**
     * Test the reset method.
     */
    @Test
    void testReset() {
        assertEquals("\033[0m", AnsiCode.reset());
    }

    /**
     * Test the underline method.
     */
    @Test
    void testUnderline() {
        assertEquals("\033[4m", AnsiCode.underline(true));
        assertEquals("\033[24m", AnsiCode.underline(false));
    }

    /**
     * Test the reverse method.
     */
    @Test
    void testReverse() {
        assertEquals("\033[7m", AnsiCode.reverse(true));
        assertEquals("\033[27m", AnsiCode.reverse(false));
    }

    /**
     * Test the strikeThrough method.
     */
    @Test
    void testStrikeThrough() {
        assertEquals("\033[9m", AnsiCode.strikeThrough(true));
        assertEquals("\033[29m", AnsiCode.strikeThrough(false));
    }

    /**
     * Test the italic method.
     */
    @Test
    void testItalic() {
        assertEquals("\033[3m", AnsiCode.italic(true));
        assertEquals("\033[23m", AnsiCode.italic(false));
    }

    /**
     * Test the bold method.
     */
    @Test
    void testBold() {
        assertEquals("\033[1m", AnsiCode.bold(true));
        assertEquals("\033[22m", AnsiCode.bold(false));
    }
}
