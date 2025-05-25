package com.dua3.utility.logging;

import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * The LogEntry interface represents a log entry with various properties such as message, logger name, time, level, marker, and throwable.
 * <p>
 * Note: Implementing classes should be immutable!
 */
@SuppressWarnings("MagicCharacter")
public interface LogEntry {
    /**
     * Retrieves the message of the log entry.
     *
     * @return The message of the log entry.
     */
    String message();

    /**
     * Returns the name of the logger associated with the log entry.
     *
     * @return The name of the logger.
     */
    String loggerName();

    /**
     * Returns the time when the logging event was created as an Instant object.
     *
     * @return the creation time of the logging event as an Instant object
     */
    Instant time();

    /**
     * Returns the log level of the LogEntry.
     *
     * @return the log level of the LogEntry
     */
    LogLevel level();

    /**
     * Returns the marker associated with this log entry.
     *
     * @return the marker
     */
    String marker();

    /**
     * Returns the throwable object associated with this LogEntry.
     *
     * @return the throwable object associated with this LogEntry, or null if no throwable is present
     */
    @Nullable
    Throwable throwable();

    /**
     * Returns the location information if present.
     *
     * @return the location information, or null if no location is present
     */
    @Nullable
    String location();

    /**
     * Formats the log entry with the given prefix and suffix.
     *
     * @param prefix the prefix to prepend to the formatted entry
     * @param suffix the suffix to append to the formatted entry
     * @return the formatted log entry as a string
     */
    default String format(String prefix, String suffix) {
        StringBuilder sb = new StringBuilder(100);
        sb.append(prefix);
        sb.append('[').append(level()).append(']');
        sb.append(' ');
        sb.append(DateTimeFormatter.ISO_INSTANT.format(time()));
        sb.append(' ');
        sb.append(loggerName());
        sb.append('\n');
        if (location() != null) {
            sb.append(location());
            sb.append('\n');
        }
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
