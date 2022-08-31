package com.dua3.utility.logging;

import com.dua3.cabe.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.event.Level;
import org.slf4j.helpers.MessageFormatter;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

public record LogEntry(Logger logger, Instant time, Level level, @Nullable Marker marker, String msg, @Nullable Object[] arguments, @Nullable Throwable throwable) implements Serializable {
    public String formatMessage() {
        return MessageFormatter.basicArrayFormat(msg, arguments);
    }

    public String formatThrowable() {
        if (throwable==null) {
            return "";
        }
        
        try (StringWriter sw = new StringWriter(200); PrintWriter pw = new PrintWriter(sw)) {
            throwable.printStackTrace(pw);
            return sw.toString();
        } catch (IOException e) {
            return throwable.toString();
        }
    }
    
    public String toString() {
        if (throwable()==null) {
            return "[%-5s] %s %s\t%s".formatted(level, DateTimeFormatter.ISO_INSTANT.format(time), logger.getName(), formatMessage());
        } else {
            return "[%-5s] %s %s\t%s%n%s".formatted(level, DateTimeFormatter.ISO_INSTANT.format(time), logger.getName(), formatMessage(), formatThrowable());
        }
    }
}
