package com.dua3.utility.logging;

import java.util.Objects;
import java.util.Optional;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Adapter class for java.util.logging.
 */
public final class JULAdapter {

    /**
     * {@link Handler} implementation that exports {@link LogRecord} instances to {@link LogListener}s.
     */
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

    /**
     * Wrapper that wraps a {@link LogRecord} as a {@link LogEntry}.
     */
    public static class JULLogEntry extends AbstractLogEntry<LogRecord> {
        private final LogRecord r;

        /**
         * Constructor.
         * @param r the {@link LogRecord} to wrap.
         */
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

    /**
     * Convert {@link LogRecord} to {@link LogEntry}.
     * @param r the {@link LogRecord} to convert
     * @return LogEntry instance
     */
    public static LogEntry toLogEntry(LogRecord r) {
        return new JULLogEntry(r);
    }

    /**
     * Add a {@link LogListener} to a {@link Logger} instance
     * @param logger the logger
     * @param listener the listener
     */
    public static void addListener(Logger logger, LogListener listener) {
        logger.addHandler(new JULHandler(listener));
    }

    /**
     * Remove a {@link LogListener} from a {@link Logger}.
     * @param logger the logger
     * @param listener the listener
     */
    public static void removeListener(Logger logger, LogListener listener) {
        for (Handler handler: logger.getHandlers()) {
            if (handler instanceof JULHandler) {
                JULHandler julHandler = (JULHandler) handler;
                if (listener==julHandler.listener) {
                    logger.removeHandler(julHandler);
                }
            }
        }
        logger.addHandler(new JULHandler(listener));
    }
}
