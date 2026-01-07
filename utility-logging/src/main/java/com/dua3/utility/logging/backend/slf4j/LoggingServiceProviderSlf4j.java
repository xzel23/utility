package com.dua3.utility.logging.backend.slf4j;

import org.slf4j.ILoggerFactory;
import org.slf4j.IMarkerFactory;
import org.slf4j.helpers.BasicMarkerFactory;
import org.slf4j.helpers.NOPMDCAdapter;
import org.slf4j.helpers.NOP_FallbackServiceProvider;
import org.slf4j.spi.MDCAdapter;
import org.slf4j.spi.SLF4JServiceProvider;

/**
 * Implementation of SLF4JServiceProvider that provides logging functionality using SLF4J.
 */
public class LoggingServiceProviderSlf4j implements SLF4JServiceProvider {

    /**
     * Constructor, called by SPI
     */
    public LoggingServiceProviderSlf4j() { /* nothing to do */ }

    private final LoggerFactorySlf4j loggerFactory = new LoggerFactorySlf4j();
    private final IMarkerFactory markerFactory = new BasicMarkerFactory();
    private final MDCAdapter mdcAdapter = new NOPMDCAdapter();

    @Override
    public ILoggerFactory getLoggerFactory() {
        return loggerFactory;
    }

    @Override
    public IMarkerFactory getMarkerFactory() {
        return markerFactory;
    }

    @Override
    public MDCAdapter getMDCAdapter() {
        return mdcAdapter;
    }

    @Override
    public String getRequestedApiVersion() {
        return NOP_FallbackServiceProvider.REQUESTED_API_VERSION;
    }

    @Override
    public void initialize() { /* nothing to do */ }
}
