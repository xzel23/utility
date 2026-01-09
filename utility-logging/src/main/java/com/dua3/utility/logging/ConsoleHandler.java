package com.dua3.utility.logging;

import com.dua3.utility.data.Color;
import com.dua3.utility.data.Pair;
import com.dua3.utility.io.AnsiCode;
import org.jspecify.annotations.Nullable;

import java.io.PrintStream;
import java.time.Instant;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * The ConsoleHandler class is an implementation of the LogEntryHandler interface.
 * It handles log entries by writing them to the console.
 */
public final class ConsoleHandler implements LogHandler {

    private static final String NEWLINE = System.lineSeparator();

    private static final Map<LogLevel, Pair<String, String>> COLOR_MAP_COLORED = java.util.Map.of(
            LogLevel.TRACE, Pair.of(AnsiCode.italic(true), AnsiCode.reset()),
            LogLevel.DEBUG, Pair.of("", ""),
            LogLevel.INFO, Pair.of(AnsiCode.bold(true), ""),
            LogLevel.WARN, Pair.of(AnsiCode.fg(Color.ORANGERED) + AnsiCode.bold(true), AnsiCode.reset()),
            LogLevel.ERROR, Pair.of(AnsiCode.fg(Color.DARKRED) + AnsiCode.bold(true), AnsiCode.reset())
    );

    private static final Map<LogLevel, Pair<String, String>> COLOR_MAP_MONOCHROME = java.util.Map.of(
            LogLevel.TRACE, Pair.of("", ""),
            LogLevel.DEBUG, Pair.of("", ""),
            LogLevel.INFO, Pair.of("", ""),
            LogLevel.WARN, Pair.of("", ""),
            LogLevel.ERROR, Pair.of("", "")
    );

    private final String name;
    private final PrintStream out;
    private volatile LogFilter filter = LogFilter.allPass();
    private volatile Map<LogLevel, Pair<String, String>> colorMap = new EnumMap<>(LogLevel.class);
    private volatile String formatString = "%Cstart[%p] %c %marker %m %l %ex%Cend%n";
    private volatile String format = translateLog4jFormatString(formatString);

    /**
     * Set the format string.
     * @param format the format string
     */
    public void setFormat(String format) {
        this.formatString = format;
        this.format = translateLog4jFormatString(format);
    }

    /**
     * Get the format string.
     * @return the format string
     */
    public String getFormat() {
        return formatString;
    }

    private static String translateLog4jFormatString(String format) {
        return format
                .replace("%%", "\u0000") // Temporary replacement for literal %
                .replaceAll("%(-?\\d*(\\.\\d+)?)Cstart", "%1\\$$1s")
                .replaceAll("%(-?\\d*(\\.\\d+)?)p", "%2\\$$1s")
                .replaceAll("%(-?\\d*(\\.\\d+)?)level", "%2\\$$1s")
                .replaceAll("%(-?\\d*(\\.\\d+)?)c", "%3\\$$1s")
                .replaceAll("%(-?\\d*(\\.\\d+)?)logger", "%3\\$$1s")
                .replaceAll("%(-?\\d*(\\.\\d+)?)marker", "%4\\$$1s")
                .replaceAll("%(-?\\d*(\\.\\d+)?)m", "%5\\$$1s")
                .replaceAll("%(-?\\d*(\\.\\d+)?)msg", "%5\\$$1s")
                .replaceAll("%(-?\\d*(\\.\\d+)?)message", "%5\\$$1s")
                .replaceAll("%(-?\\d*(\\.\\d+)?)l", "%6\\$$1s")
                .replaceAll("%(-?\\d*(\\.\\d+)?)location", "%6\\$$1s")
                .replaceAll("%(-?\\d*(\\.\\d+)?)ex", "%7\\$$1s%8\\$s")
                .replaceAll("%(-?\\d*(\\.\\d+)?)exception", "%7\\$$1s%8\\$s")
                .replaceAll("%(-?\\d*(\\.\\d+)?)throwable", "%7\\$$1s%8\\$s")
                .replaceAll("%(-?\\d*(\\.\\d+)?)Cend", "%9\\$$1s")
                .replaceAll("%d\\{([^}]*)}", "%10\\$t$1")
                .replaceAll("%d", "%10\\$tT")
                .replace("\u0000", "%%");
    }

    /**
     * Constructs a ConsoleHandler with the specified PrintStream and colored flag.
     *
     * @param name    the name of the handler
     * @param out     the PrintStream to which log messages will be written
     * @param colored flag indicating whether to use colored brackets for different log levels
     */
    public ConsoleHandler(String name, PrintStream out, boolean colored) {
        this.name = name;
        this.out = out;
        setColored(colored);
    }

    /**
     * Constructs a ConsoleHandler with the specified PrintStream and colored flag.
     *
     * @param out     the PrintStream to which log messages will be written
     * @param colored flag indicating whether to use colored brackets for different log levels
     * @deprecated use {@link #ConsoleHandler(String, PrintStream, boolean)} instead
     */
    @Deprecated(forRemoval = true)
    public ConsoleHandler(PrintStream out, boolean colored) {
        this(ConsoleHandler.class.getSimpleName(), out, colored);
    }

    @Override
    public String name() {
        return name;
    }

    /**
     * Retrieves the PrintStream for log entries.
     * @return the PrintStream for log entries
     */
    public PrintStream getPrintStream() {
        return out;
    }

    @Override
    public void handle(Instant instant, String loggerName, LogLevel lvl, String mrk, Supplier<String> msg, String location, @Nullable Throwable t) {
        if (filter.test(instant, loggerName, lvl, mrk, msg, location, t)) {
            Pair<String, String> colorCodes = colorMap.get(lvl);
            out.format(format,
                    colorCodes.first(),
                    lvl.name(),
                    loggerName,
                    mrk,
                    msg.get(),
                    location,
                    t == null ? "" : t.getClass().getName() + ": " + t.getMessage(),
                    t == null ? "" : NEWLINE,
                    colorCodes.second(),
                    instant
            );
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
     * @param filter the LogFilter to be set as the filter for log entries
     */
    @Override
    public void setFilter(LogFilter filter) {
        this.filter = filter;
    }

    /**
     * Retrieves the filter for log entries.
     * <p>
     * This method returns the current filter that is being used to determine if a log entry should
     * be included or excluded.
     *
     * @return the LogFilter that is currently set as the filter for log entries.
     */
    @Override
    public LogFilter getFilter() {
        return filter;
    }
}
