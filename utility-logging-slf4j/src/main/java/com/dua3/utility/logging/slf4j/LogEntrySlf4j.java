package com.dua3.utility.logging.slf4j;

import org.jspecify.annotations.Nullable;
import com.dua3.utility.logging.LogEntry;
import com.dua3.utility.logging.LogLevel;
import org.slf4j.Marker;
import org.slf4j.event.Level;
import org.slf4j.spi.LocationAwareLogger;

import java.time.Instant;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Represents a log entry with information about the log message, time, level, logger, and optional marker and throwable.
 */
public final class LogEntrySlf4j implements LogEntry {
    private final String loggerName;
    private final Instant time;
    private final LogLevel level;
    private final String marker;
    private @Nullable Supplier<String> messageFormatter;
    private @Nullable String formattedMessage;
    private final @Nullable Throwable throwable;

    /**
     * Creates a new instance of LogEntrySlf4j.
     *
     * @param loggerName        the name of the logger
     * @param level             the log level of the log entry
     * @param marker            the marker associated with the log entry (nullable)
     * @param messageFormatter  the supplier used to format the log message
     * @param throwable         the throwable associated with the log entry (nullable)
     */
    public LogEntrySlf4j(String loggerName, Level level, @Nullable Marker marker, Supplier<String> messageFormatter,
                         @Nullable Throwable throwable) {
        this.loggerName = loggerName;
        this.time = Instant.now();
        this.level = translate(level);
        this.marker = marker == null ? "" : marker.getName();
        this.messageFormatter = messageFormatter;
        this.throwable = throwable;
    }

    @Override
    public String message() {
        if (messageFormatter != null) {
            formattedMessage = messageFormatter.get();
            messageFormatter = null;
        }
        return Objects.requireNonNullElse(formattedMessage, "");
    }

    @Override
    public String toString() {
        return format("", "");
    }

    @Override
    public String loggerName() {
        return loggerName;
    }

    @Override
    public Instant time() {
        return time;
    }

    @Override
    public LogLevel level() {
        return level;
    }

    @Override
    public String marker() {
        return marker;
    }

    @Override
    public @Nullable Throwable throwable() {
        return throwable;
    }

    @Override
    public @Nullable String location() {
        return null;
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

}
