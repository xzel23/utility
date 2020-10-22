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
            case CAUSE:
                return cause();
            default:
                throw new IllegalArgumentException("no such field: "+f);
        }
    }

    @Override
    public String toString() {
        Formatter fmt = new Formatter();
        fmt.format("%s %s %s%n%s%n", time(), level(), logger(), message());
        cause().ifPresent(t -> printCause(fmt, t));
        return fmt.toString();
    }

    private void printCause(Formatter fmt, IThrowable t) {
        fmt.format("%s", t);
        for (IThrowable.IStackTraceElement ste: t.getStackTrace()) {
            fmt.format("%nat %s", ste.toString());
        }
        IThrowable cause = t.getCause();
        if (cause!=null) {
            fmt.format("%ncaused by ", t);
            printCause(fmt, cause);
        }
    }

}
