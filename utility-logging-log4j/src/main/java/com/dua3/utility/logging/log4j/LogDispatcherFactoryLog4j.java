package com.dua3.utility.logging.log4j;

import com.dua3.utility.logging.LogDispatcher;
import com.dua3.utility.logging.LogDispatcherFactory;

/**
 * A factory class for creating LogDispatcher instances using Log4j.
 */
public class LogDispatcherFactoryLog4j implements LogDispatcherFactory {
    static {
        LogUtilLog4J.updateLoggers();
    }

    /**
     * Constructor, called by SPI.
     */
    public LogDispatcherFactoryLog4j() { /* nothing to do */ }

    @Override
    public LogDispatcher getDispatcher() {
        return LogUtilLog4J.GLOBAL_APPENDER.dispatcher();
    }
}
