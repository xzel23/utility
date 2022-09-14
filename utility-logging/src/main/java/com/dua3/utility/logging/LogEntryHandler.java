package com.dua3.utility.logging;

@FunctionalInterface
public interface LogEntryHandler {
    void handleEntry(LogEntry entry);
}
