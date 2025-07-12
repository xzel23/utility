package com.dua3.utility.logging.log4j;

import com.dua3.utility.logging.LogEntry;
import com.dua3.utility.logging.LogLevel;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.ReusableMessage;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Represents a log entry with information about the log message, time, level, logger, and optional marker and throwable.
 */
public final class LogEntryLog4J implements LogEntry {
    private final String loggerName;
    private final Instant time;
    private final LogLevel level;
    private final String marker;
    private @Nullable Supplier<String> messageFormatter;
    private @Nullable String formattedMessage;
    private final @Nullable Throwable throwable;
    private @Nullable StackTraceElement source;
    private @Nullable String location;

    /**
     * Creates a new LogEntry object.
     *
     * @param event the log event.
     */
    public LogEntryLog4J(LogEvent event) {
        this.loggerName = event.getLoggerName();
        var instant = event.getInstant();
        this.time = Instant.ofEpochSecond(instant.getEpochSecond(), instant.getNanoOfSecond());
        this.level = LogUtilLog4J.translate(event.getLevel());
        var m = event.getMarker();
        this.marker = m == null ? "" : m.getName();
        this.source = event.getSource();
        Message message = event.getMessage();
        if (message instanceof ReusableMessage rm) {
            // for reusable messages, the message must be formatted instantly
            StringBuilder sbMsg = new StringBuilder(80);
            rm.formatTo(sbMsg);
            this.messageFormatter = null;
            this.formattedMessage = sbMsg.toString();
        } else {
            this.messageFormatter = message::getFormattedMessage;
            this.formattedMessage = null;
        }
        this.throwable = event.getThrown();
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
    public @Nullable String location() {
        if (source != null) {
            location = source.getFileName() + ":" + source.getLineNumber() + " [" + source.getMethodName() + "]";
            source = null;
        }
        return location;
    }

    @Override
    public @Nullable Throwable throwable() {
        return throwable;
    }

}
