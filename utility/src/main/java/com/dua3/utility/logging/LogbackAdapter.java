package com.dua3.utility.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.AppenderBase;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

public final class LogbackAdapter {
    
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
            int intLevel = evt.getLevel().toInt();
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
        public Optional<IThrowable> cause() {
            return Optional.ofNullable(evt.getThrowableProxy()).map(tp -> new SLF4JThrowable(tp));
        }

        @Override
        public ILoggingEvent getNative() {
            return evt;
        }

        private class SLF4JThrowable implements IThrowable {
            private final IThrowableProxy tp;
            private IStackTraceElement[] ist=null;

            private SLF4JThrowable(IThrowableProxy tp) {
                this.tp = Objects.requireNonNull(tp);
            }

            @Override
            public IThrowable getCause() {
                IThrowableProxy cause = tp.getCause();
                return cause==null ? null : new SLF4JThrowable(cause);
            }

            @Override
            public IStackTraceElement[] getStackTrace() {
                if (ist==null) {
                    StackTraceElementProxy[] st = tp.getStackTraceElementProxyArray();
                    IStackTraceElement[] ist_ = new IStackTraceElement[st.length];
                    for (int i = 0; i < st.length; i++) {
                        ist_[i] = new SLF4JStackTraceElement(st[i]);
                    }
                    ist = ist_;
                }
                return ist;
            }

            @Override
            public String toString() {
                return tp.getClassName()+": "+tp.getMessage();
            }
        }

        private class SLF4JStackTraceElement implements IThrowable.IStackTraceElement {
            private final StackTraceElementProxy step;

            private SLF4JStackTraceElement(StackTraceElementProxy step) {
                this.step = step;
            }

            @Override
            public String toString() {
                return step.getStackTraceElement().toString();
            }
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
