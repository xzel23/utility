package com.dua3.utility.logging.slf4j;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public final class LogUtilSlf4j {
    private static final Logger LOG = LogManager.getLogger(LogUtilSlf4j.class);

    private LogUtilSlf4j() {}

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
