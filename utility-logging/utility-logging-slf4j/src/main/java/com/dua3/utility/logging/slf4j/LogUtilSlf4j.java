package com.dua3.utility.logging.slf4j;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * The LogUtilSlf4j class provides utility methods for working with the SLF4J logging framework.
 */
public final class LogUtilSlf4j {
    private static final Logger LOG = LogManager.getLogger(LogUtilSlf4j.class);

    private LogUtilSlf4j() {}

    /**
     * Returns an Optional containing an instance of LoggerFactorySlf4j if the retrieved ILoggerFactory
     * is an instance of LoggerFactorySlf4j. Otherwise, it returns an empty Optional.
     *
     * @return an Optional containing an instance of LoggerFactorySlf4j, or an empty Optional
     */
    public static Optional<LoggerFactorySlf4j> getLoggerFactory() {
        ILoggerFactory iLoggerFactory = LoggerFactory.getILoggerFactory();
        if (iLoggerFactory instanceof  LoggerFactorySlf4j factory) {
            LOG.debug("ILoggerFactory of class {} found", factory.getClass());
            return Optional.of(factory);
        } else {
            LOG.debug("unexpected ILoggerFactory of class {}, expected {}", iLoggerFactory.getClass(), LoggerFactorySlf4j.class);
            return Optional.empty();
        }
    }
}
