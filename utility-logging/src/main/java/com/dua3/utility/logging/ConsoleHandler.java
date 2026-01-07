package com.dua3.utility.logging;

import com.dua3.utility.data.Color;
import com.dua3.utility.data.Pair;
import com.dua3.utility.io.AnsiCode;
import org.jspecify.annotations.Nullable;

import java.io.PrintStream;
import java.time.Instant;
import java.util.EnumMap;
import java.util.Map;

/**
 * The ConsoleHandler class is an implementation of the LogEntryHandler interface.
 * It handles log entries by writing them to the console.
 */
public final class ConsoleHandler implements LogEntryHandler {

    private static final String NEWLINE = System.lineSeparator();

    private static final Map<LogLevel, Pair<String, String>> COLOR_MAP_COLORED = java.util.Map.of(
            LogLevel.TRACE, Pair.of(AnsiCode.italic(true), AnsiCode.reset() + NEWLINE),
            LogLevel.DEBUG, Pair.of("", NEWLINE),
            LogLevel.INFO, Pair.of(AnsiCode.bold(true), NEWLINE),
            LogLevel.WARN, Pair.of(AnsiCode.fg(Color.ORANGERED) + AnsiCode.bold(true), AnsiCode.reset() + NEWLINE),
            LogLevel.ERROR, Pair.of(AnsiCode.fg(Color.DARKRED) + AnsiCode.bold(true), AnsiCode.reset() + NEWLINE)
    );

    private static final Map<LogLevel, Pair<String, String>> COLOR_MAP_MONOCHROME = java.util.Map.of(
            LogLevel.TRACE, Pair.of("", NEWLINE),
            LogLevel.DEBUG, Pair.of("", NEWLINE),
            LogLevel.INFO, Pair.of("", NEWLINE),
            LogLevel.WARN, Pair.of("", NEWLINE),
            LogLevel.ERROR, Pair.of("", NEWLINE)
    );

    private final PrintStream out;
    private volatile LogEntryFilter filter = LogEntryFilter.allPass();
    private volatile Map<LogLevel, Pair<String, String>> colorMap = new EnumMap<>(LogLevel.class);

    /**
     * Constructs a ConsoleHandler with the specified PrintStream and colored flag.
     *
     * @param out     the PrintStream to which log messages will be written
     * @param colored flag indicating whether to use colored brackets for different log levels
     */
    public ConsoleHandler(PrintStream out, boolean colored) {
        this.out = out;
        setColored(colored);
    }

    @Override
    public void handle(Instant instant, String loggerName, LogLevel lvl, String mrk, String msg, String location, @Nullable Throwable t) {
        if (filter.test(instant, loggerName, lvl, mrk, msg, location, t)) {
            Pair<String, String> colorCodes = colorMap.get(lvl);
            out.format("%s[%s] %s %s %s %s %s%s%n",
                    colorCodes.first(),
                    lvl.name(),
                    loggerName,
                    mrk,
                    msg,
                    t == null ? "" : t.getMessage(),
                    t == null ? "" : NEWLINE,
                    colorCodes.second()
            );
            LogEntryHandler.super.handle(instant, loggerName, lvl, mrk, msg, location, t);
        }
    }

    @Override
    @Deprecated
    public void handleEntry(LogEntry entry) {
        if (filter.test(entry)) {
            var colors = colorMap.get(entry.level());
            out.append(entry.format(colors.first(), colors.second()));
        }
    }

    /**
     * Enable/Disable colored output using ANSI codes.
     * @param colored true, if output use colors
     */
    public void setColored(boolean colored) {
        colorMap = colored ? COLOR_MAP_COLORED : COLOR_MAP_MONOCHROME;
    }

    /**
     * Check if colored output is enabled.
     * @return true, if colored output is enabled
     */
    public boolean isColored() {
        return colorMap == COLOR_MAP_COLORED;
    }

    /**
     * Sets the filter for log entries.
     *
     * @param filter the LogEntryFilter to be set as the filter for log entries
     */
    public void setFilter(LogEntryFilter filter) {
        this.filter = filter;
    }

    /**
     * Retrieves the filter for log entries.
     * <p>
     * This method returns the current filter that is being used to determine if a log entry should
     * be included or excluded.
     *
     * @return the LogEntryFilter that is currently set as the filter for log entries.
     */
    public LogEntryFilter getFilter() {
        return filter;
    }
}
