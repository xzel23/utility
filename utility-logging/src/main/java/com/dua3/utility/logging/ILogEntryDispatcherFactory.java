package com.dua3.utility.logging;

/**
 * Interface for a factory that provides an instance of the LogEntryDispatcher.
 */
@FunctionalInterface
public interface ILogEntryDispatcherFactory {
    /**
     * Retrieves the global LogEntryDispatcher instance and connects all available loggers to it.
     *
     * @return The global LogEntryDispatcher instance.
     */
    LogEntryDispatcher getGlobalDispatcher();
}
