package com.dua3.utility.logging;

/**
 * Represents a functional interface for handling log entries.
 */
@FunctionalInterface
public interface LogEntryHandler {
    /**
     * Handles a log entry.
     *
     * @param entry the log entry to be handled
     */
    void handleEntry(LogEntry entry);
}
