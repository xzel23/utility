package com.dua3.utility.logging;

import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.function.Supplier;

/**
 * Represents a functional interface for handling log entries.
 * @deprecated use {@link LogHandler}
 */
@Deprecated(forRemoval = true)
@FunctionalInterface
public interface LogEntryHandler extends LogHandler {

    @Override
    default String name() {
        return "unnamed " + getClass().getSimpleName();
    }

    /**
     * Handles a log entry.
     *
     * @param entry the log entry to be handled
     * @deprecated use {@link #handle(Instant, String, LogLevel, String, Supplier, String, Throwable)}
     */
    @Deprecated(forRemoval = true)
    void handleEntry(LogEntry entry);

    @Override
    default void handle(Instant instant, String loggerName, LogLevel lvl, String mrk, Supplier<String> msg, String location, @Nullable Throwable t) {
        handleEntry(new SimpleLogEntry(instant, loggerName, lvl, mrk, msg.get(), location, t));
    }

    @Override
    default void setFilter(LogFilter filter) {
        throw new UnsupportedOperationException();
    }

    @Override
    default LogFilter getFilter() {
        return LogFilter.allPass();
    }
}
