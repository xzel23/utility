package com.dua3.utility.logging;

import com.dua3.utility.data.Color;
import com.dua3.utility.io.AnsiCode;

public enum LogLevel {
    TRACE(AnsiCode.fg(Color.DARKGRAY), AnsiCode.reset()),
    DEBUG(AnsiCode.fg(Color.BLACK), AnsiCode.reset()),
    INFO(AnsiCode.fg(Color.BLUE), AnsiCode.reset()),
    WARN(AnsiCode.fg(Color.ORANGERED), AnsiCode.reset()),
    ERROR(AnsiCode.fg(Color.DARKRED), AnsiCode.reset());

    LogLevel(String escStart, String escEnd) {
        this.escStart = escStart;
        this.escEnd = escEnd;
    }

    String escStart;
    String escEnd;

    String colorize(String text, boolean colored) {
        return colored ? escStart+text+escEnd : text;
    }
}
