package com.dua3.utility.logging.slf4j;

import com.dua3.utility.data.Pair;
import com.dua3.utility.lang.LangUtil;
import com.dua3.utility.logging.LogEntryFilter;
import com.dua3.utility.logging.LogEntryHandler;
import com.dua3.utility.logging.ConsoleHandler;
import com.dua3.utility.logging.LogEntryDispatcher;
import org.slf4j.ILoggerFactory;
import org.slf4j.event.Level;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

/**
 * The LoggerFactorySlf4j class is an implementation of the ILoggerFactory and LogEntryDispatcher interfaces.
 */
public class LoggerFactorySlf4j implements ILoggerFactory, LogEntryDispatcher {
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

    private final List<Pair<String, Level>> prefixes = new ArrayList<>();
    private final List<WeakReference<LogEntryHandler>> handlers = new ArrayList<>();

    private final LogEntryHandler defaultHandler;
    private volatile LogEntryFilter filter;

    /**
     * Constructs a new instance of LoggerFactorySlf4j.
     *
     * <p>The constructor initializes logging properties from a properties file,
     * sets the default logging level, configures logging prefixes and console handlers.
     *
     * <p>The initialization process includes:
     * <ol>
     * <li> Loading properties from the logging properties file.
     * <li> Parsing the log level declaration and setting the global/default log level.
     * <li> Configuring specific log levels for various log message prefixes.
     * <li> Setting up console handlers based on properties for console stream and colored output.
     * </ol>
     *
     * @throws IllegalArgumentException if an invalid logging configuration is detected.
     */
    public LoggerFactorySlf4j() {
        Properties properties = getProperties();

        // parse log level entry
        String levelDeclaration = properties.getProperty(LEVEL, Level.INFO.name());
        String[] decls = levelDeclaration.split(",");

        if (decls.length > 0) {
            LoggerSlf4j.setDefaultLevel(Level.valueOf(decls[0].strip()));
        }

        Arrays.stream(decls)
                .skip(1) // global level has already been set
                .forEachOrdered(s -> {
                    String[] parts = s.split(":");
                    LangUtil.check(parts.length == 2, "invalid log level declaration: %s", s);
                    String prefix = parts[0].strip();
                    Level level = Level.valueOf(parts[1].strip());

                    var entry = getPrefixEntry(prefix);
                    LangUtil.check(entry.isEmpty(), () -> new IllegalStateException("prefix '%s' is shadowed by '%s'".formatted(prefix, entry.orElseThrow().first())));

                    prefixes.add(Pair.of(prefix, level));
                });

        // configure console handler
        String propertyConsoleStream = properties.getProperty(LOGGER_CONSOLE_STREAM, "").trim().toLowerCase(Locale.ROOT);
        final PrintStream stream = switch (propertyConsoleStream) {
            case "" -> null;
            case "system.err" -> System.err;
            case "system.out" -> System.out;
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
        this.defaultHandler = stream != null ? new ConsoleHandler(stream, colored) : null;
        if (defaultHandler != null) {
            handlers.add(new WeakReference<>(defaultHandler));
        }
    }

    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    private static Properties getProperties() {
        Properties properties = new Properties();
        try (InputStream in = ClassLoader.getSystemResourceAsStream("logging.properties")) {
            if (in == null) {
                properties.setProperty(LOGGER_CONSOLE_STREAM, "system.out");
            } else {
                properties.load(in);
            }
        } catch (IOException e) {
            properties.setProperty(LOGGER_CONSOLE_STREAM, "system.out");
            e.printStackTrace(System.err);
        }
        return properties;
    }

    private Optional<Pair<String, Level>> getPrefixEntry(String name) {
        return prefixes.stream().filter(p -> name.startsWith(p.first())).findFirst();
    }

    private Level getLevel(String name) {
        return getPrefixEntry(name).map(Pair::second).orElseGet(LoggerSlf4j::getDefaultLevel);
    }

    @Override
    public org.slf4j.Logger getLogger(String name) {
        LoggerSlf4j logger = new LoggerSlf4j(name, handlers);
        logger.setLevel(getLevel(name));
        return logger;
    }

    @Override
    public void addLogEntryHandler(LogEntryHandler handler) {
        handlers.add(new WeakReference<>(handler));
    }

    @Override
    public void removeLogEntryHandler(LogEntryHandler handler) {
        handlers.removeIf(h -> h.get() == handler);
    }

    @Override
    public void setFilter(LogEntryFilter filter) {
        this.filter = filter;
    }

    @Override
    public LogEntryFilter getFilter() {
        return filter;
    }

    @Override
    public Collection<LogEntryHandler> getLogEntryHandlers() {
        return handlers.stream().map(WeakReference::get).filter(Objects::nonNull).toList();
    }

    /**
     * Returns the default {@code LogEntryHandler}.
     *
     * @return the default log entry handler
     */
    public LogEntryHandler getDefaultHandler() {
        return defaultHandler;
    }
}
