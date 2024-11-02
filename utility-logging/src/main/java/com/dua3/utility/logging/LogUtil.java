package com.dua3.utility.logging;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;

import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

/**
 * Utility class for logging operations.
 */
public final class LogUtil {
    private LogUtil() {
    }

    private static final Logger LOG = LogManager.getLogger(LogUtil.class);

    private static @Nullable LogEntryDispatcher globalDispatcher;

    private static synchronized void init() {
        if (globalDispatcher == null) {
            ServiceLoader<ILogEntryDispatcherFactory> serviceLoader = ServiceLoader.load(ILogEntryDispatcherFactory.class);

            for (ILogEntryDispatcherFactory factory : serviceLoader) {
                try {
                    LogEntryDispatcher dispatcher = factory.getDispatcher();
                    if (dispatcher != null) {
                        LOG.debug("created dispatcher of class {} using factory {}", dispatcher.getClass(), factory.getClass());
                        globalDispatcher = dispatcher;
                        return;
                    }
                    LOG.debug("factory {} did not return a dispatcher", factory.getClass());
                } catch (Exception e) {
                    LOG.warn("factory {} threw an exception when trying to create a dispatcher", factory.getClass().getName(), e);
                }
            }

            throw new ServiceConfigurationError("no factories left to try - could not create a dispatcher");
        }
    }

    /**
     * Checks if the globalDispatcher variable is null and initializes it by calling the init() method if necessary.
     *
     * @throws ServiceConfigurationError if no factories can create a LogEntryDispatcher
     */
    public static void assureInitialized() {
        if (globalDispatcher == null) {
            init();
        }
    }

    /**
     * Returns the global LogEntryDispatcher by using the available ILogEntryDispatcherFactory implementations loaded
     * through ServiceLoader and connects all known loggers to it.
     *
     * @return The global LogEntryDispatcher instance.
     * @throws ServiceConfigurationError if no factories can create a LogEntryDispatcher.
     */
    public static LogEntryDispatcher getGlobalDispatcher() {
        assureInitialized();
        assert globalDispatcher != null;
        return globalDispatcher;
    }
}
