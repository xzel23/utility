package com.dua3.utility.logging.slf4j;

import org.jspecify.annotations.Nullable;
import com.dua3.utility.logging.LogEntryHandler;
import org.slf4j.Marker;
import org.slf4j.event.Level;
import org.slf4j.helpers.AbstractLogger;
import org.slf4j.helpers.MessageFormatter;

import java.io.Serial;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class represents a logger implementation using the SLF4J logging framework. It extends the AbstractLogger class.
 * <p>
 * It is used to forward SLF4J log messages to applications.
 */
public class LoggerSlf4j extends AbstractLogger {
    private static Level defaultLevel = Level.INFO;

    private final List<? extends WeakReference<LogEntryHandler>> handlers;
    private final Map<Marker, Level> markerLevelMap = new HashMap<>();
    private @Nullable Level level;

    /**
     * Constructs a new LoggerSlf4j instance with the specified name and handlers.
     *
     * @param name     the name of the logger
     * @param handlers a list of handlers for processing log entries
     */
    public LoggerSlf4j(String name, List<? extends WeakReference<LogEntryHandler>> handlers) {
        //noinspection AssignmentToSuperclassField: it is the only way to set the logger name
        super.name = name;
        this.handlers = handlers;
    }

    /**
     * Returns the default log level for the logger.
     *
     * @return the default log level
     */
    public static Level getDefaultLevel() {
        return defaultLevel;
    }

    /**
     * Sets the default logging level.
     *
     * @param level the new default logging level
     */
    public static void setDefaultLevel(Level level) {
        defaultLevel = level;
    }

    @Override
    protected @Nullable String getFullyQualifiedCallerName() {
        return null;
    }

    @Override
    protected void handleNormalizedLoggingCall(Level level, @Nullable Marker marker, String messagePattern, @Nullable Object[] arguments, @Nullable Throwable throwable) {
        boolean cleanup = false;
        for (WeakReference<LogEntryHandler> ref : handlers) {
            LogEntryHandler handler = ref.get();
            if (handler == null) {
                cleanup = true;
            } else {
                handler.handleEntry(new LogEntrySlf4j(name, level, marker, () -> MessageFormatter.basicArrayFormat(messagePattern, arguments), throwable));
            }
        }
        if (cleanup) {
            handlers.removeIf(ref -> ref.get() == null);
        }
    }

    @Override
    public boolean isTraceEnabled() {
        return getLevel().toInt() <= Level.TRACE.toInt();
    }

    @Override
    public boolean isTraceEnabled(@Nullable Marker marker) {
        return markerLevelMap.getOrDefault(marker, getLevel()).toInt() <= Level.TRACE.toInt();
    }

    @Override
    public boolean isDebugEnabled() {
        return getLevel().toInt() <= Level.DEBUG.toInt();
    }

    @Override
    public boolean isDebugEnabled(@Nullable Marker marker) {
        return markerLevelMap.getOrDefault(marker, getLevel()).toInt() <= Level.DEBUG.toInt();
    }

    @Override
    public boolean isInfoEnabled() {
        return getLevel().toInt() <= Level.INFO.toInt();
    }

    @Override
    public boolean isInfoEnabled(@Nullable Marker marker) {
        return markerLevelMap.getOrDefault(marker, getLevel()).toInt() <= Level.INFO.toInt();
    }

    @Override
    public boolean isWarnEnabled() {
        return getLevel().toInt() <= Level.WARN.toInt();
    }

    @Override
    public boolean isWarnEnabled(@Nullable Marker marker) {
        return markerLevelMap.getOrDefault(marker, getLevel()).toInt() <= Level.WARN.toInt();
    }

    @Override
    public boolean isErrorEnabled() {
        return getLevel().toInt() <= Level.ERROR.toInt();
    }

    @Override
    public boolean isErrorEnabled(@Nullable Marker marker) {
        return markerLevelMap.getOrDefault(marker, getLevel()).toInt() <= Level.ERROR.toInt();
    }

    /**
     * Retrieves the current logging level. If not explicitly set, returns the default level.
     *
     * @return the current logging level if set, otherwise the default logging level
     */
    public Level getLevel() {
        return level != null ? level : defaultLevel;
    }

    /**
     * Sets the logging level for this logger.
     *
     * @param level the new logging level to be set
     */
    public void setLevel(Level level) {
        this.level = level;
    }

    @Serial
    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {throw new java.io.NotSerializableException("com.dua3.utility.logging.slf4j.LoggerSlf4j");}

    @Serial
    private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {throw new java.io.NotSerializableException("com.dua3.utility.logging.slf4j.LoggerSlf4j");}
}
