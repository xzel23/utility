package com.dua3.utility.logging;

import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.function.Supplier;

/**
 * The LogEntryFilter interface represents a filter used to determine if a LogEntry should be included or excluded.
 *
 * <p>The LogEntryFilter interface is a functional interface and can therefore be used as the assignment target for a lambda expression or method reference.
 */
public interface LogFilter {

    /**
     * A LogEntryFilter that allows all log entries to pass through.
     *
     * <p>This filter implementation always returns true, indicating that all log entries should be included and
     * none filtered out.
     */
    LogFilter ALL_PASS_FILTER = new LogFilter() {
        @Override
        public String name() {
            return "ALL_PASS_FILTER";
        }

        @Override
        public boolean test(Instant instant, String loggerName, LogLevel lvl, String mrk, Supplier<String> msg, String location, @Nullable Throwable t) {
            return true;
        }
    };

    /**
     * Returns a LogEntryFilter that allows all log entries to pass through.
     *
     * @return a LogEntryFilter that allows all log entries to pass through
     */
    static LogFilter allPass() {
        return ALL_PASS_FILTER;
    }

    /**
     * Retrieves the name associated with this filter.
     *
     * @return the name of the filter as a String
     */
    String name();

    /**
     * Tests if a log entry, specified by its components, should be processed.
     *
     * @param instant the timestamp of the log entry
     * @param loggerName the name of the logger that generated the log entry
     * @param lvl the log level of the log entry
     * @param mrk the marker associated with the log entry, can be {@code null}
     * @param msg the message of the log entry
     * @param location the location where the log entry was generated, typically a code context such as a class or method name
     * @param t the throwable associated with the log entry, can be {@code null}
     * @return {@code true}, if the log entry should be processed, {@code false} if it should be filtered out
     */
    boolean test(Instant instant, String loggerName, LogLevel lvl, String mrk, Supplier<String> msg, String location, @Nullable Throwable t);
}
