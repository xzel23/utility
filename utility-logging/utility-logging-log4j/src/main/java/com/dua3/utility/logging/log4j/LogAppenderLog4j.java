package com.dua3.utility.logging.log4j;

import com.dua3.cabe.annotations.Nullable;
import com.dua3.utility.logging.LogEntry;
import com.dua3.utility.logging.LogEntryHandler;
import com.dua3.utility.logging.LogEntryDispatcher;
import com.dua3.utility.logging.LogLevel;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.spi.StandardLevel;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * This class is an implementation of the Log4j Appender and LogEntryHandlerPool interfaces.
 * It is used as an appender for log events and provides a mechanism for forwarding log4j log events to applications.
 */
public class LogAppenderLog4j extends AbstractAppender implements LogEntryDispatcher {
    public static final String APPENDER_NAME = LogAppenderLog4j.class.getSimpleName();
    private final List<LogEntryHandler> handlers = new ArrayList<>();

    protected LogAppenderLog4j(String name, @Nullable Filter filter, @Nullable Layout<? extends Serializable> layout,
                               final boolean ignoreExceptions) {
        super(name, filter, layout, ignoreExceptions);
    }

    @PluginFactory
    public static LogAppenderLog4j createAppender(
            @PluginAttribute("name") @Nullable String name,
            @PluginAttribute("ignoreExceptions") boolean ignoreExceptions,
            @PluginElement("Layout") @Nullable Layout<? extends Serializable> layout,
            @PluginElement("Filters") @Nullable Filter filter) {

        if (name == null) {
            LOGGER.error("No name provided for {}", APPENDER_NAME);
            return null;
        }
        if (layout == null) {
            layout = PatternLayout.createDefaultLayout();
        }
        return new LogAppenderLog4j(name, filter, layout, ignoreExceptions);
    }

    @Override
    public void append(LogEvent event) {
        handlers.forEach(handler -> handler.handleEntry(new LogEntry(
                event.getLoggerName(),
                Instant.ofEpochMilli(event.getTimeMillis()),
                translate(event.getLevel()),
                event.getMarker().getName(),
                () -> event.getMessage().getFormattedMessage(),
                event.getThrown()
        )));
    }

    private LogLevel translate(Level level) {
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

    @Override
    public void addLogEntryHandler(LogEntryHandler handler) {
        handlers.add(handler);
    }

    @Override
    public void removeLogEntryHandler(LogEntryHandler handler) {
        handlers.add(handler);
    }

    @Override
    public Collection<LogEntryHandler> getLogEntryHandlers() {
        return Collections.unmodifiableCollection(handlers);
    }
}