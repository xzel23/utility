package com.dua3.utility.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.AppenderBase;
import org.slf4j.Logger;

import java.util.*;

/**
 * A utility class to connect Logback to {@link LogBuffer}.
 * 
 * @see "http://logback.qos.ch"
 */
public final class LogbackAdapter {
    
    private static class LogbackLogAppender extends AppenderBase<ILoggingEvent> {
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

    private static class LogbackLogEntry extends AbstractLogEntry<ILoggingEvent> {
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
            return evt.getFormattedMessage();
        }

        @Override
        public Optional<IThrowable> cause() {
            return Optional.ofNullable(evt.getThrowableProxy()).map(SLF4JThrowable::new);
        }

        @Override
        public ILoggingEvent getNative() {
            return evt;
        }

        private static class SLF4JThrowable implements IThrowable {
            private final IThrowableProxy tp;
            private List<IStackTraceElement> ist=null;

            private SLF4JThrowable(IThrowableProxy tp) {
                this.tp = Objects.requireNonNull(tp);
            }

            @Override
            public IThrowable getCause() {
                IThrowableProxy cause = tp.getCause();
                return cause==null ? null : new SLF4JThrowable(cause);
            }

            @Override
            public List<IStackTraceElement> getStackTrace() {
                if (ist==null) {
                    StackTraceElementProxy[] st = tp.getStackTraceElementProxyArray();
                    List<IStackTraceElement> ist_ = new ArrayList<>(st.length);
                    for (StackTraceElementProxy ste: st) {
                        ist_.add(new SLF4JStackTraceElement(ste));
                    }
                    ist = Collections.unmodifiableList(ist_);
                }
                return ist;
            }

            @Override
            public String toString() {
                return tp.getClassName()+": "+tp.getMessage();
            }
        }

        private static class SLF4JStackTraceElement implements IThrowable.IStackTraceElement {
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

    /**
     * Convert Logback logging event to log entry.
     * @param evt the logback logging event
     * @return log entry
     */
    public static LogEntry toLogEntry(ILoggingEvent evt) {
        return new LogbackLogEntry(evt);
    }

    /**
     * Add a listener to a Logback Logger instance.
     * @param logger the logger
     * @param listener the listener
     */
    public static void addListener(Logger logger, LogListener listener) {
        if (logger instanceof ch.qos.logback.classic.Logger) {
            LogbackLogAppender appender = new LogbackLogAppender(listener);
            appender.start();
            ((ch.qos.logback.classic.Logger)logger).addAppender(appender);
        }
    }

    /**
     * Remove a listener from a Logback Logger instance.
     * @param logger the logger
     * @param listener the listener
     */
    public static void removeListener(Logger logger, LogListener listener) {
        if (logger instanceof ch.qos.logback.classic.Logger) {
            ch.qos.logback.classic.Logger logbackLogger = ((ch.qos.logback.classic.Logger)logger);
            Iterator<Appender<ILoggingEvent>> iter = logbackLogger.iteratorForAppenders();
            while (iter.hasNext()) {
                Appender<ILoggingEvent> appender = iter.next();
                if (appender instanceof LogbackLogAppender) {
                    LogbackLogAppender logbackAppender = (LogbackLogAppender) appender;
                    if (logbackAppender.listener==listener) {
                        logbackAppender.stop();
                        logbackLogger.detachAppender(logbackAppender);
                    }
                }
            }
        }
    }
}
