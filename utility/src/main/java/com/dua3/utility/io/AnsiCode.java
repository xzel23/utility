// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.io;

import com.dua3.utility.data.Color;
import com.dua3.utility.data.RGBColor;

/**
 * Support for ANSI escape codes for setting text attributes on ANSI-supporting consoles.
 */
public final class AnsiCode {

    /**
     * Marker: start of ESC-sequence.
     */
    public static final String ESC_START = "\033[";
    /**
     * Marker: end of ESC-sequence.
     */
    public static final String ESC_END = "m";

    /**
     * ESC: reset.
     */
    public static final char RESET = 0;
    /**
     * ESC: enable BOLD output.
     */
    public static final char BOLD_ON = 1;
    /**
     * ESC: disable BOLD output.
     */
    public static final char BOLD_OFF = 22;
    /**
     * ESC: enable REVERSE video output.
     */
    public static final char REVERSE_VIDEO_ON = 7;
    /**
     * ESC: disable REVERSE video output.
     */
    public static final char REVERSE_VIDEO_OFF = 27;
    /**
     * ESC: enable ITALIC output.
     */
    public static final char ITALIC_ON = 3;
    /**
     * ESC: disable ITALIC output.
     */
    public static final char ITALIC_OFF = 23;
    /**
     * ESC: enable UNDERLINE output.
     */
    public static final char UNDERLINE_ON = 4;
    /**
     * ESC: disable UNDERLINE output.
     */
    public static final char UNDERLINE_OFF = 24;
    /**
     * ESC: enable STRIKETHROUGH output.
     */
    public static final char STRIKE_THROUGH_ON = 9;
    /**
     * ESC: disable STRIKETHROUGH output.
     */
    public static final char STRIKE_THROUGH_OFF = 29;
    /**
     * ESC: start color sequence.
     */
    public static final char COLOR = 38;
    /**
     * ESC: start background color sequence.
     */
    public static final char BACKGROUND_COLOR = 48;

    /**
     * Create an escape sequence.
     *
     * @param args the sequence arguments
     * @return escape sequence as a String
     */
    public static String esc(int... args) {
        StringBuilder out = new StringBuilder();
        out.append(ESC_START);
        String delimiter = "";
        for (int arg : args) {
            out.append(delimiter).append(arg);
            delimiter = ";";
        }
        out.append(ESC_END);
        return out.toString();
    }

    /**
     * Set text color.
     *
     * @param r the red component value
     * @param g the green component value
     * @param b the blue component value
     * @return the ESC string
     */
    public static String fg(int r, int g, int b) {
        return esc(COLOR, 2, r, g, b);
    }

    /**
     * Set background color.
     *
     * @param r the red component value
     * @param g the green component value
     * @param b the blue component value
     * @return the ESC string
     */
    public static String bg(int r, int g, int b) {
        return esc(BACKGROUND_COLOR, 2, r, g, b);
    }

    /**
     * Set text color.
     *
     * @param color the color
     * @return the ESC string
     */
    public static String fg(Color color) {
        RGBColor c = color.toRGBColor();
        return fg(c.r(), c.g(), c.b());
    }

    /**
     * Set background color.
     *
     * @param c the color
     * @return the ESC string
     */
    public static String bg(RGBColor c) {
        return bg(c.r(), c.g(), c.b());
    }

    /**
     * Reset.
     *
     * @return the ESC string
     */
    public static String reset() {
        return esc(RESET);
    }

    /**
     * Set underline output.
     *
     * @param on {@code true} enables, {@code false} disables
     * @return the ESC string
     */
    public static String underline(boolean on) {
        return esc(on ? UNDERLINE_ON : UNDERLINE_OFF);
    }

    /**
     * Set reverse video output.
     *
     * @param on {@code true} enables, {@code false} disables
     * @return the ESC string
     */
    public static String reverse(boolean on) {
        return esc(on ? REVERSE_VIDEO_ON : REVERSE_VIDEO_OFF);
    }

    /**
     * Set strike-through output.
     *
     * @param on {@code true} enables, {@code false} disables
     * @return the ESC string
     */
    public static String strikeThrough(boolean on) {
        return esc(on ? STRIKE_THROUGH_ON : STRIKE_THROUGH_OFF);
    }

    /**
     * Set italic output.
     *
     * @param on {@code true} enables, {@code false} disables
     * @return the ESC string
     */
    public static String italic(boolean on) {
        return esc(on ? ITALIC_ON : ITALIC_OFF);
    }

    /**
     * Set bold output.
     *
     * @param on {@code true} enables, {@code false} disables
     * @return the ESC string
     */
    public static String bold(boolean on) {
        return esc(on ? BOLD_ON : BOLD_OFF);
    }

    private AnsiCode() {
        // utility class
    }
}
