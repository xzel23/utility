package com.dua3.utility.logging;

import java.time.LocalDateTime;
import java.util.Locale;

public interface LogEntry {
    enum Field {
        CATEGORY,
        LEVEL,
        TIME,
        TEXT,
        STACK_TRACE
    };
    
    Category category();

    String level();

    LocalDateTime time();

    String text();

    String[] stacktrace();
    
    default Object get(Field f) {
        switch (f) {
            case CATEGORY:
                return category();
            case LEVEL:
                return level();
            case TIME:
                return time();
            case TEXT:
                return text();
            case STACK_TRACE:
                return stacktrace();
            default:
                throw new IllegalArgumentException("no such field: "+f);
        }
    }
}
