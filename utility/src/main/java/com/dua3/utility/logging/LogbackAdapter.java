package com.dua3.utility.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.core.AppenderBase;
import org.slf4j.Logger;

import java.util.Objects;

public class LogbackAdapter {
    
    public static class LogbackLogAppender extends AppenderBase<ILoggingEvent> {
        private final LogListener listener;
        
        @Override
        protected void append(ILoggingEvent evt) {
            LogEntry entry = toLogEntry(evt);
            listener.entry(entry);    
        }

        LogbackLogAppender(LogListener listener) {
            this.listener = Objects.requireNonNull(listener);
        }
    }

    public static class LogbackLogEntry extends AbstractLogEntry<ILoggingEvent> {
        private final ILoggingEvent evt;

        LogbackLogEntry(ILoggingEvent evt) {
            this.evt = Objects.requireNonNull(evt);
        }

        @Override
        public Category category() {
            int intLevel = evt.getLevel().levelInt;
            if ( intLevel < Level.DEBUG_INT) {
                return Category.TRACE;
            }
            if ( intLevel < Level.INFO_INT) {
                return Category.DEBUG;
            }
            if ( intLevel < Level.WARN_INT) {
                return Category.INFO;
            }
            if ( intLevel < Level.ERROR_INT) {
                return Category.WARNING;
            }
            return Category.SEVERE;
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
            return evt.getTimeStamp();
        }

        @Override
        public String message() {
            return evt.getMessage();
        }

        @Override
        public StackTraceElement[] stacktrace() {
            IThrowableProxy t = evt.getThrowableProxy();
            if (t==null) {
                return new StackTraceElement[0];
            }

            StackTraceElementProxy[] step = t.getStackTraceElementProxyArray();
            StackTraceElement[] ste = new StackTraceElement[step.length];
            for (int i=0; i<step.length; i++) {
                ste[i] = step[i].getStackTraceElement();
            }
            return ste;
        }

        @Override
        public ILoggingEvent getNative() {
            return evt;
        }
    }

    public static LogEntry toLogEntry(ILoggingEvent evt) {
        return new LogbackLogEntry(evt);
    }

    public static void addListener(Logger logger, LogListener listener) {
        if (logger instanceof ch.qos.logback.classic.Logger) {
            LogbackLogAppender appender = new LogbackLogAppender(listener);
            appender.start();
            ((ch.qos.logback.classic.Logger)logger).addAppender(appender);
        }
    }
}
