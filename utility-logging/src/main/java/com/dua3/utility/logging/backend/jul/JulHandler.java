package com.dua3.utility.logging.backend.jul;


import com.dua3.utility.logging.backend.universal.UniversalDispatcher;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * Custom Java Util Logging (JUL) {@link Handler} implementation that dispatches log records to
 * the global {@link UniversalDispatcher}.
 * <p>
 * <strong>Note:</strong> This class filters out messages from {@code java.*}, {@code javax.*},
 * and {@code sun.*} packages with {@link Level#FINE} or below.
 */
public class JulHandler extends Handler {

    private static final UniversalDispatcher DISPATCHER = UniversalDispatcher.getInstance();

    @Override
    public void publish(LogRecord logRecord) {
        // filter out Java messages with FINE level coming in over JUL
        // these are usually not of interest and when these are handled, they
        // often trigger other message while being processed leading to a DOS situation
        String loggerName = logRecord.getLoggerName();
        if (logRecord.getLevel().intValue() > Level.FINE.intValue() || (
                !loggerName.startsWith("java.")
                        && !loggerName.startsWith("javax.")
                        && !loggerName.startsWith("sun.")
        )) {
            DISPATCHER.dispatchJul(logRecord);
        }
    }

    @Override
    public void flush() { /* nothing to do */ }

    @Override
    public void close() throws SecurityException { /* nothing to do */ }
}