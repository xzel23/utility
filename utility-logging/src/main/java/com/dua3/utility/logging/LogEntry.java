package com.dua3.utility.logging;

import com.dua3.cabe.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.event.Level;
import org.slf4j.helpers.MessageFormatter;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * Represents a log entry with information about the log message, time, level, logger, and optional marker and throwable.
 */
public record LogEntry(Logger logger, Instant time, Level level, @Nullable Marker marker, String msg,
                       @Nullable Object[] arguments, @Nullable Throwable throwable) implements Serializable {
    /**
     * Formats the message using MessageFormatter.basicArrayFormat.
     *
     * @return the formatted message as a string.
     */
    public String formatMessage() {
        return MessageFormatter.basicArrayFormat(msg, arguments);
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
        if (throwable() == null) {
            return "[%-5s] %s %s\t%s".formatted(level, DateTimeFormatter.ISO_INSTANT.format(time), logger.getName(), formatMessage());
        } else {
            return "[%-5s] %s %s\t%s%n%s".formatted(level, DateTimeFormatter.ISO_INSTANT.format(time), logger.getName(), formatMessage(), formatThrowable());
        }
    }
}
