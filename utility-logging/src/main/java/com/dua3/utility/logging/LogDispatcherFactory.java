package com.dua3.utility.logging;

import org.jspecify.annotations.Nullable;

/**
 * Interface for a factory that provides an instance of the LogDispatcher.
 */
@FunctionalInterface
public interface LogDispatcherFactory {
    /**
     * Retrieves the LogDispatcher instance and connects all available loggers to it.
     * <p>
     * NOTE: This method is called by the ServiceProvider and not intended to be called directly by user code.
     * @return The global LogDispatcher instance.
     */
    @Nullable
    LogDispatcher getDispatcher();
}
