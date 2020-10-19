package com.dua3.utility.logging;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public interface LogEntry {
    enum Field {
        CATEGORY,
        MILLIS,
        TIME,
        LOGGER,
        LEVEL,
        MESSAGE,
        STACK_TRACE
    };

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
     * Get the date and time of this entry.
     * @return the date and time of this entry
     */
    default LocalDateTime time() {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(millis()), ZoneId.systemDefault());
    }
    
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
     * Get the stack trace.
     * @return the stack trace
     */
    StackTraceElement[] stacktrace();
    
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
            case STACK_TRACE:
                return stacktrace();
            default:
                throw new IllegalArgumentException("no such field: "+f);
        }
    }
}
