package com.dua3.utility.logging;

import com.dua3.cabe.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.event.Level;
import org.slf4j.helpers.MessageFormatter;

import java.io.Serializable;
import java.time.Instant;

public record LogEntry(Logger logger, Instant time, Level level, @Nullable Marker marker, String msg, @Nullable Object[] arguments, @Nullable Throwable throwable) implements Serializable {
    public String formatMessage() {
        return MessageFormatter.basicArrayFormat(msg, arguments);
    }
}
