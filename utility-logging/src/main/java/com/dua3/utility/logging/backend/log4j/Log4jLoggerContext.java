package com.dua3.utility.logging.backend.log4j;

import org.apache.logging.log4j.spi.ExtendedLogger;
import org.apache.logging.log4j.spi.LoggerContext;

public class Log4jLoggerContext implements LoggerContext {
    private static final Object EXTERNAL_CONTEXT = new Object();

    @Override
    public Object getExternalContext() {
        return EXTERNAL_CONTEXT;
    }

    @Override
    public ExtendedLogger getLogger(String name) {
        return new LoggerLog4j(name);
    }

    @Override
    public ExtendedLogger getLogger(String name, org.apache.logging.log4j.message.MessageFactory messageFactory) {
        return getLogger(name);
    }

    @Override
    public boolean hasLogger(String name) {
        return true;
    }

    @Override
    public boolean hasLogger(String name, org.apache.logging.log4j.message.MessageFactory messageFactory) {
        return true;
    }

    @Override
    public boolean hasLogger(String name, Class<? extends org.apache.logging.log4j.message.MessageFactory> messageFactoryClass) {
        return true;
    }
}