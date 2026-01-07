package com.dua3.utility.logging.backend.slf4j;

import com.dua3.utility.logging.LogEntryHandler;
import com.dua3.utility.logging.backend.universal.UniversalDispatcher;
import org.jspecify.annotations.Nullable;
import org.slf4j.Marker;
import org.slf4j.event.Level;
import org.slf4j.helpers.AbstractLogger;

import java.io.NotSerializableException;
import java.io.Serial;
import java.lang.ref.WeakReference;
import java.util.List;

/**
 * This class represents a logger implementation using the SLF4J logging framework. It extends the AbstractLogger class.
 * <p>
 * It is used to forward SLF4J log messages to applications.
 * <p>
 * Note that SLF4J markers are not supported by this implementation, i.e., markers in logging calls are ignored and
 * logging is done as though the marker was not present.
 */
public class LoggerSlf4j extends AbstractLogger {
    @Serial
    private static final long serialVersionUID = 1L;

    private static final UniversalDispatcher DISPATCHER = UniversalDispatcher.getInstance();

    private static Level defaultLevel = Level.INFO;

    private final transient List<? extends WeakReference<LogEntryHandler>> handlers;
    private @Nullable Level level;

    /**
     * Constructs a new LoggerSlf4j instance with the specified name and handlers.
     *
     * @param name     the name of the logger
     * @param handlers a list of handlers for processing log entries
     */
    public LoggerSlf4j(String name, List<? extends WeakReference<LogEntryHandler>> handlers) {
        //noinspection AssignmentToSuperclassField - API restriction; it is the only way to set the logger name
        super.name = name;
        this.handlers = handlers;
    }

    /**
     * Returns the default log level for the logger.
     *
     * @return the default log level
     */
    public static Level getDefaultLevel() {
        return defaultLevel;
    }

    /**
     * Sets the default logging level.
     *
     * @param level the new default logging level
     */
    public static void setDefaultLevel(Level level) {
        defaultLevel = level;
    }

    @Override
    protected @Nullable String getFullyQualifiedCallerName() {
        return null;
    }

    @Override
    protected void handleNormalizedLoggingCall(Level level, @Nullable Marker marker, String messagePattern, @Nullable Object @Nullable [] arguments, @Nullable Throwable throwable) {
        if (level.toInt() >= defaultLevel.toInt()) {
            DISPATCHER.dispatchSlf4j(name, level, marker, messagePattern, arguments, throwable);
        }
    }

    /**
     * Determines if the specified logging level is enabled for this logger.
     * The method compares the logger's current level with the provided level, returning true
     * if the current level is less than or equal to the specified level.
     *
     * @param level the logging level to check
     * @return true if the provided logging level is enabled, false otherwise
     */
    public boolean isEnabled(Level level) {
        return getLevel().toInt() <= level.toInt();
    }

    /**
     * Determines whether logging is enabled for the specified level and marker.
     * <p>
     * Note that SLF4J markers are ignored by this implementation.
     *
     * @param marker the marker to filter log messages; may be null to indicate no filtering based on markers
     * @param level  the logging level to check against
     * @return true if the current logging level is less than or equal to the specified level, false otherwise
     */
    public boolean isEnabled(@Nullable Marker marker, Level level) {
        return isEnabled(level);
    }

    @Override
    public boolean isTraceEnabled() {
        return isEnabled(Level.TRACE);
    }

    @Override
    public boolean isTraceEnabled(@Nullable Marker marker) {
        return isEnabled(marker, Level.TRACE);
    }

    @Override
    public boolean isDebugEnabled() {
        return isEnabled(Level.DEBUG);
    }

    @Override
    public boolean isDebugEnabled(@Nullable Marker marker) {
        return isEnabled(marker, Level.DEBUG);
    }

    @Override
    public boolean isInfoEnabled() {
        return isEnabled(Level.INFO);
    }

    @Override
    public boolean isInfoEnabled(@Nullable Marker marker) {
        return isEnabled(marker, Level.INFO);
    }

    @Override
    public boolean isWarnEnabled() {
        return isEnabled(Level.WARN);
    }

    @Override
    public boolean isWarnEnabled(@Nullable Marker marker) {
        return isEnabled(marker, Level.WARN);
    }

    @Override
    public boolean isErrorEnabled() {
        return isEnabled(Level.ERROR);
    }

    @Override
    public boolean isErrorEnabled(@Nullable Marker marker) {
        return isEnabled(marker, Level.ERROR);
    }

    /**
     * Retrieves the current logging level. If not explicitly set, returns the default level.
     *
     * @return the current logging level if set, otherwise the default logging level
     */
    public Level getLevel() {
        return level != null ? level : defaultLevel;
    }

    /**
     * Sets the logging level for this logger.
     *
     * @param level the new logging level to be set
     */
    public void setLevel(Level level) {
        this.level = level;
    }

    /**
     * Read an instance from an {@link java.io.ObjectInputStream}.
     * <p>
     * <strong>This method is unimplemented and will throw a {@link NotSerializableException}!</strong>
     *
     * @param in the ObjectInputStream instance used for reading the object during deserialization
     * @throws NotSerializableException always thrown to prevent deserialization of the object
     */
    @Serial
    private void readObject(java.io.ObjectInputStream in) throws NotSerializableException {throw new NotSerializableException("com.dua3.utility.logging.slf4j.LoggerSlf4j");}

    /**
     * Write an instance to a {@link java.io.ObjectInputStream}.
     * <p>
     * <strong>This method is unimplemented and will throw a {@link NotSerializableException}!</strong>
     *
     * @param out the ObjectOutputStream to which the object should be written
     * @throws NotSerializableException always thrown to prevent serialization of the object
     */
    @Serial
    private void writeObject(java.io.ObjectOutputStream out) throws NotSerializableException {throw new NotSerializableException("com.dua3.utility.logging.slf4j.LoggerSlf4j");}
}
