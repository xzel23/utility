package com.dua3.utility.logging;

import java.util.function.Predicate;

@FunctionalInterface
public interface LogListener {

    static LogListener filter(LogListener listener, Predicate<LogEntry> pass) {
        return entry -> new LogListener() {
            @Override
            public void entry(LogEntry entry) {
                if (pass.test(entry)) {
                    listener.entry(entry);
                }
            }
        };
    }
    
    void entry(LogEntry entry);

}
