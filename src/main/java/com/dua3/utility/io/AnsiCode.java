package com.dua3.utility.io;

import java.io.IOException;
import java.io.UncheckedIOException;

import com.dua3.utility.Color;
import com.dua3.utility.lang.LangUtil;

public class AnsiCode {

    public static final char RESET = 0;
    public static final char REVERSE_VIDEO_ON = 7;
    public static final char REVERSE_VIDEO_OFF = 27;
    public static final char ITALIC_ON = 3;
    public static final char ITALIC_OFF = 23;
    public static final char UNDERLINE_ON = 4;
    public static final char UNDERLINE_OFF = 24;
    public static final char STRIKE_THROUGH_ON = 9;
    public static final char STRIKE_THROUGH_OFF = 29;

    private static String byteStr(int b) {
        LangUtil.check(b>=0 && b<=255);
        return Integer.toString(b);
    }

    public static <T extends Appendable>
    void esc(T out, int code, int... args) throws IOException {
        out.append("\033[");
        out.append(byteStr(code));
        for (int arg: args) {
            out.append(';').append(byteStr(arg));
        }
        out.append('m');
    }

    public static <T extends Appendable>
    void fg(T out, int r, int g, int b) throws IOException {
        esc(out, 38, 2, r, g, b);
    }

    public static <T extends Appendable>
    void bg(T out, int r, int g, int b) throws IOException {
        esc(out, 48, 2, r, g, b);
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


    
    
    public static String esc(int code, int... args) {
    	StringBuilder out = new StringBuilder();
    	try {
			esc(out, code, args);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
    	return out.toString();
    }

    public static String fg(int r, int g, int b) {
        return esc(38, 2, r, g, b);
    }


    public static String bg(int r, int g, int b) {
    	return esc(48, 2, r, g, b);
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
