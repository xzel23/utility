package com.dua3.utility.logging;

import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.event.Level;
import org.slf4j.helpers.MessageFormatter;

import java.io.Serializable;
import java.time.Instant;

public record LogEntry(Logger logger, Instant time, Level level, Marker marker, String msg, Object[] arguments, Throwable throwable) implements Serializable {
    public String formatMessage() {
        return MessageFormatter.basicArrayFormat(msg, arguments);
    }
}
