package com.dua3.utility.logging;

import com.dua3.utility.concurrent.SimpleValue;
import com.dua3.utility.concurrent.Value;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

class LoggingConfiguration {
    /**
     * Represents the root property key for the logging configurations.
     */
    public static final String LOGGING_ROOT = "logging";

    /**
     * Constant representing the configuration key for setting the root logging level.
     */
    public static final String LOGGING_ROOT_LEVEL = LOGGING_ROOT + ".level";

    /**
     * Defines the key used to specify the logging handlers configuration in the
     * logging properties.
     */
    public static final String LOGGING_HANDLERS = LOGGING_ROOT + ".handlers";

    /**
     * Configuration key for specifying the properties of log handlers.
     * <p>
     * To configure a handler with name 'name', use {@code LOGGING_HANDLER + "name"}.
     */
    public static final String LOGGING_HANDLER = LOGGING_ROOT + ".handler";

    /**
     * Defines the key used to specify the logging filters configuration in the
     * logging properties.
     */
    public static final String LOGGING_FILTERS = LOGGING_ROOT + ".filters";

    /**
     * Configuration key for specifying the properties of log filters.
     * <p>
     * To configure a handler with name 'name', use {@code LOGGING_HANDLER + "name"}.
     */
    public static final String LOGGING_FILTER = LOGGING_ROOT + ".filter";

    /**
     * A constant representing the key for specifying the handler type in configuration properties.
     */
    public static final String LOGGING_TYPE = "type";

    // *** ConsoleHandler configuration ***

    /**
     * Configuration key for specifying the output stream used by the console logger.
     * <p>
     * Valid values are {@code LoggingConfiguration.SYSTEM_OUT} and
     * {@code LoggingConfiguration.SYSTEM_ERR}.
     */
    public static final String LOGGER_CONSOLE_STREAM = "stream";

    /**
     * Constant representing the standard output stream to configure the console handler stream.
     */
    public static final String SYSTEM_OUT = "system.out";

    /**
     * Constant representing the standard error stream to configure the console handler stream.
     */
    public static final String SYSTEM_ERR = "system.err";

    /**
     * Constant representing the configuration key used to specify whether console logging
     * should include colored output for better readability.
     * <p>
     * Valid values are {@code "true"}, {@code "false"}, and {@code "auto"}.
     * <p>
     * {@code "auto"} will evaluate to {@code "true"} if the JVM is connected to a terminal,
     * otherwise {@code "false"}.
     */
    public static final String LOGGER_CONSOLE_COLORED = "colored";

    /**
     * Constant representing colored output for the console handler.
     */
    public static final String COLOR_ENABLED = "true";

    /**
     * Constant representing non-colored output for the console handler.
     */
    public static final String COLOR_DISABLED = "false";

    /**
     * Constant representing automatic setting colored output for the console handler.
     */
    public static final String COLOR_AUTO = "auto";

    // *** filter configuration ***

    public static final String LEVEL = "level";

    // *** end of configuration constants ***

    private final Value<LogLevel> rootLevel = new SimpleValue<>(LogLevel.INFO);
    private final Map<String, LogHandler> handlers = new LinkedHashMap<>();
    private final Map<String, LogFilter> filters = new LinkedHashMap<>();

    /**
     * Retrieves an unmodifiable list of all registered log entry handlers.
     *
     * @return a list containing all {@link LogHandler} instances currently registered
     */
    public List<LogHandler> getHandlers() {
        return List.copyOf(handlers.values());
    }

    /**
     * Parses the given {@link Properties} object and creates a {@code LoggingConfiguration} instance.
     *
     * @param properties the {@link Properties} object containing the configuration settings for logging
     * @return a new {@code LoggingConfiguration} instance with settings applied from the provided properties
     */
    public static LoggingConfiguration parse(Properties properties) {
        LoggingConfiguration cfg = new LoggingConfiguration();
        cfg.configure(properties);
        return cfg;
    }

    /**
     * Parses the given {@link Properties} object into this {@code LoggingConfiguration}.
     *
     * @param properties the {@link Properties} object containing configuration settings
     */
    private void configure(Properties properties){
        handleProperty(properties, LOGGING_ROOT_LEVEL, LogLevel::valueOf, rootLevel::set, () -> LogLevel.INFO);
        handleProperty(properties, LOGGING_FILTERS, s -> parseList(s, name -> name), (List<String> list) -> list.forEach(name -> addFilter(properties, name)), List::of);
        handleProperty(properties, LOGGING_HANDLERS, s -> parseList(s, name -> name), (List<String> list) -> list.forEach(name -> addHandler(properties, name)), List::of);
    }

    /**
     * Processes a property from a {@link Properties} object using the provided converter, action,
     * and default value supplier.
     *
     * @param <T>          the type of the property value after conversion
     * @param properties   the {@link Properties} object containing the property to be processed
     * @param key          the key of the property to be processed
     * @param convert      a {@link Function} to convert the property value from {@link String} to the target type
     * @param action       a {@link Consumer} to process the converted value
     * @param defaultValue a {@link Supplier} to provide a default value if the property is not found or is null
     * @throws IllegalStateException if the property value is invalid or the conversion fails
     */
    private static <T> void handleProperty(Properties properties, String key, Function<String, T> convert, Consumer<T> action, Supplier<T> defaultValue) {
        String s = properties.getProperty(key);
        try {
            T value = s != null ? convert.apply(s.strip()) : defaultValue.get();
            action.accept(value);
        } catch (Exception e) {
            throw new IllegalStateException("invalid value for property " + key + ": '" + s + "'", e);
        }
    }

    /**
     * Parses a comma-separated string into a list of elements by applying a conversion
     * function to each trimmed substring.
     *
     * @param <T>     the type of elements in the resulting list
     * @param s       the comma-separated string to be parsed
     * @param convert a function that converts each trimmed substring into an element of type T
     * @return a list containing elements of type T parsed and converted from the input string
     */
    private static <T> List<T> parseList(String s, Function<String, T> convert) {
        return Arrays.stream(s.split(",")).map(String::strip).map(convert).toList();
    }

    /**
     * Adds a new log entry handler to the current logging configuration based on the given properties and handler name.
     *
     * @param properties the {@link Properties} object containing configuration for the logging handler
     * @param name the name of the handler to be added, used as a key to extract specific handler configurations
     * @throws IllegalArgumentException if an invalid handler type or configuration value is provided
     */
    private void addHandler(Properties properties, String name) {
        String prefix = LOGGING_HANDLER + "." + name + ".";

        String sType = properties.getProperty(prefix + LOGGING_TYPE, "").strip();
        LogHandler handler = switch (sType) {
            case "console" -> {
                PrintStream stream = switch (properties.getProperty(prefix + LOGGER_CONSOLE_STREAM, SYSTEM_OUT).strip()) {
                    case SYSTEM_OUT -> System.out;
                    case SYSTEM_ERR -> System.err;
                    default ->
                            throw new IllegalArgumentException("handler '" + name + "' - invalid value for '" + LOGGER_CONSOLE_STREAM + "': '" + sType + "'");
                };

                String sColored = properties.getProperty(prefix + LOGGER_CONSOLE_COLORED, "false");
                boolean colored = switch (sColored) {
                    case COLOR_ENABLED -> true;
                    case COLOR_DISABLED -> false;
                    case COLOR_AUTO -> System.console() != null && System.getenv().get("TERM") != null;
                    default ->
                            throw new IllegalArgumentException("handler '" + name + "' - invalid value for '" + prefix + LOGGER_CONSOLE_COLORED + "': '" + sColored + "'");
                };

                yield new ConsoleHandler(name, stream, colored);
            }
            default -> throw new IllegalArgumentException("unknown handler type for handler '" + name + "' : " + sType);
        };

        handleProperty(properties, LOGGING_HANDLER + "filter", filters::get, handler::setFilter, LogFilter::allPass);

        handlers.put(name, handler);
    }

    private void addFilter(Properties properties, String name) {
        String prefix = LOGGING_FILTER + "." + name + ".";

        StandardLogFilter filter = new StandardLogFilter(name);

        // set the global filter level
        handleProperty(properties, prefix + LEVEL, LogLevel::valueOf, filter::setLevel, () -> LogLevel.INFO);
        // set the level of the root node
        handleProperty(properties, prefix + LEVEL + "rule", LogLevel::valueOf, lvl -> filter.setLevel("", lvl), () -> LogLevel.INFO);

        // set all other levels
        String filterPrefix = prefix + "rule.";
        properties.forEach((r, v) -> {
            String key = String.valueOf(r).strip();
            if (!key.startsWith(filterPrefix)) {
                return;
            }

            String rule = key.substring(filterPrefix.length()).strip();
            LogLevel level = LogLevel.valueOf(String.valueOf(v).strip());

            filter.setLevel(rule, level);
        });

        filters.put(name, filter);
    }

    public void addToProperties(Properties properties) {
        properties.setProperty(LOGGING_ROOT_LEVEL, rootLevel.get().name());
        properties.setProperty(LOGGING_FILTERS, String.join(",", filters.keySet()));
        properties.setProperty(LOGGING_HANDLERS, String.join(",", handlers.keySet()));

        // add filter configurations
        for (Map.Entry<String, LogFilter> entry : filters.entrySet()) {
            String name = entry.getKey();
            LogFilter filter = entry.getValue();
            String prefix = LOGGING_FILTER + "." + name + ".";

            if (filter instanceof StandardLogFilter logFilter) {
                // set global filter level
                properties.setProperty(prefix + LEVEL, logFilter.getLevel().name());
                // set level of the root node
                properties.setProperty(prefix + "rule", logFilter.getLevel("").name());

                // recursively traverse logFilter.getRoot() and add properties
                logFilter.getRules().forEach((loggerName, logLevel) -> {
                    String rule = loggerName.isEmpty() ? "rule" : "rule." + loggerName;
                    properties.setProperty(prefix + rule, logLevel.name());
                });
            }
        }

        // add handler configurations
        for (Map.Entry<String, LogHandler> entry : handlers.entrySet()) {
            String name = entry.getKey();
            LogHandler handler = entry.getValue();
            String prefix = LOGGING_HANDLER + "." + name + ".";

            if (handler instanceof ConsoleHandler consoleHandler) {
                properties.setProperty(prefix + LOGGING_TYPE, "console");
                PrintStream stream = consoleHandler.getPrintStream();
                String sStream = stream == System.err ? SYSTEM_ERR : SYSTEM_OUT;
                properties.setProperty(prefix + LOGGER_CONSOLE_STREAM, sStream);
                properties.setProperty(prefix + LOGGER_CONSOLE_COLORED, String.valueOf(consoleHandler.isColored()));
            }
        }
    }

    @Override
    public String toString() {
        Properties p = new Properties();
        addToProperties(p);
        return p.toString();
    }
}
