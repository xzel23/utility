package com.dua3.utility.logging;

/**
 * Interface for a factory that provides an instance of the LogEntryDispatcher.
 */
@FunctionalInterface
public interface ILogEntryDispatcherFactory {
    /**
     * Retrieves the LogEntryDispatcher instance and connects all available loggers to it.
     * <p>
     * NOTE: This method is called by the ServiceProvider and not intended to be called directly by user code.
     * @return The global LogEntryDispatcher instance.
     */
    LogEntryDispatcher getDispatcher();
}
