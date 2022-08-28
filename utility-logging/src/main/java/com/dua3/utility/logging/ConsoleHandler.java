package com.dua3.utility.logging;

import java.io.PrintStream;

public class ConsoleHandler implements LogEntryHandler {
    private final PrintStream out;

    public ConsoleHandler(PrintStream out) {
        this.out = out;
    }

    @Override
    public void handleEntry(LogEntry entry) {
        if (entry.throwable()==null) {
            out.format("%s %s%n[%5s] %s%n", entry.time(), entry.logger(), entry.level(), entry.formatMessage());
        } else {
            out.format("%s %s%n[%5s] %s%n%s%n", entry.time(), entry.logger(), entry.level(), entry.formatMessage(), entry.throwable());
        }
    }
}
