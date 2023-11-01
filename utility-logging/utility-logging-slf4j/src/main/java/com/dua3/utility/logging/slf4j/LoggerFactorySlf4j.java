package com.dua3.utility.logging.slf4j;

import com.dua3.utility.data.Pair;
import com.dua3.utility.lang.LangUtil;
import com.dua3.utility.logging.LogEntryHandler;
import com.dua3.utility.logging.ConsoleHandler;
import com.dua3.utility.logging.LogEntryDispatcher;
import org.slf4j.ILoggerFactory;
import org.slf4j.event.Level;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Properties;

/**
 * The LoggerFactorySlf4j class is an implementation of the ILoggerFactory and LogEntryDispatcher interfaces.
 */
public class LoggerFactorySlf4j implements ILoggerFactory, LogEntryDispatcher {
    public static final String LEVEL = "logger.level";

    public static final String LOGGER_CONSOLE_STREAM = "logger.console.stream";
    public static final String LOGGER_CONSOLE_COLORED = "logger.console.colored";

    private final List<Pair<String, Level>> prefixes = new ArrayList<>();
    private final List<WeakReference<LogEntryHandler>> handlers = new ArrayList<>();

    public LoggerFactorySlf4j() {
        Properties properties = getProperties();

        // parse log level entry
        String leveldeclaration = properties.getProperty(LEVEL, Level.INFO.name());
        String[] decls = leveldeclaration.split(",");

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
        if (stream != null) {
            handlers.add(new WeakReference<>(new ConsoleHandler(stream, colored)));
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
        handlers.removeIf(h -> h.get()==handler);
    }
}
