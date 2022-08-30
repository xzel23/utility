package com.dua3.utility.logging;

import org.slf4j.ILoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Properties;

public class LoggerFactory implements ILoggerFactory {
    public static final String LOGGER_CONSOLE = "logger.console";
    public static final String LOGGER_CONSOLE_COLORED = "logger.console.colored";
    public static final String LOGGER_BUFFER = "logger.buffer";
    
    private final LogBuffer logBuffer;
    private final List<LogEntryHandler> handlers = new ArrayList<>();
    
    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    private static Properties getProperties() {
        Properties properties = new Properties();
        try (InputStream in = ClassLoader.getSystemResourceAsStream("logging.properties")) {
            if (in==null) {
                properties.put(LOGGER_CONSOLE, "system.out");
            } else {
                properties.load(in);
            }
        } catch (IOException e) {
            properties.put(LOGGER_CONSOLE, "system.out");
            e.printStackTrace(System.err);
        }
        return properties;
    }

    public LoggerFactory() {
        Properties properties = getProperties();
        
        String propertyConsole = properties.getProperty(LOGGER_CONSOLE, "").trim().toLowerCase(Locale.ROOT);
        final PrintStream stream = switch (propertyConsole) {
            case "" -> null;
            case "system.err" -> System.err;
            case "system.out" -> System.out;
            default -> throw new IllegalArgumentException("invalid value for property "+LOGGER_CONSOLE+": '"+propertyConsole+"'");
        };
        String propertyColored = properties.getProperty(LOGGER_CONSOLE_COLORED, "true").trim().toLowerCase(Locale.ROOT);
        final boolean colored = switch (propertyColored) {
            case "true" -> true;
            case "false" -> false;
            default -> throw new IllegalArgumentException("invalid value for property "+LOGGER_CONSOLE_COLORED+": '"+propertyColored+"'");
        };
        if (stream!=null) {
            handlers.add(new ConsoleHandler(stream, colored));
        }
        
        String propertyBuffer = properties.getProperty(LOGGER_BUFFER, "0").trim();
        int bufferSize = Integer.parseInt(propertyBuffer);
        if (bufferSize > 0) {
            logBuffer = new LogBuffer(bufferSize);
            handlers.add(logBuffer);
        } else {
            logBuffer = null;
        }
    }
    
    @Override
    public org.slf4j.Logger getLogger(String name) {
        return new Logger(name, handlers);
    }

    public Optional<LogBuffer> getLogBuffer() {
        return Optional.ofNullable(logBuffer);
    }
}
