package com.dua3.utility.logging;

import java.util.Objects;
import java.util.Optional;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public final class JULAdapter {
    
    public static class JULHandler extends Handler {

        private LogListener listener;

        public JULHandler(LogListener listener) {
            this.listener = Objects.requireNonNull(listener);    
        }
        
        @Override
        public void publish(LogRecord r) {
            if (listener!=null) {
                listener.entry(toLogEntry(r));
            }
        }

        @Override
        public void flush() {
            // nop
        }

        @Override
        public void close() throws SecurityException {
            listener = null;
        }
    }
    
    public static class JULLogEntry extends AbstractLogEntry<LogRecord> {
        private final LogRecord r;

        JULLogEntry(LogRecord r) {
            this.r = Objects.requireNonNull(r);
        }

        @Override
        public Category category() {
            int intLevel = r.getLevel().intValue();
            if (intLevel < Level.FINE.intValue()) {
                return Category.TRACE;
            }
            if ( intLevel < Level.INFO.intValue()) {
                return Category.DEBUG;
            }
            if ( intLevel < Level.WARNING.intValue()) {
                return Category.INFO;
            }
            if ( intLevel < Level.SEVERE.intValue()) {
                return Category.WARNING;
            }
            return Category.SEVERE;
        }

        @Override
        public String level() {
            return r.getLevel().toString();
        }

        @Override
        public String logger() {
            return r.getLoggerName();
        }

        @Override
        public long millis() {
            return r.getMillis();
        }

        @Override
        public String message() {
            return r.getMessage();
        }

        @Override
        public Optional<IThrowable> cause() {
            return Optional.ofNullable(r.getThrown()).map(IThrowable.JavaThrowable::new);
        }

        @Override
        public LogRecord getNative() {
            return r;
        }
    }

    public static LogEntry toLogEntry(LogRecord r) {
        return new JULLogEntry(r);
    }

    public static void addListener(Logger logger, LogListener listener) {
        logger.addHandler(new JULHandler(listener));
    }
}
