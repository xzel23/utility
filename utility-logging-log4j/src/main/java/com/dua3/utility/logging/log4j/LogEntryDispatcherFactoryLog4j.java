package com.dua3.utility.logging.log4j;

import com.dua3.utility.logging.LogEntryDispatcher;
import com.dua3.utility.logging.ILogEntryDispatcherFactory;

/**
 * A factory class for creating LogEntryDispatcher instances using Log4j.
 */
public class LogEntryDispatcherFactoryLog4j implements ILogEntryDispatcherFactory {
    static {
        LogUtilLog4J.updateLoggers();
    }

    /**
     * Constructor, called by SPI.
     */
    public LogEntryDispatcherFactoryLog4j() { /* nothing to do */ }

    @Override
    public LogEntryDispatcher getDispatcher() {
        return LogUtilLog4J.GLOBAL_APPENDER.dispatcher();
    }
}
