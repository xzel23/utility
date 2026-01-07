package com.dua3.utility.logging.backend.log4j;

import org.apache.logging.log4j.spi.ExtendedLogger;
import org.apache.logging.log4j.spi.LoggerContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a context implementation for managing and retrieving Log4j loggers.
 */
public class Log4jLoggerContext implements LoggerContext {
    private static final Object EXTERNAL_CONTEXT = new Object();

    private final Map<String, ExtendedLogger> loggers = new ConcurrentHashMap<>();

    @Override
    public Object getExternalContext() {
        return EXTERNAL_CONTEXT;
    }

    @Override
    public ExtendedLogger getLogger(String name) {
        return loggers.computeIfAbsent(name, LoggerLog4j::new);
    }

    @Override
    public ExtendedLogger getLogger(String name, org.apache.logging.log4j.message.MessageFactory messageFactory) {
        return getLogger(name);
    }

    @Override
    public boolean hasLogger(String name) {
        return loggers.containsKey(name);
    }

    @Override
    public boolean hasLogger(String name, org.apache.logging.log4j.message.MessageFactory messageFactory) {
        return hasLogger(name);
    }

    @Override
    public boolean hasLogger(String name, Class<? extends org.apache.logging.log4j.message.MessageFactory> messageFactoryClass) {
        return hasLogger(name);
    }
}
