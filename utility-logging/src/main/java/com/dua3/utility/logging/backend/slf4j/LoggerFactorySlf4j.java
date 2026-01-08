package com.dua3.utility.logging.backend.slf4j;

import org.slf4j.ILoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

/**
 * The LoggerFactorySlf4j class is an implementation of the ILoggerFactory and LogDispatcher interfaces.
 */
public class LoggerFactorySlf4j implements ILoggerFactory {

    private final ConcurrentHashMap<String, LoggerSlf4j> loggers = new ConcurrentHashMap<>();

    /**
     * Constructs a new instance of LoggerFactorySlf4j.
     */
    public LoggerFactorySlf4j() {
        // nothing  to do
    }

    @Override
    public org.slf4j.Logger getLogger(String name) {
        return loggers.computeIfAbsent(name, LoggerSlf4j::new);
    }
}
