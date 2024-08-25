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
    private volatile BiPredicate<String, LogLevel> filterLoggerName;
    private volatile BiPredicate<String, LogLevel> filterText;

    /**
     * Constructs a new DefaultLogEntryFilter with {@link LogLevel#TRACE} as the default log level and a looger name and
     * message filters that let all log entries pass.
     */
    public DefaultLogEntryFilter() {
        this(LogLevel.TRACE, (name, level) -> true, (text, level) -> true);
    }

    /**
     * Constructs a new DefaultLogEntryFilter with the specified log level and filter.
     *
     * @param level  the log level to set
     * @param filterLoggerName the filter to set for the logger name
     * @param filterLoggerName the filter to set for the message content
     */
    public DefaultLogEntryFilter(LogLevel level, BiPredicate<String, LogLevel> filterLoggerName, BiPredicate<String, LogLevel> filterText) {
        this.level = level;
        this.filterLoggerName = filterLoggerName;
        this.filterText = filterText;
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
     * Sets the logger name filter of the DefaultLogEntryFilter.
     *
     * @param filter A {@link BiPredicate} that takes a logger name and a log level as input and returns a boolean
     *              indicating whether the log entry should be filtered or not.
     *              The first argument is the logger name, and the second argument is the log level.
     *              Returns true if the log entry should be included, false otherwise.
     */
    public void setFilterLoggerName(BiPredicate<String, LogLevel> filter) {
        if (this.filterLoggerName != filter) {
            this.filterLoggerName = filter;
            knownLoggers.clear();
        }
    }

    /**
     * Retrieves the filter used to determine if a log entry should be included or excluded based on the logger name.
     *
     * @return The filter used to determine if a log entry should be included or excluded based on the logger name.
     * @see #setFilterLoggerName(BiPredicate)
     */
    public BiPredicate<String, LogLevel> getFilterLoggerName() {
        return filterLoggerName;
    }

    /**
     * Sets the message filter of the DefaultLogEntryFilter.
     *
     * @param filter A {@link BiPredicate} that takes a log message and a log level as input and returns a boolean
     *              indicating whether the log entry should be filtered or not.
     *              The first argument is the message text, and the second argument is the log level.
     *              Returns true if the log entry should be included, false otherwise.
     */
    public void setFilterText(BiPredicate<String, LogLevel> filter) {
        if (this.filterText != filter) {
            this.filterText = filter;
        }
    }

    /**
     * Retrieves the filter used to determine if a log entry should be included or excluded based on the logger name.
     *
     * @return The filter used to determine if a log entry should be included or excluded based on the logger name.
     * @see #setFilterLoggerName(BiPredicate)
     */
    public BiPredicate<String, LogLevel> getFilterText() {
        return filterText;
    }

    @Override
    public boolean test(LogEntry logEntry) {
        if (logEntry.level().ordinal() < level.ordinal()) {
            return false;
        }

        boolean isLoggerShown = knownLoggers.computeIfAbsent(logEntry.loggerName(), loggerName -> filterLoggerName.test(logEntry.loggerName(), logEntry.level()));
        if (!isLoggerShown) {
            return false;
        }

        return filterText.test(logEntry.message(), logEntry.level());
    }

    /**
     * Creates a copy of the DefaultLogEntryFilter with the same log level and filter.
     *
     * @return A new {code DefaultLogEntryFilter} instance with the same log level and filter.
     */
    public DefaultLogEntryFilter copy() {
        return new DefaultLogEntryFilter(getLevel(), getFilterLoggerName(), getFilterText());
    }

    /**
     * Returns a new DefaultLogEntryFilter with the same filter and the specified log level.
     *
     * @param newLevel the log level to set
     * @return a new {code DefaultLogEntryFilter} instance with the specified log level
     */
    public DefaultLogEntryFilter withLevel(LogLevel newLevel) {
        return new DefaultLogEntryFilter(newLevel, getFilterLoggerName(), getFilterText());
    }

    /**
     * Returns a new {code DefaultLogEntryFilter} with the same log level and the specified logger name filter.
     *
     * @param newFilter the new filter to set
     * @return a new instance of {code DefaultLogEntryFilter} with the specified filter
     */
    public DefaultLogEntryFilter withFilterLoggerName(BiPredicate<String, LogLevel> newFilter) {
        return new DefaultLogEntryFilter(getLevel(), newFilter, getFilterText());
    }

    /**
     * Returns a new {code DefaultLogEntryFilter} with the same log level and the specified message filter.
     *
     * @param newFilter the new filter to set
     * @return a new instance of {code DefaultLogEntryFilter} with the specified filter
     */
    public DefaultLogEntryFilter withFilterText(BiPredicate<String, LogLevel> newFilter) {
        return new DefaultLogEntryFilter(getLevel(), getFilterLoggerName(), newFilter);
    }
}
