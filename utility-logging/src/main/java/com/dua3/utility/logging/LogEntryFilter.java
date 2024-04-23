package com.dua3.utility.logging;

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
@FunctionalInterface
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
     */
    @Override
    boolean test(LogEntry logEntry);

    /**
     * Returns a LogEntryFilter that allows all log entries to pass through.
     *
     * @return a LogEntryFilter that allows all log entries to pass through
     */
    static LogEntryFilter allPass() {
        return ALL_PASS_FILTER;
    }
}
