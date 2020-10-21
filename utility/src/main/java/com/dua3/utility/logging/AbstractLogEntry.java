package com.dua3.utility.logging;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Formatter;

public abstract class AbstractLogEntry<T extends Object> implements LogEntry {

    public abstract T getNative();

    @Override
    public LocalDateTime time() {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(millis()), ZoneId.systemDefault());
    }

    @Override
    public Object get(Field f) {
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

    @Override
    public String toString() {
        Formatter fmt = new Formatter();
        fmt.format("%s %s %s%n%s", time(), level(), logger(), message());
        return fmt.toString();
    }
    
}
