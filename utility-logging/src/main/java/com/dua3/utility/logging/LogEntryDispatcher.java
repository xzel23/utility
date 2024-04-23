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

    /**
     * Sets the {@link LogEntryFilter} for log entry events.
     *
     * <p>Only entries that pass the filter will be dispatched to handlers.
     *
     * @param filter The filter to be set for log entry events.
     */
    void setFilter(LogEntryFilter filter);

    /**
     * Get the {@link LogEntryFilter}.
     *
     * @return the filter in use
     */
    LogEntryFilter getFilter();

    /**
     * Get the registered log entry handlers. Note that implementations usually hold weak references
     * to the handlers, so unused handlers may already have been removed from the list.
     * @return collection containing the registered log entry handlers
     */
    Collection<LogEntryHandler> getLogEntryHandlers();
}
