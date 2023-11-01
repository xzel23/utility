package com.dua3.utility.logging.slf4j;

import com.dua3.utility.logging.ILogEntryDispatcherFactory;
import com.dua3.utility.logging.LogEntryDispatcher;

/**
 * This class is an implementation of the ILogEntryDispatcherFactory interface.
 * It provides the functionality to create a Slf4j LogEntryDispatcher instance.
 */
public class LogEntryDispatcherFactorySlf4j implements ILogEntryDispatcherFactory {
    @Override
    public LogEntryDispatcher getGlobalDispatcher() {
        return LogUtilSlf4j.getLoggerFactory().orElse(null);
    }
}
