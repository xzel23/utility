package com.dua3.utility.logging.backend.log4j;

import org.apache.logging.log4j.spi.LoggerContext;
import org.jspecify.annotations.Nullable;

import java.net.URI;

/**
 * A factory class for creating LogEntryDispatcher instances using Log4j.
 */
public class Log4jLoggerContextFactory implements org.apache.logging.log4j.spi.LoggerContextFactory {

    private static final LoggerContext CONTEXT = new Log4jLoggerContext();

    /**
     * Constructor, called by SPI.
     */
    public Log4jLoggerContextFactory() { /* nothing to do */ }

    @Override
    public LoggerContext getContext(String fqcn, @Nullable ClassLoader loader, @Nullable Object externalContext, boolean currentContext) {
        return CONTEXT;
    }

    @Override
    public LoggerContext getContext(String fqcn, @Nullable ClassLoader loader, @Nullable Object externalContext, boolean currentContext, @Nullable URI configLocation, @Nullable String name) {
        return CONTEXT;
    }

    @Override
    public void removeContext(LoggerContext context) {
        // nop
    }
}
