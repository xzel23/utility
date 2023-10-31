package com.dua3.utility.logging;

import com.dua3.cabe.annotations.Nullable;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Represents a log entry with information about the log message, time, level, logger, and optional marker and throwable.
 */
public final class LogEntry {
    private final String loggerName;
    private final Instant time;
    private final LogLevel level;
    private final String marker;
    private Supplier<String> messageFormatter;
    private String formattedMessage;
    private final Throwable throwable;

    /**
     *
     */
    public LogEntry(String loggerName, Instant time, LogLevel level, @Nullable String marker, Supplier<String> messageFormatter,
                    @Nullable Throwable throwable) {
        this.loggerName = loggerName;
        this.time = time;
        this.level = level;
        this.marker = marker;
        this.messageFormatter = messageFormatter;
        this.throwable = throwable;
    }

    /**
     * Formats the message using MessageFormatter.basicArrayFormat.
     *
     * @return the formatted message as a string.
     */
    public String formatMessage() {
        if (formattedMessage==null) {
            formattedMessage = messageFormatter.get();
            messageFormatter = null;
        }
        return formattedMessage;
    }

    /**
     * Formats the throwable object by printing its stack trace.
     * If the throwable object is null, it returns an empty string.
     *
     * @return the formatted stack trace as a string.
     */
    public String formatThrowable() {
        if (throwable == null) {
            return "";
        }

        try (StringWriter sw = new StringWriter(200); PrintWriter pw = new PrintWriter(sw)) {
            throwable.printStackTrace(pw);
            return sw.toString();
        } catch (IOException e) {
            return throwable.toString();
        }
    }

    @Override
    public String toString() {
        return format("", "");
    }

    String format(String prefix, String suffix) {
        if (throwable() == null) {
            return "%s[%-5s] %s %s\t%s%s".formatted(prefix, level, DateTimeFormatter.ISO_INSTANT.format(time), loggerName, formatMessage(), suffix);
        } else {
            return "%s[%-5s] %s %s\t%s%n%s%s".formatted(prefix, level, DateTimeFormatter.ISO_INSTANT.format(time), loggerName, formatMessage(), formatThrowable(), suffix);
        }
    }

    public String loggerName() {
        return loggerName;
    }

    public Instant time() {
        return time;
    }

    public LogLevel level() {
        return level;
    }

    public String marker() {
        return marker;
    }

    public Supplier<String> messageFormatter() {
        return messageFormatter;
    }

    public Throwable throwable() {
        return throwable;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (LogEntry) obj;
        return Objects.equals(this.loggerName, that.loggerName) &&
                Objects.equals(this.time, that.time) &&
                Objects.equals(this.level, that.level) &&
                Objects.equals(this.marker, that.marker) &&
                Objects.equals(this.messageFormatter, that.messageFormatter) &&
                Objects.equals(this.throwable, that.throwable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(loggerName, time, level, marker, messageFormatter, throwable);
    }

}
