package com.dua3.utility.logging.slf4j;

import com.dua3.utility.logging.LogEntryDispatcher;
import com.dua3.utility.logging.LogUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ServiceConfigurationError;

/**
 * The LogUtilSlf4j class provides utility methods for working with the SLF4J logging framework.
 */
public final class LogUtilSlf4j {
    private static final Logger LOG = LogManager.getLogger(LogUtilSlf4j.class);

    private LogUtilSlf4j() {
    }

    /**
     * Check if the default dispatcher factory implementation is the SLF4J implementation.
     * @return true, if the SLF4J implementation is used
     */
    public static boolean isDefaultImplementation() {
        return LogUtil.getGlobalDispatcher() instanceof LoggerFactorySlf4j;
    }

    /**
     * Returns the global LogEntryDispatcher by using the available ILogEntryDispatcherFactory implementations loaded
     * through ServiceLoader and connects all known loggers to it.
     * <p>
     * NOTE: This method delegates to {@link LogUtil#getGlobalDispatcher()}.
     *
     * @return The global LogEntryDispatcher instance.
     * @throws ServiceConfigurationError if no factories can create a LogEntryDispatcher.
     * @throws IllegalStateException if the implementations do not match
     */
    public static LoggerFactorySlf4j getGlobalDispatcher() {
        LogEntryDispatcher dispatcher = LogUtil.getGlobalDispatcher();
        if(dispatcher instanceof LoggerFactorySlf4j loggerFactorySlf4j) {
            return loggerFactorySlf4j;
        }
        throw new IllegalStateException("wrong implementation: " + dispatcher.getClass());
    }
}
