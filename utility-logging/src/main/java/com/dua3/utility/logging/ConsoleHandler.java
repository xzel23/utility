package com.dua3.utility.logging;

import com.dua3.utility.data.Color;
import com.dua3.utility.data.Pair;
import com.dua3.utility.io.AnsiCode;

import java.io.PrintStream;
import java.util.EnumMap;
import java.util.Map;

/**
 * The ConsoleHandler class is an implementation of the LogEntryHandler interface.
 * It handles log entries by writing them to the console.
 */
public class ConsoleHandler implements LogEntryHandler {
    private static final String NEWLINE = "%n".formatted();
    private final PrintStream out;
    private final Map<LogLevel, Pair<String, String>> colorMap = new EnumMap<>(LogLevel.class);

    /**
     * Constructs a ConsoleHandler with the specified PrintStream and colored flag.
     *
     * @param out     the PrintStream to which log messages will be written
     * @param colored flag indicating whether to use colored brackets for different log levels
     */
    public ConsoleHandler(PrintStream out, boolean colored) {
        this.out = out;

        if (colored) {
            colorMap.put(LogLevel.TRACE, Pair.of(AnsiCode.fg(Color.DARKGRAY), AnsiCode.reset()+NEWLINE));
            colorMap.put(LogLevel.DEBUG, Pair.of(AnsiCode.fg(Color.BLACK), AnsiCode.reset()+NEWLINE));
            colorMap.put(LogLevel.INFO, Pair.of(AnsiCode.fg(Color.BLUE), AnsiCode.reset()+NEWLINE));
            colorMap.put(LogLevel.WARN, Pair.of(AnsiCode.fg(Color.ORANGERED), AnsiCode.reset()+NEWLINE));
            colorMap.put(LogLevel.ERROR, Pair.of(AnsiCode.fg(Color.DARKRED), AnsiCode.reset()+NEWLINE));
        } else {
            colorMap.put(LogLevel.TRACE, Pair.of("", NEWLINE));
            colorMap.put(LogLevel.DEBUG, Pair.of("", NEWLINE));
            colorMap.put(LogLevel.INFO, Pair.of("", NEWLINE));
            colorMap.put(LogLevel.WARN, Pair.of("", NEWLINE));
            colorMap.put(LogLevel.ERROR, Pair.of("", NEWLINE));
        }
    }

    @Override
    public void handleEntry(LogEntry entry) {
        var colors = colorMap.get(entry.level());
        out.append(entry.format(colors.first(), colors.second()));
    }
}
