package com.dua3.utility.logging;

import java.util.Collection;

public interface LogEntryDispatcher {
    void addLogEntryHandler(LogEntryHandler handler);

    void removeLogEntryHandler(LogEntryHandler handler);
}
