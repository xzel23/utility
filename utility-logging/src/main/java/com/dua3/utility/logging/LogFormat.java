package com.dua3.utility.logging;

import com.dua3.utility.data.Pair;
import org.jspecify.annotations.Nullable;

import java.io.PrintStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The LogFormat class handles the formatting of log entries using Log4J-style format strings.
 */
public class LogFormat {

    private static final String NEWLINE = System.lineSeparator();
    private static final ZoneId ZONE_ID = ZoneId.systemDefault();

    private static final ThreadLocal<StringBuilder> SB_THREAD_LOCAL = ThreadLocal.withInitial(() -> new StringBuilder(256));

    private interface LogFormatEntry {
        void format(StringBuilder sb, Instant instant, String loggerName, LogLevel lvl, String mrk, Supplier<String> msg, String location, @Nullable Throwable t, Pair<String, String> colorCodes);
        String getLog4jFormat();
    }

    private abstract static class AbstractLogFormatEntry implements LogFormatEntry {
        protected final String prefix;
        protected final int minWidth;
        protected final int maxWidth;
        protected final boolean leftAlign;

        protected AbstractLogFormatEntry(String prefix, int minWidth, int maxWidth, boolean leftAlign) {
            this.prefix = prefix;
            this.minWidth = minWidth;
            this.maxWidth = maxWidth;
            this.leftAlign = leftAlign;
        }

        private static final int N_SPACES = 20;
        private static final String SPACES = " ".repeat(N_SPACES);
        private static void appendSpaces(StringBuilder sb, int n) {
            while (n > 0) {
                int count = Math.min(n, SPACES.length());
                sb.append(SPACES, 0, count);
                n -= count;
            }
        }

        protected void appendFormatted(StringBuilder sb, @Nullable String value) {
            if (value == null) {
                value = "";
            }

            if (maxWidth > 0 && value.length() > maxWidth) {
                sb.append(value, 0, maxWidth);
                return;
            }

            if (value.length() < minWidth) {
                int padding = minWidth - value.length();
                if (leftAlign) {
                    sb.append(value);
                    appendSpaces(sb, padding);
                } else {
                    appendSpaces(sb, padding);
                    sb.append(value);
                }
            } else {
                sb.append(value);
            }
        }

        @Override
        public String getLog4jFormat() {
            if (minWidth == 0 && maxWidth == 0) {
                return "%" + prefix;
            }
            StringBuilder sb = new StringBuilder("%");
            if (leftAlign) sb.append("-");
            if (minWidth > 0) sb.append(minWidth);
            if (maxWidth > 0) sb.append(".").append(maxWidth);
            sb.append(prefix);
            return sb.toString();
        }
    }

    private static class LiteralEntry implements LogFormatEntry {
        private final String literal;

        LiteralEntry(String literal) {
            this.literal = literal;
        }

        @Override
        public void format(StringBuilder sb, Instant instant, String loggerName, LogLevel lvl, String mrk, Supplier<String> msg, String location, @Nullable Throwable t, Pair<String, String> colorCodes) {
            sb.append(literal);
        }

        @Override
        public String getLog4jFormat() {
            return literal.replace("%", "%%");
        }
    }

    private static class LevelEntry extends AbstractLogFormatEntry {
        LevelEntry(int minWidth, int maxWidth, boolean leftAlign) {
            super("p", minWidth, maxWidth, leftAlign);
        }

        @Override
        public void format(StringBuilder sb, Instant instant, String loggerName, LogLevel lvl, String mrk, Supplier<String> msg, String location, @Nullable Throwable t, Pair<String, String> colorCodes) {
            appendFormatted(sb, lvl.name());
        }
    }

    private static class LoggerEntry extends AbstractLogFormatEntry {
        LoggerEntry(int minWidth, int maxWidth, boolean leftAlign) {
            super("c", minWidth, maxWidth, leftAlign);
        }

        @Override
        public void format(StringBuilder sb, Instant instant, String loggerName, LogLevel lvl, String mrk, Supplier<String> msg, String location, @Nullable Throwable t, Pair<String, String> colorCodes) {
            appendFormatted(sb, loggerName);
        }
    }

    private static class MarkerEntry extends AbstractLogFormatEntry {
        MarkerEntry(int minWidth, int maxWidth, boolean leftAlign) {
            super("marker", minWidth, maxWidth, leftAlign);
        }

        @Override
        public void format(StringBuilder sb, Instant instant, String loggerName, LogLevel lvl, String mrk, Supplier<String> msg, String location, @Nullable Throwable t, Pair<String, String> colorCodes) {
            appendFormatted(sb, mrk);
        }
    }

    private static class MessageEntry extends AbstractLogFormatEntry {
        MessageEntry(int minWidth, int maxWidth, boolean leftAlign) {
            super("m", minWidth, maxWidth, leftAlign);
        }

        @Override
        public void format(StringBuilder sb, Instant instant, String loggerName, LogLevel lvl, String mrk, Supplier<String> msg, String location, @Nullable Throwable t, Pair<String, String> colorCodes) {
            appendFormatted(sb, msg.get());
        }
    }

    private static class LocationEntry extends AbstractLogFormatEntry {
        LocationEntry(int minWidth, int maxWidth, boolean leftAlign) {
            super("l", minWidth, maxWidth, leftAlign);
        }

        @Override
        public void format(StringBuilder sb, Instant instant, String loggerName, LogLevel lvl, String mrk, Supplier<String> msg, String location, @Nullable Throwable t, Pair<String, String> colorCodes) {
            appendFormatted(sb, location);
        }
    }

    private static class ExceptionEntry extends AbstractLogFormatEntry {
        ExceptionEntry(int minWidth, int maxWidth, boolean leftAlign) {
            super("ex", minWidth, maxWidth, leftAlign);
        }

        @Override
        public void format(StringBuilder sb, Instant instant, String loggerName, LogLevel lvl, String mrk, Supplier<String> msg, String location, @Nullable Throwable t, Pair<String, String> colorCodes) {
            if (t != null) {
                appendFormatted(sb, t.getClass().getName() + ": " + t.getMessage());
                sb.append(NEWLINE);
            }
        }
    }

    private static class ColorStartEntry extends AbstractLogFormatEntry {
        ColorStartEntry(int minWidth, int maxWidth, boolean leftAlign) {
            super("Cstart", minWidth, maxWidth, leftAlign);
        }

        @Override
        public void format(StringBuilder sb, Instant instant, String loggerName, LogLevel lvl, String mrk, Supplier<String> msg, String location, @Nullable Throwable t, Pair<String, String> colorCodes) {
            appendFormatted(sb, colorCodes.first());
        }
    }

    private static class ColorEndEntry extends AbstractLogFormatEntry {
        ColorEndEntry(int minWidth, int maxWidth, boolean leftAlign) {
            super("Cend", minWidth, maxWidth, leftAlign);
        }

        @Override
        public void format(StringBuilder sb, Instant instant, String loggerName, LogLevel lvl, String mrk, Supplier<String> msg, String location, @Nullable Throwable t, Pair<String, String> colorCodes) {
            appendFormatted(sb, colorCodes.second());
        }
    }

    private static class DateEntry implements LogFormatEntry {
        private final String pattern;
        private final DateTimeFormatter formatter;

        DateEntry(String pattern) {
            this.pattern = pattern;
            this.formatter = switch (pattern) {
                case "ISO8601" -> DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssSSSZ");
                case "HH:mm:ss,SSS" -> DateTimeFormatter.ofPattern("HH:mm:ss,SSS");
                case "yyyy-MM-dd HH:mm:ss,SSS" -> DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss,SSS");
                case "yyyy-MM-dd HH:mm:ss" -> DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                default -> DateTimeFormatter.ofPattern(pattern.isEmpty() ? "HH:mm:ss" : pattern);
            };
        }

        @Override
        public void format(StringBuilder sb, Instant instant, String loggerName, LogLevel lvl, String mrk, Supplier<String> msg, String location, @Nullable Throwable t, Pair<String, String> colorCodes) {
            formatter.formatTo(instant.atZone(ZONE_ID), sb);
        }

        @Override
        public String getLog4jFormat() {
            return pattern.isEmpty() ? "%d" : "%d{" + pattern + "}";
        }
    }

    private static class NewlineEntry implements LogFormatEntry {
        @Override
        public void format(StringBuilder sb, Instant instant, String loggerName, LogLevel lvl, String mrk, Supplier<String> msg, String location, @Nullable Throwable t, Pair<String, String> colorCodes) {
            sb.append(NEWLINE);
        }

        @Override
        public String getLog4jFormat() {
            return "%n";
        }
    }

    private volatile List<LogFormatEntry> entries;

    /**
     * Constructs a LogFormat with the default format.
     */
    public LogFormat() {
        setFormat("%Cstart[%p] %d{HH:mm:ss} %c %marker %m %l %ex%Cend%n");
    }

    /**
     * Set the format string.
     * @param format the format string in Log4J style
     */
    public void setFormat(String format) {
        this.entries = parseLog4jFormatString(format);
    }

    /**
     * Get the format string.
     * @return the format string in Log4J style
     */
    public String getFormat() {
        StringBuilder sb = new StringBuilder();
        for (LogFormatEntry entry : entries) {
            sb.append(entry.getLog4jFormat());
        }
        return sb.toString();
    }

    /**
     * Formats a log entry.
     *
     * @param out
     * @param instant    the timestamp of the log entry
     * @param loggerName the name of the logger
     * @param lvl        the log level
     * @param mrk        the marker
     * @param msg        the message supplier
     * @param location   the location information
     * @param t          the throwable, if any
     * @param colorCodes the color codes for the log level (start and end)
     */
    public void formatLogEntry(PrintStream out, Instant instant, String loggerName, LogLevel lvl, String mrk, Supplier<String> msg, String location, @Nullable Throwable t, Pair<String, String> colorCodes) {
        StringBuilder sb = SB_THREAD_LOCAL.get();
        sb.setLength(0);
        for (LogFormatEntry entry : entries) {
            entry.format(sb, instant, loggerName, lvl, mrk, msg, location, t, colorCodes);
        }
        out.print(sb);
        if (sb.length() > 4096) {
            sb.setLength(256);
            sb.trimToSize();
        }
    }

    private static final Pattern PATTERN = Pattern.compile("%(-?\\d*)(\\.\\d+)?([a-zA-Z]+)(\\{([^}]+)})?|%%|%n");

    private static List<LogFormatEntry> parseLog4jFormatString(String format) {
        List<LogFormatEntry> entries = new ArrayList<>();
        Matcher matcher = PATTERN.matcher(format);
        int lastEnd = 0;
        while (matcher.find()) {
            if (matcher.start() > lastEnd) {
                entries.add(new LiteralEntry(format.substring(lastEnd, matcher.start())));
            }

            String match = matcher.group();
            if (match.equals("%%")) {
                entries.add(new LiteralEntry("%"));
            } else if (match.equals("%n")) {
                entries.add(new NewlineEntry());
            } else {
                String minWidthStr = matcher.group(1);
                String maxWidthStr = matcher.group(2);
                String type = matcher.group(3);
                String options = matcher.group(5);

                boolean leftAlign = minWidthStr != null && minWidthStr.startsWith("-");
                int minWidth = (minWidthStr != null && !minWidthStr.isEmpty()) ? Math.abs(Integer.parseInt(minWidthStr)) : 0;
                int maxWidth = (maxWidthStr != null && maxWidthStr.length() > 1) ? Integer.parseInt(maxWidthStr.substring(1)) : 0;

                switch (type) {
                    case "p", "level" -> entries.add(new LevelEntry(minWidth, maxWidth, leftAlign));
                    case "c", "logger" -> entries.add(new LoggerEntry(minWidth, maxWidth, leftAlign));
                    case "marker" -> entries.add(new MarkerEntry(minWidth, maxWidth, leftAlign));
                    case "m", "msg", "message" -> entries.add(new MessageEntry(minWidth, maxWidth, leftAlign));
                    case "l", "location" -> entries.add(new LocationEntry(minWidth, maxWidth, leftAlign));
                    case "ex", "exception", "throwable" -> entries.add(new ExceptionEntry(minWidth, maxWidth, leftAlign));
                    case "Cstart" -> entries.add(new ColorStartEntry(minWidth, maxWidth, leftAlign));
                    case "Cend" -> entries.add(new ColorEndEntry(minWidth, maxWidth, leftAlign));
                    case "d" -> entries.add(new DateEntry(options != null ? options : ""));
                    default -> entries.add(new LiteralEntry(match));
                }
            }
            lastEnd = matcher.end();
        }
        if (lastEnd < format.length()) {
            entries.add(new LiteralEntry(format.substring(lastEnd)));
        }
        return entries;
    }
}
