package com.dua3.utility.logging;

import java.io.IOException;
import java.util.*;
import java.util.function.Supplier;
import java.util.logging.*;

public final class LogUtil {

    private static final Logger LOG = Logger.getLogger(LogUtil.class.getName());
    
    private LogUtil() {
        //nop
    }

    /**
     * Helper class to create lazy 'toString()' evaluators for logging.
     */
    private static final class LazyToString implements Supplier<String> {
        private final Supplier<String> base;
        private String msg = null;

        LazyToString(Supplier<String> s) {
            this.base = Objects.requireNonNull(s);
        }

        @Override
        public String get() {
            if (msg==null) {
                msg = base.get();
            }
            return msg;
        }

        @Override
        public String toString() {
            return get();
        }
    }

    /**
     * Create a wrapper around a {@link Supplier} that delegates {@code #toString()} to {@link Supplier#get()}.
     * {@link Supplier#get()} is only evaluated in case {@link #toString()} is called and the result is cached for
     * further invocations (provided {@link Supplier#get()} does not return {@code null}).
     * @param s the supplier
     * @return an Object that acts as a proxy for calls to toString()
     */
    public static Supplier<String> formatLazy(Supplier<String> s) {
        return new LazyToString(s);
    }

    public static final String DEFAULT_FORMAT = "%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS %4$-6s %2$s %5$s%6$s%n";

    /**
     * Command line option to set the global log level.
     */
    public static final String ARG_LOG_LEVEL = "--log-level=";

    /**
     * Command line option to set the global log path pattern.
     * See {@link FileHandler#FileHandler(String)} for pattern syntax.
     */
    public static final String ARG_LOG_PATH_PATTERN = "--log-path-pattern=";

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

        // set log format
        if (System.getProperty("java.util.logging.SimpleFormatter.format")==null) {
            System.setProperty("java.util.logging.SimpleFormatter.format", DEFAULT_FORMAT);
            LOG.info("default log format changed to 1-line output");
        }
        
        // set log handlers first (because it should be done before setting levels)
        for (int i=0; i<argList.size(); i++) {
            String arg = argList.get(i);
            if (arg.startsWith(ARG_LOG_PATH_PATTERN)) {
                // set log level
                String value = arg.substring(ARG_LOG_PATH_PATTERN.length());
                try {
                    LogUtil.setLogPath(value);
                } catch (IOException e) {
                    LOG.log(Level.WARNING, "could not set log pattern", e);
                }

                // remove from list of arguments
                //noinspection AssignmentToForLoopParameter
                argList.remove(i--);
            }
        }
        
        // now set level
        Level level = defaultLevel;
        for (int i=0; i<argList.size(); i++) {
            String arg = argList.get(i);
            if (arg.startsWith(ARG_LOG_LEVEL)) {
                // set log level
                String value = arg.substring(ARG_LOG_LEVEL.length());
                level = Level.parse(value);

                // remove from list of arguments
                //noinspection AssignmentToForLoopParameter
                argList.remove(i--);
            }
        }
        LogUtil.setLogLevel(level);

        return argList.toArray(String[]::new);
    }

    private static Logger getRootLogger() {
        return LogManager.getLogManager().getLogger("");
    }

    /**
     * Set log path pattern.
     * See {@link FileHandler#FileHandler(String)} for pattern syntax.
     * @param pattern the pattern to use
     */
    private static void setLogPath(String pattern) throws IOException {
        Handler fh = new FileHandler(pattern);
        fh.setFormatter(new SimpleFormatter());
        getRootLogger().addHandler (fh);
        LOG.info("log path set to "+pattern);
    }

    /**
     * Set java.util.logging log level for the root logger.
     *
     * @param level the log level to set
     */
    public static void setLogLevel(Level level) {
        Logger rootLogger = getRootLogger();
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
        LOG.info(() -> "logger '%s': log level set to '%s'".formatted(logger.getName(), level));
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
