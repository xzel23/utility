package com.dua3.utility.logging.slf4j;

import com.dua3.utility.logging.LogDispatcherFactory;
import com.dua3.utility.logging.LogDispatcher;
import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;

/**
 * This class is an implementation of the ILogDispatcherFactory interface.
 * It provides the functionality to create a SLF4J LogDispatcher instance.
 */
public final class LogDispatcherFactorySlf4j implements LogDispatcherFactory {

    private static final class SingletonHolder {
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
     * Default constructor for the class LogDispatcherFactorySlf4j.
     * <p>
     * This constructor initializes an instance of the factory, which is used to create and retrieve
     * LogDispatcher instances based on the SLF4J logging framework.
     */
    public LogDispatcherFactorySlf4j() { /* nothing to do */ }

    /**
     * Retrieves the instance of the LoggerFactorySlf4j class, which is a singleton implementation of the ILoggerFactory interface and LogDispatcher interface.
     *
     * @return The instance of the LoggerFactorySlf4j class.
     */
    public static LoggerFactorySlf4j getFactory() {
        return SingletonHolder.INSTANCE;
    }

    @Override
    public LogDispatcher getDispatcher() {
        return getFactory();
    }
}
