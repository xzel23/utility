// Copyright (c) 2019 Axel Howind
// 
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.io;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collection;

import com.dua3.utility.Color;
import com.dua3.utility.lang.LangUtil;

public class AnsiCode {

    public static final String ESC_START = "\033[";
    public static final String ESC_END = "m";

    public static final char RESET = 0;
    public static final char BOLD_ON = 1;
    public static final char BOLD_OFF = 22;
    public static final char REVERSE_VIDEO_ON = 7;
    public static final char REVERSE_VIDEO_OFF = 27;
    public static final char ITALIC_ON = 3;
    public static final char ITALIC_OFF = 23;
    public static final char UNDERLINE_ON = 4;
    public static final char UNDERLINE_OFF = 24;
    public static final char STRIKE_THROUGH_ON = 9;
    public static final char STRIKE_THROUGH_OFF = 29;
    public static final char COLOR = 38;
    public static final char BACKGROUND_COLOR = 48;

    private static String byteStr(int b) {
        LangUtil.check(b>=0 && b<=255);
        return Integer.toString(b);
    }

    public static <T extends Appendable>
    void esc(T out, Collection<Character> args) throws IOException {
        if (args.isEmpty()) {
            return;
        }

        out.append(ESC_START);
        String delimiter = "";
        for (int arg: args) {
            out.append(delimiter).append(byteStr(arg));
            delimiter = ";";
        }
        out.append(ESC_END);
    }

    public static <T extends Appendable>
    void esc(T out, int... args) throws IOException {
        out.append(ESC_START);
        String delimiter = "";
        for (int arg: args) {
            out.append(delimiter).append(byteStr(arg));
            delimiter = ";";
        }
        out.append(ESC_END);
    }

    public static <T extends Appendable>
    void fg(T out, int r, int g, int b) throws IOException {
        esc(out, COLOR, 2, r, g, b);
    }

    public static <T extends Appendable>
    void bg(T out, int r, int g, int b) throws IOException {
        esc(out, BACKGROUND_COLOR, 2, r, g, b);
    }

    public static <T extends Appendable>
    void fg(T out, Color c) throws IOException {
        fg(out, c.r(), c.g(), c.b());
    }

    public static <T extends Appendable>
    void bg(T out, Color c) throws IOException {
        bg(out, c.r(), c.g(), c.b());
    }

    public static <T extends Appendable>
    void reset(T out) throws IOException {
        esc(out, RESET);
    }

    public static <T extends Appendable>
    void underline(T out, boolean on) throws IOException {
        esc(out, on ? UNDERLINE_ON : UNDERLINE_OFF);
    }

    public static <T extends Appendable>
    void reverse(T out, boolean on) throws IOException {
        esc(out, on ? REVERSE_VIDEO_ON : REVERSE_VIDEO_OFF);
    }

    public static <T extends Appendable>
    void strikeThrough(T out, boolean on) throws IOException {
        esc(out, on ? STRIKE_THROUGH_ON : STRIKE_THROUGH_OFF);
    }

    public static <T extends Appendable>
    void italic(T out, boolean on) throws IOException {
        esc(out, on ? ITALIC_ON : ITALIC_OFF);
    }

    public static String esc(int... args) {
    	StringBuilder out = new StringBuilder();
    	try {
			esc(out, args);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
    	return out.toString();
    }

    public static String fg(int r, int g, int b) {
        return esc(COLOR, 2, r, g, b);
    }


    public static String bg(int r, int g, int b) {
    	return esc(BACKGROUND_COLOR, 2, r, g, b);
    }


    public static String fg(Color c) {
        return fg(c.r(), c.g(), c.b());
    }


    public static String bg(Color c) {
        return bg(c.r(), c.g(), c.b());
    }


    public static String reset() {
    	return esc(RESET);
    }


    public static String underline(boolean on) {
    	return esc(on ? UNDERLINE_ON : UNDERLINE_OFF);
    }


    public static String reverse(boolean on) {
    	return esc(on ? REVERSE_VIDEO_ON : REVERSE_VIDEO_OFF);
    }


    public static String strikeThrough(boolean on) {
    	return esc(on ? STRIKE_THROUGH_ON : STRIKE_THROUGH_OFF);
    }


    public static String italic(boolean on) {
    	return esc(on ? ITALIC_ON : ITALIC_OFF);
    }

    private AnsiCode() {
        // utility class
    }
}
