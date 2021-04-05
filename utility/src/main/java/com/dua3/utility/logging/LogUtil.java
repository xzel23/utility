package com.dua3.utility.logging;

import java.util.Locale;
import java.util.function.Supplier;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public final class LogUtil {
    
    private LogUtil() {
        //nop
    }

    /**
     * Set java.util.logging log level for the root logger.
     *
     * @param level the log level to set
     */
    public static void setLogLevel(Level level) {
        Logger rootLogger = LogManager.getLogManager().getLogger("");
        setLogLevel(level, rootLogger);
    }

    /**
     * Set java.util.logging log level.
     *
     * @param level
     *               the log level to set
     * @param logger
     *               the logger for which to set the level
     */
    public static void setLogLevel(Level level, Logger logger) {
        logger.setLevel(level);
        for (Handler h : logger.getHandlers()) {
            h.setLevel(level);
        }
    }

    /**
     * Set java.util.logging log level.
     *
     * @param level
     *                the log level to set
     * @param loggers
     *                the loggers to set the level for
     */
    public static void setLogLevel(Level level, Logger... loggers) {
        for (Logger logger : loggers) {
            setLogLevel(level, logger);
        }
    }

    /**
     * Create a log message supplier.
     *
     * @param  fmt  format, {@link String#format(Locale, String, Object...)} with
     *              the
     *              root locale
     * @param  args arguments
     * @return      a supplier that returns the formatted message
     */
    public static Supplier<String> format(String fmt, Object... args) {
        return () -> String.format(Locale.ROOT, fmt, args);
    }
}
