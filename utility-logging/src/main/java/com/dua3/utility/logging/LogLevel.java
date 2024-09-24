package com.dua3.utility.logging;

import com.dua3.utility.data.Color;
import com.dua3.utility.io.AnsiCode;

/**
 * Enumeration representing different log levels.
 */
public enum LogLevel {
    /**
     * TRACE log level.
     */
    TRACE(AnsiCode.fg(Color.DARKGRAY), AnsiCode.reset()),
    /**
     * DEBUG log level.
     */
    DEBUG(AnsiCode.fg(Color.BLACK), AnsiCode.reset()),
    /**
     * INFO log level.
     */
    INFO(AnsiCode.fg(Color.BLUE), AnsiCode.reset()),
    /**
     * WARN log level.
     */
    WARN(AnsiCode.fg(Color.ORANGERED), AnsiCode.reset()),
    /**
     * ERROR log level.
     */
    ERROR(AnsiCode.fg(Color.DARKRED), AnsiCode.reset());

    LogLevel(String escStart, String escEnd) {
        this.escStart = escStart;
        this.escEnd = escEnd;
    }

    final String escStart;
    final String escEnd;

    /**
     * Add ASCII coloring escape sequence to a text.
     * @param text the text
     * @param colored if true, add coloring escape sequence, else return unchanged text
     * @return the text with coloring escape according to this log level applied or the unchanged text if {@code colored == false}
     */
    String colorize(String text, boolean colored) {
        return colored ? escStart + text + escEnd : text;
    }
}
