package com.dua3.utility.logging.slf4j;

import com.dua3.cabe.annotations.Nullable;
import com.dua3.utility.logging.LogEntry;
import com.dua3.utility.logging.LogEntryHandler;
import com.dua3.utility.logging.LogLevel;
import org.slf4j.Marker;
import org.slf4j.event.Level;
import org.slf4j.helpers.AbstractLogger;
import org.slf4j.helpers.MessageFormatter;
import org.slf4j.spi.LocationAwareLogger;

import java.lang.ref.WeakReference;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class represents a logger implementation using the SLF4J logging framework. It extends the AbstractLogger class.
 *
 * It is used to forward SLF4J log messages to applications.
 */
public class LoggerSlf4j extends AbstractLogger {
    private static Level defaultLevel = Level.INFO;

    private final List<WeakReference<LogEntryHandler>> handlers;
    private final Map<Marker, Level> markerLevelMap = new HashMap<>();
    private Level level;

    public LoggerSlf4j(String name, List<WeakReference<LogEntryHandler>> handlers) {
        //noinspection AssignmentToSuperclassField: it is the only way to set the logger name
        super.name = name;
        this.handlers = handlers;
    }

    public static Level getDefaultLevel() {
        return defaultLevel;
    }

    public static void setDefaultLevel(Level level) {
        defaultLevel = level;
    }

    @Override
    protected String getFullyQualifiedCallerName() {
        return null;
    }

    @Override
    protected void handleNormalizedLoggingCall(Level level, @Nullable Marker marker, String messagePattern, @Nullable Object[] arguments, @Nullable Throwable throwable) {
        String markerName = marker != null ? marker.getName() : null;
        boolean cleanup = false;
        for (WeakReference<LogEntryHandler> ref: handlers) {
            LogEntryHandler handler = ref.get();
            if (handler==null) {
                cleanup = true;
            } else {
                handler.handleEntry(new LogEntry(name, Instant.now(), translate(level), markerName, () -> MessageFormatter.basicArrayFormat(messagePattern, arguments), throwable));
            }
        }
        if (cleanup) {
            handlers.removeIf(ref -> ref.get()==null);
        }
    }

    /**
     * Translates a given SLF4J Level object to an equivalent LogLevel object.
     *
     * @param level the SLF4J Level object to be translated
     * @return the translated LogLevel object
     */
    private static LogLevel translate(Level level) {
        int levelInt = level.toInt();
        if (levelInt < LocationAwareLogger.DEBUG_INT) {
            return LogLevel.TRACE;
        }
        if (levelInt < LocationAwareLogger.INFO_INT) {
            return LogLevel.DEBUG;
        }
        if (levelInt < LocationAwareLogger.WARN_INT) {
            return LogLevel.INFO;
        }
        if (levelInt < LocationAwareLogger.ERROR_INT) {
            return LogLevel.WARN;
        }
        return LogLevel.ERROR;
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

    public Level getLevel() {
        return level != null ? level : defaultLevel;
    }

    public void setLevel(Level level) {
        this.level = level;
    }
}
