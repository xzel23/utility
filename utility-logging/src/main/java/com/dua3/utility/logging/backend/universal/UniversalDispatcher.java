package com.dua3.utility.logging.backend.universal;

import com.dua3.utility.lang.LangUtil;
import com.dua3.utility.logging.LogDispatcher;
import com.dua3.utility.logging.LogFilter;
import com.dua3.utility.logging.LogHandler;
import com.dua3.utility.logging.LogLevel;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.spi.StandardLevel;
import org.jspecify.annotations.Nullable;
import org.slf4j.helpers.MessageFormatter;
import org.slf4j.spi.LocationAwareLogger;

import java.lang.ref.WeakReference;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;
import java.util.logging.LogRecord;

/**
 * A centralized dispatcher for handling and processing log events across different logging frameworks.
 * <p>
 * The CommonDispatcher acts as a bridge between multiple logging APIs, providing unified log event
 * dispatching to registered handlers. It supports the logging frameworks Log4j, SLF4J, Java Util Logging (JUL),
 * and Jakarta Commons Logging (JCL), enabling consistent processing of log events regardless of the source.
 * <p>
 * The class follows a singleton pattern to ensure a single instance is used throughout the application.
 */
public class UniversalDispatcher implements LogDispatcher {

    /**
     * A private static final class that holds a singleton instance of {@link UniversalDispatcher}.
     * This implementation leverages the "Initialization-on-demand holder idiom" to ensure
     * thread-safe, lazy initialization of the singleton instance.
     */
    private static final class SingletonHolder {
        private static final UniversalDispatcher INSTANCE = new UniversalDispatcher();
    }

    /**
     * Provides the singleton instance of the CommonDispatcher.
     * <p>
     * This method ensures a single instance of CommonDispatcher is shared
     * across the application using a holder class for lazy initialization.
     *
     * @return the singleton instance of CommonDispatcher
     */
    public static UniversalDispatcher getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private LogFilter filter = LogFilter.allPass();

    /**
     * A thread-safe list of weak references to LogHandler instances. This guarantees that the handlers
     * can be accessed concurrently without external synchronization and minimizes memory retention by
     * allowing garbage collection of handlers no longer in use.
     */
    private final List<WeakReference<LogHandler>> handlers = new CopyOnWriteArrayList<>();

    /**
     * Default constructor for the CommonDispatcher class.
     * <p>
     * This constructor initializes an instance of the CommonDispatcher class
     * without any specific configuration or parameters.
     */
    public UniversalDispatcher() {
        // nothing to do
    }

    @Override
    public void addLogHandler(LogHandler handler) {
        handlers.add(new WeakReference<>(handler));
    }

    @Override
    public synchronized void removeLogHandler(LogHandler handler) {
        handlers.removeIf(h -> h.get() == handler);
    }

    @Override
    public void setFilter(LogFilter filter) {
        this.filter = filter;
    }

    @Override
    public LogFilter getFilter() {
        return filter;
    }

    @Override
    public Collection<LogHandler> getLogHandlers() {
        return List.copyOf(handlers.stream().map(WeakReference::get).filter(Objects::nonNull).toList());
    }

    /**
     * Determines if Log4j logging is enabled for a specific logger name, level, and optional marker.
     * This method evaluates the logger name, log level, and marker to decide whether logging
     * should proceed.
     *
     * @param name the name of the logger to check; must not be null
     * @param level the Log4j {@code Level} to evaluate; must not be null
     * @param marker an optional {@code Marker} associated with the logger; may be null
     * @return true if logging is enabled for the specified logger name, level, and marker, false otherwise
     */
    public boolean isEnabledLog4j(String name, Level level, @Nullable Marker marker) {
        return true;
    }

    /**
     * Checks if logging is enabled for the provided SLF4J log level.
     *
     * @param level the SLF4J logging level to check
     * @return true if logging is enabled for the specified level, false otherwise
     */
    public boolean isEnabledSlf4j(org.slf4j.event.Level level) {
        return true;
    }

    /**
     * Determines if logging is enabled for a specific {@link LogLevel}.
     * This method evaluates the provided log level to determine whether
     * logging should proceed.
     *
     * @param logLevel the log level to check; must not be null
     * @return true if logging is enabled for the specified log level, false otherwise
     */
    public boolean isEnabled(LogLevel logLevel) {
        return true;
    }

    /**
     * Dispatches a Log4j log event to all registered {@link LogHandler} instances.
     * This method determines if the log event should be processed based on the
     * translated log level and forwards the event to handlers enabled for that level.
     *
     * @param name the fully qualified class name of the logger emitting the event; must not be null
     * @param level the Log4j {@code Level} of the log event; must not be null
     * @param marker an optional {@code Marker} associated with the log event; may be null
     * @param message the log message to be formatted and dispatched; must not be null
     * @param t an optional {@code Throwable} associated with the log event; may be null
     */
    public void dispatchLog4j(String name, Level level, @Nullable Marker marker, Message message, @Nullable Throwable t) {
        Instant instant = Instant.now();
        String mrk = marker == null ? "" : marker.getName();
        Supplier<String> msg = LangUtil.cachingStringSupplier(message::getFormattedMessage);

        LogLevel lvl = translateLog4jLevel(level);

        if (filter.test(instant, name, lvl, mrk, msg, "", t)) {
            for (WeakReference<LogHandler> handlerRef : handlers) {
                LogHandler handler = handlerRef.get();
                if (handler != null && handler.isEnabled(lvl)) {
                    handler.handle(instant, name, lvl, mrk, msg, "", t);
                }
            }
        }
    }

    /**
     * Translates a Log4j {@code Level} into the corresponding {@link LogLevel}.
     * This method maps Log4j log levels to the application's internal {@link LogLevel} enumeration
     * based on the relative severity of the levels.
     *
     * @param level the Log4j {@code Level} to be translated; must not be null
     * @return the {@link LogLevel} equivalent of the provided Log4j {@code Level}
     */
    private static LogLevel translateLog4jLevel(Level level) {
        int levelInt = level.intLevel();
        if (levelInt > StandardLevel.DEBUG.intLevel()) {
            return LogLevel.TRACE;
        }
        if (levelInt > StandardLevel.INFO.intLevel()) {
            return LogLevel.DEBUG;
        }
        if (levelInt > StandardLevel.WARN.intLevel()) {
            return LogLevel.INFO;
        }
        if (levelInt > StandardLevel.ERROR.intLevel()) {
            return LogLevel.WARN;
        }
        return LogLevel.ERROR;
    }

    /**
     * Formats a Log4j {@link Message} instance into its string representation.
     * This method retrieves the formatted message from the input {@code Message} object.
     *
     * @param message the Log4j {@link Message} object to format; must not be null
     * @return the formatted string representation of the provided {@link Message} object
     */
    private static String formatLog4jMessage(Message message) {
        return message.getFormattedMessage();
    }

    /**
     * Dispatches an SLF4J logging event to all registered {@link LogHandler} instances.
     * This method determines if the log event should be processed based on the log level and
     * forwards the event to handlers that are enabled for the translated log level.
     *
     * @param loggerName the name of the logger emitting the event; must not be null
     * @param level the SLF4J {@link org.slf4j.event.Level} of the log event; must not be null
     * @param marker an optional {@link org.slf4j.Marker} associated with the log event; may be null
     * @param messagePattern the SLF4J-style message pattern to be formatted; must not be null
     * @param arguments an optional array of arguments for the message pattern; may be null or empty
     * @param throwable an optional {@link Throwable} associated with the log event; may be null
     */
    public void dispatchSlf4j(String loggerName, org.slf4j.event.Level level, org.slf4j.@Nullable Marker marker, String messagePattern, @Nullable Object @Nullable [] arguments, @Nullable Throwable throwable) {
        Instant instant = Instant.now();
        String mrk = marker == null ? "" : marker.getName();
        Supplier<String> msg = LangUtil.cachingStringSupplier(() -> formatSlf4jMessage(messagePattern, arguments));

        LogLevel lvl = translateSlf4jLevel(level);
        if (filter.test(instant, loggerName, lvl, mrk, msg, "", throwable)) {
            for (WeakReference<LogHandler> handlerRef : handlers) {
                LogHandler handler = handlerRef.get();
                if (handler != null && handler.isEnabled(lvl)) {
                    handler.handle(instant, loggerName, lvl, mrk, msg, "", throwable);
                }
            }
        }
    }

    /**
     * Translates an SLF4J logging {@code Level} to the corresponding {@link LogLevel}.
     * This method maps SLF4J log levels to the application's internal {@link LogLevel} enumeration.
     *
     * @param level the SLF4J {@code Level} to translate; must not be null
     * @return the {@link LogLevel} equivalent of the provided SLF4J {@code Level}
     */
    private static LogLevel translateSlf4jLevel(org.slf4j.event.Level level) {
        int levelInt = level.toInt();
        if (levelInt < LocationAwareLogger.DEBUG_INT) {
            return LogLevel.TRACE;
        }
        if (levelInt < LocationAwareLogger.INFO_INT) {
            return LogLevel.DEBUG;
        }
        if (levelInt < LocationAwareLogger.WARN_INT) {
            return LogLevel.INFO;
        }
        if (levelInt < LocationAwareLogger.ERROR_INT) {
            return LogLevel.WARN;
        }
        return LogLevel.ERROR;
    }

    /**
     * Formats an SLF4J-style message pattern using the provided arguments.
     * If the arguments are null or empty, the raw message pattern is returned.
     *
     * @param messagePattern the SLF4J-style message pattern to be formatted; must not be null
     * @param arguments the arguments to replace placeholders in the message pattern;
     *                  may be null or an empty array
     * @return the formatted message if arguments are provided, or the raw message pattern
     *         if no arguments are given
     */
    private static String formatSlf4jMessage(String messagePattern, @Nullable Object @Nullable [] arguments) {
        if (arguments != null && arguments.length > 0) {
            return MessageFormatter.arrayFormat(messagePattern, arguments).getMessage();
        } else {
            return messagePattern;
        }
    }

    /**
     * Dispatches a log event derived from a {@link LogRecord}.
     * This method translates the JUL (Java Util Logging) level to the appropriate
     * {@link LogLevel}, formats the log message, and forwards the log record to all
     * registered {@link LogHandler} instances that are enabled for the translated log level.
     *
     * @param logRecord the {@code LogRecord} containing the log information; must not be null
     */
    public void dispatchJul(LogRecord logRecord) {
        Instant instant = Instant.now();
        Supplier<String> msg = LangUtil.cachingStringSupplier(() -> formatJulMessage(logRecord.getMessage(), logRecord.getParameters()));
        LogLevel lvl = translateJulLevel(logRecord.getLevel());

        if (filter.test(instant, logRecord.getLoggerName(), lvl, "", msg, "", logRecord.getThrown())) {
            for (WeakReference<LogHandler> handlerRef : handlers) {
                LogHandler handler = handlerRef.get();
                if (handler != null && handler.isEnabled(lvl)) {
                    handler.handle(instant, logRecord.getLoggerName(), lvl, "", msg, "", logRecord.getThrown());
                }
            }
        }
    }

    /**
     * Formats a message pattern using the provided parameters. If the parameters are null
     * or empty, the raw pattern is returned. In case of a formatting error, the method
     * falls back to returning the raw pattern.
     *
     * @param pattern the message pattern to be formatted; must not be null
     * @param params the parameters to replace the placeholders in the pattern; may be null
     *               or an empty array
     * @return the formatted message if formatting is successful, or the raw pattern if
     *         no parameters are provided or an error occurs during formatting
     */
    private static String formatJulMessage(String pattern, @Nullable Object @Nullable[] params) {
        if (params == null || params.length == 0) {
            return pattern;
        }
        try {
            return java.text.MessageFormat.format(pattern, params);
        } catch (Exception e) {
            return pattern; // Fallback to raw pattern on error
        }
    }

    /**
     * Translates a {@link java.util.logging.Level} to the corresponding {@link LogLevel}.
     * This method maps the {@code Level} instances from the `java.util.logging` API
     * to the application's internal {@code LogLevel} enumeration.
     *
     * @param level the {@code java.util.logging.Level} to be translated; must not be null
     * @return the {@code LogLevel} equivalent of the provided {@code java.util.logging.Level}
     */
    private static LogLevel translateJulLevel(java.util.logging.Level level) {
        int val = level.intValue();
        if (val <= java.util.logging.Level.FINEST.intValue()) return LogLevel.TRACE;
        if (val <= java.util.logging.Level.FINE.intValue()) return LogLevel.DEBUG;
        if (val <= java.util.logging.Level.INFO.intValue()) return LogLevel.INFO;
        if (val <= java.util.logging.Level.WARNING.intValue())return LogLevel.WARN;
        return LogLevel.ERROR;
    }

    /**
     * Dispatches a log event using the JCL (Jakarta Commons Logging) mechanism.
     * The method determines whether the log event should be handled based on its log level
     * and dispatches it to all registered {@link LogHandler} instances that are enabled
     * for the specified log level.
     *
     * @param name the name of the logger
     * @param level the log level of the event
     * @param message the log message to be dispatched; can be null
     * @param t an optional {@link Throwable} associated with the log event; can be null
     */
    public void dispatchJcl(String name, LogLevel level, @Nullable Object message, @Nullable Throwable t) {
        Instant instant = Instant.now();
        Supplier<String> msg = LangUtil.cachingStringSupplier(() -> String.valueOf(message));

        if (filter.test(instant, name, level, "", ()-> "", "", t)) {
            for (WeakReference<LogHandler> handlerRef : handlers) {
                LogHandler handler = handlerRef.get();
                if (handler != null && handler.isEnabled(level)) {
                    handler.handle(instant, name, level, "", msg, "", t);
                }
            }
        }
    }

}
