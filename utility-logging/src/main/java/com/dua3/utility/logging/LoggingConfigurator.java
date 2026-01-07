package com.dua3.utility.logging;

import com.dua3.utility.lang.LangUtil;
import org.jspecify.annotations.NonNull;
import org.slf4j.event.Level;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.function.Consumer;

public class LoggingConfigurator {

    /**
     * Specifies the logging level for the {@code LoggerFactorySlf4j}.
     *
     * <p>This variable defines the configuration key used to set the logging level
     * across the application. It is typically read from a properties file during
     * the initialization of the logger factory.
     *
     * <p>Possible values include logging levels such as "DEBUG", "INFO", "WARN", "ERROR", etc.
     */
    public static final String LEVEL = "logger.level";

    /**
     * Configuration key used to specify the stream to which log entries are written for console logging.
     *
     * <p>This key can be used to configure the console output stream in the logging properties file,
     * enabling redirection of log entries to different streams such as System.out or System.err.
     */
    public static final String LOGGER_CONSOLE_STREAM = "logger.console.stream";

    /**
     * Property key to enable or disable colored output for the console logger.
     */
    public static final String LOGGER_CONSOLE_COLORED = "logger.console.colored";

    private static final String SYSTEM_OUT = "system.out";
    private static final String SYSTEM_ERR = "system.err";

    public static void configure(Properties properties, Consumer<LogEntryFilter> setFilter, Consumer<LogEntryHandler> addHandler) {
        LoggingConfigurator configurator = new LoggingConfigurator();
        configurator.doConfigure(properties, setFilter, addHandler);
    }

    public LoggingConfigurator() {
    }

    private void doConfigure(Properties properties, Consumer<LogEntryFilter> setFilter, Consumer<LogEntryHandler> setHandler) {
        LogFilter filter = configureFilter(properties);
        setFilter.accept(filter);

        List<LogEntryHandler> handlers = configureHandlers(properties);
        handlers.forEach(setHandler);
    }

    private static @NonNull LogFilter configureFilter(Properties properties) {
        LogFilter filter = new LogFilter();

        // parse log level entry
        String levelDeclaration = properties.getProperty(LEVEL, Level.INFO.name());
        String[] decls = levelDeclaration.split(",");

        LogLevel rootLevel = decls.length > 0 ? LogLevel.valueOf(decls[0].strip()) : LogLevel.INFO;
        filter.setLevel(rootLevel);

        Arrays.stream(decls)
                .skip(1) // root level has already been extracted
                .forEachOrdered(s -> {
                    String[] parts = s.split(":");
                    LangUtil.check(parts.length == 2, "invalid log level declaration: %s", s);
                    String prefix = parts[0].strip();
                    LogLevel level = LogLevel.valueOf(parts[1].strip());

                    filter.setLevel(prefix, level);
                });
        return filter;
    }

    private List<LogEntryHandler> configureHandlers(Properties properties) {
        List<LogEntryHandler> handlers = new ArrayList<>();

        String propertyConsoleStream = properties.getProperty(LOGGER_CONSOLE_STREAM, "").trim().toLowerCase(Locale.ROOT);
        final PrintStream stream = switch (propertyConsoleStream) {
            case "" -> null;
            case SYSTEM_ERR -> //noinspection UseOfSystemOutOrSystemErr
                    System.err;
            case SYSTEM_OUT -> //noinspection UseOfSystemOutOrSystemErr
                    System.out;
            default ->
                    throw new IllegalArgumentException("invalid value for property " + LOGGER_CONSOLE_STREAM + ": '" + propertyConsoleStream + "'");
        };

        String propertyConsoleColored = properties.getProperty(LOGGER_CONSOLE_COLORED, "auto").trim().toLowerCase(Locale.ROOT);
        final boolean colored = switch (propertyConsoleColored) {
            case "true" -> true;
            case "false" -> false;
            // "auto" enables colored output when a terminal is attached and the TERM environment variable is set
            case "auto" -> System.console() != null && System.getenv().get("TERM") != null;
            default ->
                    throw new IllegalArgumentException("invalid value for property " + LOGGER_CONSOLE_COLORED + ": '" + propertyConsoleColored + "'");
        };

        if (stream != null) {
            handlers.add(new ConsoleHandler(stream, colored));
        }

        return handlers;
    }

}
