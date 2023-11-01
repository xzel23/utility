package com.dua3.utility.logging;

import java.util.Collection;

/**
 * This interface defines the contract for classes that dispatch log entries to registered handlers.
 */
public interface LogEntryDispatcher {
    /**
     * Adds a handler for log entry events. The handler will be invoked
     * whenever a log entry is received.
     *
     * @param handler The log entry handler to be added.
     */
    void addLogEntryHandler(LogEntryHandler handler);

    /**
     * Removes a previously added log entry handler. The handler will no longer be invoked
     * for any log entries.
     *
     * @param handler The log entry handler to be removed.
     */
    void removeLogEntryHandler(LogEntryHandler handler);
}
