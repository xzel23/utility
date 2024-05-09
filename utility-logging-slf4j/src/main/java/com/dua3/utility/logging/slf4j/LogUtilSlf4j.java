package com.dua3.utility.logging.slf4j;

import com.dua3.utility.logging.LogEntryDispatcher;
import com.dua3.utility.logging.LogUtil;

import java.util.ServiceConfigurationError;

/**
 * The LogUtilSlf4j class provides utility methods for working with the SLF4J logging framework.
 * <p>
 * <b>Rerouting logging to SLF4J</b><br>
 * <ul>
 *     <li><b>JUL (java.util.logging):</b> add {@code jul-to-slf4j} to your dependencies and add a static initializer
 *     block before declaring any Logger:
 *     <pre>
 *         static {
 *             java.util.logging.LogManager.getLogManager().reset();
 *             SLF4JBridgeHandler.install();
 *         }
 *     </pre>
 *     <li><b>Log4J:</b> add {@code log4j-to-slf4j} to your dependencies. Do not add any {@code log4j-core}.
 * </ul>
 */
public final class LogUtilSlf4j {
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
