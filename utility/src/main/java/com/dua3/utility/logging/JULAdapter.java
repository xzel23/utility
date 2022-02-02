package com.dua3.utility.logging;

import com.dua3.cabe.annotations.NotNull;

import java.util.Optional;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Adapter class for java.util.logging.
 */
public final class JULAdapter {

    private JULAdapter() {
    }

    /**
     * {@link Handler} implementation that exports {@link LogRecord} instances to {@link LogListener}s.
     */
    public static class JULHandler extends Handler {

        private LogListener listener;

        public JULHandler(@NotNull LogListener listener) {
            this.listener = listener;    
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

        LogListener getListener() {
            return listener;
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
        JULLogEntry(@NotNull LogRecord r) {
            this.r = r;
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
            if ( intLevel == Level.SEVERE.intValue()) {
                return Category.SEVERE;
            }
            return Category.FATAL;
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
    public static LogEntry toLogEntry(@NotNull LogRecord r) {
        return new JULLogEntry(r);
    }

    /**
     * Add a {@link LogListener} to the root {@link Logger} instance.
     * This is a convenience method that simply calls {@link #addListener(LogListener, Logger)} with the root logger.
     * @param listener the listener
     */
    public static void addListener(@NotNull LogListener listener) {
        addListener(listener, Logger.getLogger(""));
    }
    
    /**
     * Add a {@link LogListener} to a {@link Logger} instance.
     * @param listener the listener
     * @param logger the logger
     */
    public static void addListener(@NotNull LogListener listener, @NotNull Logger logger) {
        logger.addHandler(new JULHandler(listener));
    }

    /**
     * Remove a {@link LogListener} from the root {@link Logger}.
     * @param listener the listener
     */
    public static void removeListener(@NotNull LogListener listener) {
        removeListener(listener, Logger.getLogger(""));
    }
    
    /**
     * Remove a {@link LogListener} from a {@link Logger}.
     * @param listener the listener
     * @param logger the logger
     */
    public static void removeListener(@NotNull LogListener listener, @NotNull Logger logger) {
        for (Handler handler: logger.getHandlers()) {
            if (handler instanceof JULHandler julHandler) {
                if (listener==julHandler.getListener()) {
                    logger.removeHandler(julHandler);
                }
            }
        }
        logger.addHandler(new JULHandler(listener));
    }
}
