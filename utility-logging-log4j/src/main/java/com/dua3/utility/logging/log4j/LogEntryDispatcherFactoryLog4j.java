package com.dua3.utility.logging.log4j;

import com.dua3.utility.logging.LogEntryDispatcher;
import com.dua3.utility.logging.ILogEntryDispatcherFactory;

/**
 * A factory class for creating LogEntryDispatcher instances using Log4j.
 */
public class LogEntryDispatcherFactoryLog4j implements ILogEntryDispatcherFactory {
    @Override
    public LogEntryDispatcher getDispatcher() {
        return LogAppenderLog4j.getGlobalInstance().dispatcher();
    }
}
