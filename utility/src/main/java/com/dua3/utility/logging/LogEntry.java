package com.dua3.utility.logging;

import java.time.LocalDateTime;

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

    /**
     * Get the date and time of this entry.
     * @return the date and time of this entry
     */
    LocalDateTime time();

    /**
     * Get specified field of this entry.
     * 
     * @param f the field
     * @return the field's value
     */
    Object get(Field f);
    
}
