package com.dua3.utility.logging;

import java.util.function.Predicate;

/**
 * An interface that listens for log entries.
 */
@FunctionalInterface
public interface LogListener {

    /**
     * Filter and pass on log entries to listener.
     * @param listener the listener to send entries to
     * @param pass the predicate that determines if entries should be passed on
     * @return a LogListerner instance that passes all entries on that match the predicate
     */
    static LogListener filter(LogListener listener, Predicate<? super LogEntry> pass) {
        return entry -> {
            if (pass.test(entry)) {
                listener.entry(entry);
            }
        };
    }
    
    void entry(LogEntry entry);

}
