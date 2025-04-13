package com.dua3.utility.logging.slf4j;

import com.dua3.utility.logging.ILogEntryDispatcherFactory;
import com.dua3.utility.logging.LogEntryDispatcher;
import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;

/**
 * This class is an implementation of the ILogEntryDispatcherFactory interface.
 * It provides the functionality to create a SLF4J LogEntryDispatcher instance.
 */
public final class LogEntryDispatcherFactorySlf4j implements ILogEntryDispatcherFactory {

    private static class SingletonHolder {
        private static final LoggerFactorySlf4j INSTANCE = initInstance();

        private static LoggerFactorySlf4j initInstance() {
            ILoggerFactory iLoggerFactory = LoggerFactory.getILoggerFactory();
            if (iLoggerFactory instanceof LoggerFactorySlf4j loggerFactorySlf4j) {
                return loggerFactorySlf4j;
            } else {
                throw new IllegalStateException("unecpected factory type: " + iLoggerFactory.getClass());
            }
        }
    }

    /**
     * Default constructor for the class LogEntryDispatcherFactorySlf4j.
     * <p>
     * This constructor initializes an instance of the factory, which is used to create and retrieve
     * LogEntryDispatcher instances based on the SLF4J logging framework.
     */
    public LogEntryDispatcherFactorySlf4j() {}

    /**
     * Retrieves the instance of the LoggerFactorySlf4j class, which is a singleton implementation of the ILoggerFactory interface and LogEntryDispatcher interface.
     *
     * @return The instance of the LoggerFactorySlf4j class.
     */
    public static LoggerFactorySlf4j getFactory() {
        return SingletonHolder.INSTANCE;
    }

    @Override
    public LogEntryDispatcher getDispatcher() {
        return getFactory();
    }
}
