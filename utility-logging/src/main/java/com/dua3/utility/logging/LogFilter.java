package com.dua3.utility.logging;

import org.jspecify.annotations.Nullable;

import java.time.Instant;

/**
 * The LogFilter class is an implementation of the LogEntryFilter interface
 * that filters log entries based on their name and log levels defined on package/class levels.
 */
public class LogFilter implements LogEntryFilter {

    private LogLevel level;
    private final LevelMap levelMap;

    public LogFilter() {
        this.level = LogLevel.INFO;
        this.levelMap = new LevelMap(level);
    }

    /**
     * Sets the global log level of the filter.
     *
     * @param level the global log level to set
     */
    public void setLevel(LogLevel level) {
        this.level = level;
    }

    /**
     * Retrieves the global log level of the filter.
     *
     * @return The global log level of the filter.
     */
    public LogLevel getLevel() {
        return level;
    }

    /**
     * Sets the log level for a given logger name or prefix.
     *
     * @param name the name or prefix of the logger(s) for which the log level is to be set
     * @param level the log level to assign
     */
    public void setLevel(String name, LogLevel level) {
        levelMap.put(name, level);
    }

    /**
     * Retrieves the log level associated with the specified logger name.
     *
     * @param name the name of the logger whose log level is to be retrieved
     * @return the log level assigned to the specified logger.
     */
    public LogLevel getLevel(String name) {
        return levelMap.level(name);
    }

    @Override
    @Deprecated(forRemoval = true)
    public boolean test(LogEntry logEntry) {
        return test(logEntry.time(), logEntry.loggerName(), logEntry.level(), logEntry.marker(), logEntry.message(), logEntry.location(), logEntry.throwable());
    }

    @Override
    public boolean test(Instant instant, String loggerName, LogLevel lvl, String mrk, String msg, String location, @Nullable Throwable t) {
        return (lvl.ordinal() >= level.ordinal() && lvl.ordinal() >= getLevel(loggerName).ordinal());
    }
}
