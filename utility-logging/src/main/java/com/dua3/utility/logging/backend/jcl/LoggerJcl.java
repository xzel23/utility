package com.dua3.utility.logging.backend.jcl;

import com.dua3.utility.logging.LogLevel;
import com.dua3.utility.logging.backend.universal.UniversalDispatcher;
import org.apache.commons.logging.Log;

/**
 * LoggerJcl is an implementation of the Apache commons Log interfac that forwards all logging
 * calls to the global universal dispatcher instance.
 */
public class LoggerJcl implements Log {
    private static final UniversalDispatcher DISPATCHER = UniversalDispatcher.getInstance();

    private final String name;

    /**
     * Creates an instance of the LoggerJcl class with the specified logger name.
     *
     * @param name the name of the logger
     */
    public LoggerJcl(String name) {
        this.name = name;
    }

    @Override
    public void debug(Object message) {
        DISPATCHER.dispatchJcl(name, LogLevel.DEBUG, message, null);
    }

    @Override
    public void debug(Object message, Throwable t) {
        DISPATCHER.dispatchJcl(name, LogLevel.DEBUG, message, t);
    }

    @Override
    public void error(Object message) {
        DISPATCHER.dispatchJcl(name, LogLevel.ERROR, message, null);
    }

    @Override
    public void error(Object message, Throwable t) {
        DISPATCHER.dispatchJcl(name, LogLevel.ERROR, message, t);
    }

    @Override
    public void fatal(Object message) {
        DISPATCHER.dispatchJcl(name, LogLevel.ERROR, message, null);
    }

    @Override
    public void fatal(Object message, Throwable t) {
        DISPATCHER.dispatchJcl(name, LogLevel.ERROR, message, t);
    }

    @Override
    public void info(Object message) {
        DISPATCHER.dispatchJcl(name, LogLevel.INFO, message, null);
    }

    @Override
    public void info(Object message, Throwable t) {
        DISPATCHER.dispatchJcl(name, LogLevel.INFO, message, t);
    }

    @Override
    public boolean isDebugEnabled() {
        return DISPATCHER.isEnabled(LogLevel.DEBUG);
    }

    @Override
    public boolean isErrorEnabled() {return DISPATCHER.isEnabled(LogLevel.ERROR);}

    @Override
    public boolean isFatalEnabled() {
        return DISPATCHER.isEnabled(LogLevel.ERROR);
    }

    // Implement warn, debug, trace, fatal similarly...
    @Override
    public boolean isInfoEnabled() {return DISPATCHER.isEnabled(LogLevel.INFO);}

    @Override
    public boolean isTraceEnabled() {
        return DISPATCHER.isEnabled(LogLevel.TRACE);
    }

    @Override
    public boolean isWarnEnabled() {
        return DISPATCHER.isEnabled(LogLevel.WARN);
    }

    @Override
    public void trace(Object message) {
        DISPATCHER.dispatchJcl(name, LogLevel.TRACE, message, null);
    }

    @Override
    public void trace(Object message, Throwable t) {
        DISPATCHER.dispatchJcl(name, LogLevel.TRACE, message, t);
    }

    @Override
    public void warn(Object message) {
        DISPATCHER.dispatchJcl(name, LogLevel.WARN, message, null);
    }

    @Override
    public void warn(Object message, Throwable t) {
        DISPATCHER.dispatchJcl(name, LogLevel.WARN, message, t);
    }

}
