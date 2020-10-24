package com.dua3.utility.logging;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

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
                throw new IllegalArgumentException("no such field: " + f);
        }
    }

    @Override
    public String toString() {
        try {
            StringBuilder sb = new StringBuilder(80);
            sb.append(time()).append(" ").append(level()).append(" ").append(logger()).append(" ").append(message());
            Optional<IThrowable> cause = cause();
            if (cause.isPresent()) {
                sb.append("\n");
                cause.get().appendTo(sb);
            }
            return sb.toString();
        } catch (IOException e) {
            return "";
        }
    }

}
