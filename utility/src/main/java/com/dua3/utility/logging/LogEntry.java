package com.dua3.utility.logging;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

/**
 * A wrapper interface for log messages of different frameworks.
 */
public interface LogEntry {
    /**
     * The fields provided by this interface.
     */
    enum Field {
        CATEGORY,
        MILLIS,
        TIME,
        LOGGER,
        LEVEL,
        MESSAGE,
        CAUSE
    }

    /**
     * Get value of field by enum.
     * @param f the field
     * @return value of field
     */
    default Object get(Field f) {
        switch (f) {
            case CATEGORY:
                return category();
            case LEVEL:
                return level();
            case LOGGER:
                return logger();
            case MILLIS:
                return millis();
            case TIME:
                return time();
            case MESSAGE:
                return message();
            case CAUSE:
                return cause();
            default:
                throw new IllegalArgumentException("no such field: " + f);
        }
    }
    
    /**
     * Get the category of this entry. 
     * @return the category
     */
    Category category();

    /**
     * Get the millis of this entry.
     * @return the millis
     */
    long millis();

    /**
     * Get the logger name of this entry.
     * @return the logger name as reported by the used logging framework
     */
    String logger();

    /**
     * Get the level of this entry.
     * @return the logger level as reported by the used logging framework
     */
    String level();

    /**
     * Get the message of this entry.
     * @return the log message
     */
    String message();

    /**
     * Get cause.
     * @return the cause
     */
    Optional<IThrowable> cause();

    /**
     * Get the date and time of this log entry.
     * @return date and time of the entry
     */
    default LocalDateTime time() {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(millis()), ZoneId.systemDefault());
    }

    /**
     * Format LogEntry to String.
     * @param entry the log entry
     * @return the string
     */
    static String format(LogEntry entry) {
        try {
            StringBuilder sb = new StringBuilder(80);

            sb.append(entry.time())
                    .append(" ")
                    .append(entry.level())
                    .append(" ")
                    .append(entry.logger())
                    .append("\n")
                    .append(entry.message());

            Optional<IThrowable> cause = entry.cause();
            if (cause.isPresent()) {
                sb.append("\ncaused by ");
                cause.get().appendTo(sb);
            }

            return sb.toString();
        } catch (IOException e) {
            return "";
        }
    }

}
