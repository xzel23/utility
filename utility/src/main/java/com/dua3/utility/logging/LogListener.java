package com.dua3.utility.logging;

@FunctionalInterface
public interface LogListener {

    void entry(LogEntry entry);

}
