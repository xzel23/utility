package com.dua3.utility.logging;

import java.time.LocalDateTime;

public interface LogEntry {
    Category category();

    String level();

    LocalDateTime time();

    String text();

    String[] stacktrace();
}
