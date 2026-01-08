package com.dua3.utility.logging;

import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.function.Supplier;

/**
 * Represents a functional interface for handling log entries.
 */
public interface LogHandler {

    /**
     * Retrieves the name of the log entry handler.
     *
     * @return the name of the log entry handler as a String
     */
    String name();

    /**
     * Determines whether logging is enabled for a specific log level.
     *
     * @param lvl the log level to check
     * @return {@code true} if logging is enabled for the specified log level, otherwise {@code false}
     */
    default boolean isEnabled(LogLevel lvl) {
        return !(getFilter() instanceof StandardLogFilter filter) || filter.getLevel().ordinal() <= lvl.ordinal();
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
    void handle(Instant instant, String loggerName, LogLevel lvl, String mrk, Supplier<String> msg, String location, @Nullable Throwable t);

    /**
     * Sets the filter to be used for determining which log entries should be processed.
     *
     * @param filter the filter used to include or exclude log entries; must not be {@code null}
     */
    void setFilter(LogFilter filter);

    /**
     * Retrieves the current filter used for determining which log entries should be processed.
     *
     * @return the LogEntryFilter that is currently set; never {@code null}
     */
    LogFilter getFilter();
}
