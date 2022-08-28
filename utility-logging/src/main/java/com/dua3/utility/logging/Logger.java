package com.dua3.utility.logging;

import org.slf4j.Marker;
import org.slf4j.event.Level;
import org.slf4j.helpers.AbstractLogger;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Logger extends AbstractLogger {
    private static Level defaultLevel = Level.INFO;

    private List<LogEntryHandler> handlers;
    private final Map<Marker,Level> markerLevelMap = new HashMap<>();
    private Level level = null;
    
    public static void setDefaultLevel(Level level) {
        defaultLevel = level;
    }
    
    public static Level getDefaultLevel() {
        return defaultLevel;
    }
    
    public Logger(String name, List<LogEntryHandler> handlers) {
        super.name = name;
        this.handlers = handlers;
    }
    
    @Override
    protected String getFullyQualifiedCallerName() {
        return null;
    }

    @Override
    public String getName() {
        return super.getName();
    }

    @Override
    protected void handleNormalizedLoggingCall(Level level, Marker marker, String msg, Object[] arguments, Throwable throwable) {
        handlers.forEach(handler -> handler.handleEntry(new LogEntry(this, Instant.now(), level, marker, msg, arguments, throwable)));
    }

    @Override
    public boolean isTraceEnabled() {
        return getLevel().toInt() <= Level.TRACE.toInt();
    }

    @Override
    public boolean isTraceEnabled(Marker marker) {
        return markerLevelMap.getOrDefault(marker, getLevel()).toInt() <= Level.TRACE.toInt();
    }

    @Override
    public boolean isDebugEnabled() {
        return getLevel().toInt() <= Level.DEBUG.toInt();
    }

    @Override
    public boolean isDebugEnabled(Marker marker) {
        return markerLevelMap.getOrDefault(marker, getLevel()).toInt() <= Level.DEBUG.toInt();
    }

    @Override
    public boolean isInfoEnabled() {
        return getLevel().toInt() <= Level.INFO.toInt();
    }

    @Override
    public boolean isInfoEnabled(Marker marker) {
        return markerLevelMap.getOrDefault(marker, getLevel()).toInt() <= Level.INFO.toInt();
    }

    @Override
    public boolean isWarnEnabled() {
        return getLevel().toInt() <= Level.WARN.toInt();
    }

    @Override
    public boolean isWarnEnabled(Marker marker) {
        return markerLevelMap.getOrDefault(marker, getLevel()).toInt() <= Level.WARN.toInt();
    }

    @Override
    public boolean isErrorEnabled() {
        return getLevel().toInt() <= Level.ERROR.toInt();
    }

    @Override
    public boolean isErrorEnabled(Marker marker) {
        return markerLevelMap.getOrDefault(marker, getLevel()).toInt() <= Level.ERROR.toInt();
    }

    public Level getLevel() {
        return level!=null ? level : defaultLevel;
    }

    public void setLevel(Level level) {
        this.level = level;
    }
}
