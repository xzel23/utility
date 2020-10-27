package com.dua3.utility.logging;

/**
 * A base class for wrappers around log records of different logging frameworks.
 * @param <T> underlying type of log records  
 */
public abstract class AbstractLogEntry<T extends Object> implements LogEntry {

    /**
     * Get the log record of the underlying implementation.
     * @return the underlying  implementation's log record
     */
    public abstract T getNative();

    @Override
    public String toString() {
        return LogEntry.format(this);
    }

}
