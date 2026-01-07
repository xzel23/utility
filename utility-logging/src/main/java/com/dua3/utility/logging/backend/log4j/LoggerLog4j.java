package com.dua3.utility.logging.backend.log4j;

import com.dua3.utility.logging.backend.universal.UniversalDispatcher;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.spi.AbstractLogger;
import org.jspecify.annotations.Nullable;

public class LoggerLog4j extends AbstractLogger {
    private static final UniversalDispatcher DISPATCHER = UniversalDispatcher.getInstance();

    public LoggerLog4j(String name) {
        super(name);
    }

    @Override
    public boolean isEnabled(Level level, @Nullable Marker marker, Message message, @Nullable Throwable t) {
        return DISPATCHER.isEnabledLog4j(name, level, marker);
    }

    @Override
    public boolean isEnabled(Level level, @Nullable Marker marker, CharSequence message, @Nullable Throwable t) {
        return DISPATCHER.isEnabledLog4j(name, level, marker);
    }

    @Override
    public boolean isEnabled(Level level, @Nullable Marker marker, @Nullable Object message, @Nullable Throwable t) {
        return DISPATCHER.isEnabledLog4j(name, level, marker);
    }

    @Override
    public boolean isEnabled(Level level, @Nullable Marker marker, String message, @Nullable Throwable t) {
        return DISPATCHER.isEnabledLog4j(name, level, marker);
    }

    @Override
    public boolean isEnabled(Level level, @Nullable Marker marker, String message) {
        return DISPATCHER.isEnabledLog4j(name, level, marker);
    }

    @Override
    public boolean isEnabled(Level level, @Nullable Marker marker, String message, @Nullable Object @Nullable ... params) {
        return DISPATCHER.isEnabledLog4j(name, level, marker);
    }

    @Override
    public boolean isEnabled(Level level, @Nullable Marker marker, String message, @Nullable Object p0) {
        return DISPATCHER.isEnabledLog4j(name, level, marker);
    }

    @Override
    public boolean isEnabled(Level level, @Nullable Marker marker, String message, @Nullable Object p0, @Nullable Object p1) {
        return DISPATCHER.isEnabledLog4j(name, level, marker);
    }

    @Override
    public boolean isEnabled(Level level, @Nullable Marker marker, String message, @Nullable Object p0, @Nullable Object p1, @Nullable Object p2) {
        return DISPATCHER.isEnabledLog4j(name, level, marker);
    }

    @Override
    public boolean isEnabled(Level level, @Nullable Marker marker, String message, @Nullable Object p0, @Nullable Object p1, @Nullable Object p2, @Nullable Object p3) {
        return DISPATCHER.isEnabledLog4j(name, level, marker);
    }

    @Override
    public boolean isEnabled(Level level, @Nullable Marker marker, String message, @Nullable Object p0, @Nullable Object p1, @Nullable Object p2, @Nullable Object p3, @Nullable Object p4) {
        return DISPATCHER.isEnabledLog4j(name, level, marker);
    }

    @Override
    public boolean isEnabled(Level level, @Nullable Marker marker, String message, @Nullable Object p0, @Nullable Object p1, @Nullable Object p2, @Nullable Object p3, @Nullable Object p4, @Nullable Object p5) {
        return DISPATCHER.isEnabledLog4j(name, level, marker);
    }

    @Override
    public boolean isEnabled(Level level, @Nullable Marker marker, String message, @Nullable Object p0, @Nullable Object p1, @Nullable Object p2, @Nullable Object p3, @Nullable Object p4, @Nullable Object p5, @Nullable Object p6) {
        return false;
    }

    @Override
    public boolean isEnabled(Level level, @Nullable Marker marker, String message, @Nullable Object p0, @Nullable Object p1, @Nullable Object p2, @Nullable Object p3, @Nullable Object p4, @Nullable Object p5, @Nullable Object p6, @Nullable Object p7) {
        return DISPATCHER.isEnabledLog4j(name, level, marker);
    }

    @Override
    public boolean isEnabled(Level level, @Nullable Marker marker, String message, @Nullable Object p0, @Nullable Object p1, @Nullable Object p2, @Nullable Object p3, @Nullable Object p4, @Nullable Object p5, @Nullable Object p6, @Nullable Object p7, @Nullable Object p8) {
        return DISPATCHER.isEnabledLog4j(name, level, marker);
    }

    @Override
    public boolean isEnabled(Level level, @Nullable Marker marker, String message, @Nullable Object p0, @Nullable Object p1, @Nullable Object p2, @Nullable Object p3, @Nullable Object p4, @Nullable Object p5, @Nullable Object p6, @Nullable Object p7, @Nullable Object p8, @Nullable Object p9) {
        return DISPATCHER.isEnabledLog4j(name, level, marker);
    }

    @Override
    public void logMessage(String fqcn, Level level, @Nullable Marker marker, Message message, @Nullable Throwable t) {
        DISPATCHER.dispatchLog4j(name, level, marker, message, t);
    }

    @Override
    public Level getLevel() {
        return Level.ALL;
    }
}