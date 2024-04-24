package com.dua3.utility.logging;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiPredicate;

/**
 * The DefaultLogEntryFilter class is an implementation of the LogEntryFilter interface
 * that filters log entries based on their log level and a user-defined filter.
 *
 * <p>DefaultLogEntryFilter provides methods to set and retrieve the log level and filter,
 * as well as a test method to determine if a LogEntry should be included or excluded.
 */
public class DefaultLogEntryFilter implements LogEntryFilter {

    private final ConcurrentHashMap<String, Boolean> knownLoggers = new ConcurrentHashMap<>();

    private volatile LogLevel level;
    private volatile BiPredicate<String, LogLevel> filter;

    /**
     * Constructs a new DefaultLogEntryFilter with {@link LogLevel#TRACE} as the default log level and a filter that
     * lets all log entries pass.
     */
    public DefaultLogEntryFilter() {
        this(LogLevel.TRACE, (name, level) -> true);
    }

    /**
     * Constructs a new DefaultLogEntryFilter with the specified log level and filter.
     *
     * @param level  the log level to set
     * @param filter the filter to set
     */
    public DefaultLogEntryFilter(LogLevel level, BiPredicate<String, LogLevel> filter) {
        this.level = level;
        this.filter = filter;
    }

    /**
     * Sets the log level of the DefaultLogEntryFilter.
     *
     * @param level the log level to set
     */
    public void setLevel(LogLevel level) {
        this.level = level;
    }

    /**
     * Retrieves the log level of the DefaultLogEntryFilter.
     *
     * @return The log level of the DefaultLogEntryFilter.
     */
    public LogLevel getLevel() {
        return level;
    }

    /**
     * Sets the filter of the DefaultLogEntryFilter.
     *
     * @param filter A {@link BiPredicate} that takes a logger name and a log level as input and returns a boolean
     *              indicating whether the log entry should be filtered or not.
     *              The first argument is the logger name, and the second argument is the log level.
     *              Returns true if the log entry should be included, false otherwise.
     */
    public void setFilter(BiPredicate<String, LogLevel> filter) {
        if (this.filter != filter) {
            this.filter = filter;
            knownLoggers.clear();
        }
    }

    /**
     * Retrieves the filter used to determine if a log entry should be included or excluded.
     *
     * @return The filter used to determine if a log entry should be included or excluded.
     * @see #setFilter(BiPredicate)
     */
    public BiPredicate<String, LogLevel> getFilter() {
        return filter;
    }

    @Override
    public boolean test(LogEntry logEntry) {
        if (logEntry.level().ordinal() < level.ordinal()) {
            return false;
        }
        return knownLoggers.computeIfAbsent(logEntry.loggerName(), loggerName -> filter.test(logEntry.loggerName(), logEntry.level()));
    }

    /**
     * Creates a copy of the DefaultLogEntryFilter with the same log level and filter.
     *
     * @return A new {@link DefaultLogEntryFilter} instance with the same log level and filter.
     */
    public DefaultLogEntryFilter copy() {
        return new DefaultLogEntryFilter(getLevel(), getFilter());
    }

    /**
     * Returns a new DefaultLogEntryFilter with the same filter and the specified log level.
     *
     * @param newLevel the log level to set
     * @return a new {@link DefaultLogEntryFilter} instance with the specified log level
     * @see DefaultLogEntryFilter
     */
    public DefaultLogEntryFilter withLevel(LogLevel newLevel) {
        return new DefaultLogEntryFilter(newLevel, getFilter());
    }

    /**
     * Returns a new {@link DefaultLogEntryFilter} with the same log level and the specified filter.
     *
     * @param newFilter the new filter to set
     * @return a new instance of {@link DefaultLogEntryFilter} with the specified filter
     */
    public DefaultLogEntryFilter withFilter(BiPredicate<String, LogLevel> newFilter) {
        return new DefaultLogEntryFilter(getLevel(), newFilter);
    }
}
