package com.dua3.utility.logging;

import org.slf4j.ILoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Properties;

public class LoggerFactory implements ILoggerFactory {
    public static final String LOGGER_CONSOLE = "logger.console";
    public static final String LOGGER_BUFFER = "logger.buffer";
    
    private final LogBuffer logBuffer;
    private List<LogEntryHandler> handlers = new ArrayList<>();
    
    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    private static Properties getProperties() {
        Properties properties = new Properties();
        try (InputStream in = getResourceAsStream("logging.properties")) {
            if (in==null) {
                properties.put(LOGGER_CONSOLE, "system.err");
            } else {
                properties.load(in);
            }
        } catch (IOException e) {
            properties.put(LOGGER_CONSOLE, "system.err");
            e.printStackTrace(System.err);
        }
        return properties;
    }

    private static InputStream getResourceAsStream(String name) throws IOException {
        InputStream in = ClassLoader.getSystemClassLoader().getResourceAsStream(name);
        if (in == null) {
            in = Files.newInputStream(Paths.get(".", name));
        }
        return in;
    }

    public LoggerFactory() {
        Properties properties = getProperties();
        
        String propertyConsole = properties.getProperty(LOGGER_CONSOLE, "").trim().toLowerCase(Locale.ROOT);
        switch (propertyConsole) {
            case "" -> {}
            case "system.err" -> handlers.add(new ConsoleHandler(System.err));
            case "system.out" -> handlers.add(new ConsoleHandler(System.out));
            default -> throw new IllegalArgumentException("invalid value for property "+LOGGER_CONSOLE+": '"+propertyConsole+"'");
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
