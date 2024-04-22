package com.dua3.utility.logging.slf4j;

import com.dua3.utility.logging.ILogEntryDispatcherFactory;
import com.dua3.utility.logging.LogEntryDispatcher;
import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;

/**
 * This class is an implementation of the ILogEntryDispatcherFactory interface.
 * It provides the functionality to create a Slf4j LogEntryDispatcher instance.
 */
public class LogEntryDispatcherFactorySlf4j implements ILogEntryDispatcherFactory {

    private static LoggerFactorySlf4j factory;

    public static synchronized LoggerFactorySlf4j getFactory() {
        if (factory == null) {
            ILoggerFactory iLoggerFactory = LoggerFactory.getILoggerFactory();
            if (iLoggerFactory instanceof LoggerFactorySlf4j loggerFactorySlf4j) {
                factory = loggerFactorySlf4j;
            }
        }
        return factory;
    }

    @Override
    public LogEntryDispatcher getDispatcher() {
        return getFactory();
    }
}
