package com.dua3.utility.logging;

import org.jspecify.annotations.Nullable;

import java.time.Instant;

/**
 * Represents a single log entry containing details about a logging event.
 * This record implements the {@link LogEntry} interface and provides an immutable representation
 * of a log entry with properties such as message, logger name, time, log level, marker, throwable, and location.
 *
 * @param time       the timestamp of the log entry as an {@link Instant}
 * @param loggerName the name of the logger associated with the log entry
 * @param level      the {@link LogLevel} describing the severity of the log entry
 * @param marker     an identifier or additional metadata associated with the log entry
 * @param message    the message of the log entry
 * @param location   the source location associated with the log entry, or null if no location is specified
 * @param throwable  the {@link Throwable} associated with the log entry, or null if no throwable is present
 */
public record SimpleLogEntry(
        Instant time, String loggerName, LogLevel level, String marker, String message,
        String location,
        @Nullable Throwable throwable
) implements LogEntry {
    @Deprecated(forRemoval = true) // for compatibility with existing code
    public SimpleLogEntry (       String message,
                                  String loggerName,
                                  Instant time,
                                  LogLevel level,
                                  String marker,
                                  @Nullable Throwable throwable,
                                  @Nullable String location) {
        this(time, loggerName, level, marker, message, location, throwable);
    }
}
