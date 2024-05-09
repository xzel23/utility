package com.dua3.utility.logging.log4j;

import com.dua3.utility.logging.LogEntry;
import com.dua3.utility.logging.LogLevel;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.ReusableMessage;

import java.time.Instant;
import java.util.function.Supplier;

/**
 * Represents a log entry with information about the log message, time, level, logger, and optional marker and throwable.
 */
public final class LogEntryLog4J implements LogEntry {
    private final String loggerName;
    private final Instant time;
    private final LogLevel level;
    private final String marker;
    private Supplier<String> messageFormatter;
    private String formattedMessage;
    private final Throwable throwable;

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
        var marker = event.getMarker();
        this.marker = marker == null ? "" : marker.getName();
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

    /**
     * Formats the message using MessageFormatter.basicArrayFormat.
     *
     * @return the formatted message as a string.
     */
    @Override
    public String message() {
        if (messageFormatter != null) {
            formattedMessage = messageFormatter.get();
            messageFormatter = null;
        }
        return formattedMessage;
    }

    @Override
    public String toString() {
        return format("", "");
    }

    /**
     * Returns the logger name.
     *
     * @return the name of the logger.
     */
    @Override
    public String loggerName() {
        return loggerName;
    }

    /**
     * Returns the time when the log entry was created.
     *
     * @return the time when the log entry was created.
     */
    @Override
    public Instant time() {
        return time;
    }

    /**
     * Returns the log level of the log entry.
     *
     * @return the log level of the log entry.
     */
    @Override
    public LogLevel level() {
        return level;
    }

    /**
     * Returns the marker of the log entry.
     *
     * @return the marker of the log entry.
     */
    @Override
    public String marker() {
        return marker;
    }

    /**
     * Returns the throwable associated with this log entry.
     *
     * @return the throwable associated with this log entry.
     */
    @Override
    public Throwable throwable() {
        return throwable;
    }

}
