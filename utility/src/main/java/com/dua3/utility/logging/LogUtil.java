package com.dua3.utility.logging;

import com.dua3.utility.lang.LangUtil;
import com.dua3.utility.options.Arguments;
import com.dua3.utility.options.ArgumentsParser;
import com.dua3.utility.options.Flag;
import com.dua3.utility.options.SimpleOption;
import com.dua3.utility.options.StandardOption;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public final class LogUtil {

    private static final Logger LOG = Logger.getLogger(LogUtil.class.getName());
    
    private LogUtil() {
        // nop
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
                msg = Objects.requireNonNull(base.get());
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

    private static final String DEFAULT_FORMAT = "%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS %4$-6s [%2$s] %5$s%6$s\n";

    /**
     * Utility method to set global log level at program startup. The argument list is scanned for arguments
     * that control logging and the log system is set up accordingly.
     * @param args          the command line args
     * @return              the command line args with arguments for setting up logging removed
     */
    public static String[] handleLoggingCmdArgs(String... args) {
        //noinspection UseOfSystemOutOrSystemErr
        return handleLoggingCmdArgs(System.err::println, args);    
    }

    /**
     * Utility method to set global log level at program startup. The argument list is scanned for arguments
     * that control logging and the log system is set up accordingly.
     * @param msgPrinter    print method for displaying usage messages concerning the "-log..." options
     * @param args          the command line args
     * @return              the command line args with arguments for setting up logging removed
     */
    public static String[] handleLoggingCmdArgs(Consumer<String> msgPrinter, String... args) {
        // create parser
        ArgumentsParser parser = new ArgumentsParser("log parser", "parser for command line log options");

        // --log-help
        Flag flagHelp = parser.flag("--log-help");
        
        // --log-level-root
        SimpleOption<Level> optRootLevel = parser.simpleOption(Level::parse, "--log-level-root")
                .description("set root logger level")
                .defaultValue(Level.INFO);

        // --log-level
        StandardOption<String> optLevel = parser.option(String.class, "--log-level").arity(2)
                .description("set log level for logger");

        // --log-path
        SimpleOption<String> optLogPath = parser.simpleOption(String.class, "--log-path")
                .description("set log path pattern");
        
        // --log-format
        SimpleOption<String> optLogFormat = parser.simpleOption(String.class, "--log-format")
                .description("set log format")
                .defaultValue(System.getProperty("java.util.logging.SimpleFormatter.format", DEFAULT_FORMAT));
        
        // parse
        Arguments arguments = parser.parse(args);
        
        // show help
        arguments.ifSet(flagHelp, () -> LOG.info(parser.help()));
        
        // set log format
        String logFormat = arguments.getOrThrow(optLogFormat);
        System.setProperty("java.util.logging.SimpleFormatter.format", logFormat);
        
        // set log path
        arguments.ifPresent(optLogPath, logPath -> {
            try {
                LogUtil.setLogPath(logPath);
            } catch (IOException e) {
                LOG.log(Level.WARNING, "could not set log path to "+logPath, e);
            }
        });

        // set root level
        Level rootLevel = arguments.getOrThrow(optRootLevel);
        LogUtil.setLogLevel(rootLevel);
        
        // set log level
        arguments.forEach(optLevel, params -> {
            LangUtil.check(params.size()==2, "wrong number of arguments for option %s ", optLevel.name());
            String loggerName = params.get(0);
            Level level = Level.parse(params.get(1));
            setLogLevel(level, Logger.getLogger(loggerName));
        });

        // return all unprocessed arguments
        return arguments.positionalArgs().toArray(String[]::new);
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
        fh.setLevel(Level.ALL);
        getRootLogger().addHandler (fh);
        LOG.fine(() -> "log path set to "+pattern);
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
        
        String loggerName = logger.getName();
        if (loggerName.isEmpty()) {
            LOG.fine(() -> "root logger: log level set to '%s'".formatted(level));
        } else {
            LOG.fine(() -> "logger '%s': log level set to '%s'".formatted(loggerName, level));
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

}
