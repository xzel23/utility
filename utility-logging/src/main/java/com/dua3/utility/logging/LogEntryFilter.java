package com.dua3.utility.logging;

import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.function.Predicate;

/**
 * The LogEntryFilter interface represents a filter used to determine if a LogEntry should be included or excluded.
 *
 * <p>The LogEntryFilter interface is a functional interface and can therefore be used as the assignment target for a lambda expression or method reference.
 *
 * <p>Implementations of this interface are expected to override the {@link #test(LogEntry)} method, which takes a LogEntry parameter and returns true if the entry should be included
 * , or false if it should be excluded.
 *
 * <p>Example usage:
 * <pre>
 * {@code
 * LogEntryFilter filter = entry -> entry.level().ordinal() >= LogLevel.INFO.ordinal();
 * boolean shouldInclude = filter.test(logEntry);
 * }
 * </pre>
 */
public interface LogEntryFilter extends Predicate<LogEntry> {

    /**
     * A LogEntryFilter that allows all log entries to pass through.
     *
     * <p>This filter implementation always returns true, indicating that all log entries should be included and
     * none filtered out.
     */
    LogEntryFilter ALL_PASS_FILTER = entry -> true;

    /**
     * Test if a {@link LogEntry} should be processed.
     * @param logEntry the input argument
     * @return {@code true}, if the {@code logEntry} should be processed, {@code false} if it should be filtered out
     * @deprecated use {@link #test(Instant, String, LogLevel, String, String, String, Throwable)}
     */
    @Override
    @Deprecated(forRemoval = true)
    boolean test(LogEntry logEntry);

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
    default boolean test(Instant instant, String loggerName, LogLevel lvl, String mrk, String msg, String location, @Nullable Throwable t) {
        return test(new SimpleLogEntry(instant, loggerName, lvl, mrk, msg, location, t));
    }

    /**
     * Returns a LogEntryFilter that allows all log entries to pass through.
     *
     * @return a LogEntryFilter that allows all log entries to pass through
     */
    static LogEntryFilter allPass() {
        return ALL_PASS_FILTER;
    }
}
