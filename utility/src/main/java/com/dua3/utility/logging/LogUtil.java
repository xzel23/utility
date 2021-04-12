package com.dua3.utility.logging;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public final class LogUtil {
    
    private static final Logger LOG = Logger.getLogger(LogUtil.class.getName());
    
    private LogUtil() {
        //nop
    }

    /**
     * option to parse on the command line to set the global log level.
     */
    public static final String ARG_LOG_LEVEL = "--log-level=";

    /**
     * Utility method to set global log level at program starttup. The argument list is scanned for arguments
     * in the form of {@code --log-level=<level>}. The global log level is then set to the last found value, or
     * to the default, if no matching argument was found.
     * @param defaultLevel  the default level to use when no explicit level is set on the command line
     * @param args          the command line args
     * @return              the command line args with arguments for setting the log level filtered out
     */
    public static String[] handleLoggingCmdArgs(Level defaultLevel, String... args) {
        List<String> argList = new ArrayList<>(Arrays.asList(args));
        
        Level level = defaultLevel;
        for (int i=0; i<argList.size(); i++) {
            String arg = argList.get(i);
            if (arg.startsWith(ARG_LOG_LEVEL)) {
                // set log level
                String value = arg.substring(ARG_LOG_LEVEL.length());
                level = Level.parse(value);

                // remove from list of arguments
                argList.remove(i--);
            }
        }
        LogUtil.setLogLevel(level);
        LOG.info(format("global log level set to '%s'", level));

        return argList.toArray(String[]::new);
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
