package com.dua3.utility.logging;

import org.jspecify.annotations.Nullable;

import java.time.Instant;

/**
 * Represents a functional interface for handling log entries.
 */
@FunctionalInterface
public interface LogEntryHandler {
    /**
     * Handles a log entry.
     *
     * @param entry the log entry to be handled
     * @deprecated use {@link #handle(Instant, String, LogLevel, String, String, String, Throwable)}
     */
    @Deprecated(forRemoval = true)
    void handleEntry(LogEntry entry);

    /**
     * Determines whether logging is enabled for a specific log level.
     *
     * @param lvl the log level to check
     * @return {@code true} if logging is enabled for the specified log level, otherwise {@code false}
     */
    default boolean isEnabled(LogLevel lvl) {
        return true;
    }

    /**
     * Handles a log entry.
     *
     * @param instant the timestamp of the log entry
     * @param loggerName the name of the logger
     * @param lvl the log level of the log entry
     * @param mrk the marker associated with the log entry, or {@code null} if none
     * @param msg the message of the log entry
     * @param location the location in the code where the log entry was created
     * @param t the throwable associated with the log entry, or {@code null} if none
     */
    default void handle(Instant instant, String loggerName, LogLevel lvl, String mrk, String msg, String location, @Nullable Throwable t) {
        handleEntry(new SimpleLogEntry(instant, loggerName, lvl, mrk, msg, location, t));
    }
}
