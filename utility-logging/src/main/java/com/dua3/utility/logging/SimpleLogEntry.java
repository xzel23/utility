package com.dua3.utility.logging;

import org.jspecify.annotations.Nullable;

import java.time.Instant;

/**
 * Represents a single log entry containing details about a logging event.
 * This record implements the {@link LogEntry} interface and provides an immutable representation
 * of a log entry with properties such as message, logger name, time, log level, marker, throwable, and location.
 *
 * @param message    the message of the log entry
 * @param loggerName the name of the logger associated with the log entry
 * @param time       the timestamp of the log entry as an {@link Instant}
 * @param level      the {@link LogLevel} describing the severity of the log entry
 * @param marker     an identifier or additional metadata associated with the log entry
 * @param throwable  the {@link Throwable} associated with the log entry, or null if no throwable is present
 * @param location   the source location associated with the log entry, or null if no location is specified
 */
public record SimpleLogEntry(
        String message,
        String loggerName,
        Instant time,
        LogLevel level,
        String marker,
        @Nullable Throwable throwable,
        @Nullable String location
) implements LogEntry {
}
