package com.dua3.utility.logging;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

public interface LogEntry {
    String message();

    String loggerName();

    Instant time();

    LogLevel level();

    String marker();

    Throwable throwable();

    default String format(String prefix, String suffix) {
        StringBuilder sb = new StringBuilder(100);
        sb.append(prefix);
        sb.append('[').append(level()).append(']');
        sb.append(' ');
        sb.append(DateTimeFormatter.ISO_INSTANT.format(time()));
        sb.append(' ');
        sb.append(loggerName());
        sb.append(' ');
        sb.append(message());
        if (throwable() != null) {
            sb.append(System.lineSeparator());
            appendThrowable(sb);
        }
        sb.append(suffix);
        return sb.toString();
    }

    /**
     * Appends the throwable object to the supplied StringBuilder instance by printing its stack trace.
     * @param sb the StringBuilder to append to
     */
    private void appendThrowable(StringBuilder sb) {
        Throwable t = throwable();
        if (t == null) {
            sb.append("null");
        } else {
            try (StringWriter sw = new StringWriter(200); PrintWriter pw = new PrintWriter(sw)) {
                t.printStackTrace(pw);
                sb.append(sw);
            } catch (IOException e) {
                sb.append(t);
            }
        }
    }
}
