package com.dua3.utility.logging;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.util.Objects;
import java.util.Optional;

/**
 * A utility class to connect Log4j2 to {@link LogBuffer}.
 */
public final class Log4jAdapter {

    private Log4jAdapter() {
    }

    private static class Log4jLogEntry extends AbstractLogEntry<LogEvent> {
        private final LogEvent evt;

        Log4jLogEntry(LogEvent evt) {
            this.evt = Objects.requireNonNull(evt.toImmutable());
        }

        @Override
        public Category category() {
            int intLevel = evt.getLevel().intLevel();
            if (intLevel >= Level.TRACE.intLevel()) {
                return Category.TRACE;
            }
            if ( intLevel >= Level.DEBUG.intLevel()) {
                return Category.DEBUG;
            }
            if ( intLevel >= Level.INFO.intLevel()) {
                return Category.INFO;
            }
            if ( intLevel >= Level.WARN.intLevel()) {
                return Category.WARNING;
            }
            if ( intLevel >= Level.ERROR.intLevel()) {
                return Category.SEVERE;
            }
            return Category.FATAL;
        }

        @Override
        public String level() {
            return evt.getLevel().toString();
        }

        @Override
        public String logger() {
            return evt.getLoggerName();
        }

        @Override
        public long millis() {
            return evt.getInstant().getEpochMillisecond();
        }

        @Override
        public String message() {
            return evt.getMessage().getFormattedMessage();
        }

        @Override
        public Optional<IThrowable> cause() {
            return Optional.ofNullable(evt.getThrown()).map(IThrowable.JavaThrowable::new);
        }

        @Override
        public LogEvent getNative() {
            return evt;
        }
    }

    /**
     * Convert Logback logging event to log entry.
     * @param evt the log4j2 logging event
     * @return log entry
     */
    public static LogEntry toLogEntry(LogEvent evt) {
        return new Log4jLogEntry(evt);
    }

    /**
     * Add a listener to the root Logger instance.
     * @param listener the listener
     */
    public static void addListener(LogListener listener) {
        addListener(listener, LogManager.getRootLogger());
    }
    
    /**
     * Add a listener to a log4j2 Logger instance.
     * @param listener the listener
     * @param logger the logger
     */
    public static void addListener(LogListener listener, Logger logger) {
        LoggerContext context = LoggerContext.getContext(false);
        Configuration config = context.getConfiguration();
        PatternLayout layout = PatternLayout.createDefaultLayout(config);
        Appender appender = new AbstractAppender(getAppenderName(logger), null, layout, false, null) {
            @Override
            public void append(LogEvent event) {
                listener.entry(toLogEntry(event));
            }
        };
        appender.start();
        org.apache.logging.log4j.core.Logger coreLogger = (org.apache.logging.log4j.core.Logger) logger;
        coreLogger.setLevel(Level.ALL);
        updateLoggers(appender, config);
    }

    private static String getAppenderName(Logger logger) {
        return logger.getName() + "[Log4j]";
    }

    private static void updateLoggers(final Appender appender, final Configuration config) {
        Level level = null;
        Filter filter = null;
        for (LoggerConfig loggerConfig : config.getLoggers().values()) {
            loggerConfig.setLevel(Level.ALL);
            loggerConfig.addAppender(appender, level, filter);
        }
        config.getRootLogger().addAppender(appender, level, filter);
        config.getRootLogger().setLevel(Level.ALL);
    }
    
    /**
     * Remove a listener from the root instance.
     * @param listener the listener
     */
    public static void removeListener(LogListener listener) {
        removeListener(listener, LogManager.getRootLogger());
    }
    
    /**
     * Remove a listener from a log4j2 Logger instance.
     * @param listener the listener
     * @param logger the logger
     */
    public static void removeListener(LogListener listener, Logger logger) {
        LoggerContext context = LoggerContext.getContext(false);
        Configuration config = context.getConfiguration();
        for (LoggerConfig loggerConfig : config.getLoggers().values()) {
            loggerConfig.removeAppender(getAppenderName(logger));
        }
    }

}
