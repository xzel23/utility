package com.dua3.utility.logging.log4j;

import com.dua3.utility.lang.LangUtil;
import com.dua3.utility.logging.LogLevel;
import org.apache.logging.log4j.message.ReusableMessage;
import org.jspecify.annotations.Nullable;
import com.dua3.utility.logging.LogDispatcher;
import com.dua3.utility.logging.LogFilter;
import com.dua3.utility.logging.LogHandler;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * This class is an implementation of the Log4j Appender and LogHandlerPool interfaces.
 * It is used as an appender for log events and provides a mechanism for forwarding log4j log events to applications.
 */
public class LogAppenderLog4j extends AbstractAppender {

    /**
     * The name of the appender class used in the log4j configuration.
     *
     * <p>
     * This variable holds the simple name of the log4j appender class used in the application's log4j configuration.
     * </p>
     */
    public static final String APPENDER_NAME = LogAppenderLog4j.class.getSimpleName();

    /**
     * The list of handlers for log entries.
     */
    private final List<WeakReference<LogHandler>> handlers = new ArrayList<>();

    /**
     * The LogDispatcher associated with this LogAppenderLog4j instance.
     * The LogDispatcher is responsible for dispatching log entries to registered handlers based on a filter.
     */
    private final LogDispatcherLog4J dispatcher;

    /**
     * This class represents an implementation of the LogDispatcher interface using Log4J.
     * It dispatches log entries to registered handlers based on a filter.
     */
    public class LogDispatcherLog4J implements LogDispatcher {
        private volatile LogFilter filter = LogFilter.ALL_PASS_FILTER;

        /**
         * Constructor.
         */
        public LogDispatcherLog4J() { /* nothing to do */ }

        @Override
        public void addLogHandler(LogHandler handler) {
            handlers.add(new WeakReference<>(handler));
        }

        @Override
        public void removeLogHandler(LogHandler handler) {
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
            return handlers.stream().map(WeakReference::get).filter(Objects::nonNull).toList();
        }

        /**
         * Returns the LogAppenderLog4j instance associated with this LogDispatcherLog4J instance.
         *
         * @return the LogAppenderLog4j instance
         */
        public LogAppenderLog4j getAppender() {
            return LogAppenderLog4j.this;
        }
    }

    /**
     * Constructs a new instance of LogAppenderLog4j with the specified parameters.
     *
     * @param name              the name of the appender
     * @param filter            the filter to be used for filtering log events (nullable)
     * @param layout            the layout to be used for formatting log events (nullable)
     * @param ignoreExceptions  specifies whether exceptions should be ignored or not
     */
    protected LogAppenderLog4j(String name, @Nullable Filter filter, @Nullable Layout<? extends Serializable> layout,
                               final boolean ignoreExceptions) {
        super(name, filter, layout, ignoreExceptions, Property.EMPTY_ARRAY);
        this.dispatcher = new LogDispatcherLog4J();
    }

    /**
     * Creates a LogAppenderLog4j object with the specified parameters.
     *
     * @param name              the name of the appender
     * @param ignoreExceptions  whether to ignore exceptions thrown by the appender
     * @param layout            the layout for formatting log messages
     * @param filter            the filter for filtering log events
     * @return a LogAppenderLog4j object
     */
    @PluginFactory
    public static LogAppenderLog4j createAppender(
            @PluginAttribute("name") @Nullable String name,
            @PluginAttribute("ignoreExceptions") boolean ignoreExceptions,
            @PluginElement("Layout") @Nullable Layout<? extends Serializable> layout,
            @PluginElement("Filters") @Nullable Filter filter) {

        if (name == null) {
            LOGGER.warn("No name provided for {}", APPENDER_NAME);
            name = "[unnamed]";
        }
        if (layout == null) {
            layout = PatternLayout.createDefaultLayout();
        }
        return new LogAppenderLog4j(name, filter, layout, ignoreExceptions);
    }

    /**
     * Dispatches a log event to the list of log entry handlers.
     * <p>
     * This method iterates over the list of log entry handlers and invokes the `handleEntry` method
     * on each not garbage collected handler.
     * <p>
     * If any garbage collected log entry handler is detected, the log entry list is cleaned up.
     *
     * @param event the log event to be appended
     */
    @Override
    public void append(LogEvent event) {
        var evtInstant = event.getInstant();
        var instant = Instant.ofEpochSecond(evtInstant.getEpochSecond(), evtInstant.getNanoOfSecond());
        LogLevel level = LogUtilLog4J.translate(event.getLevel());
        Supplier<String> msg = event.getMessage() instanceof ReusableMessage rm
                ? () -> rm.getFormattedMessage()
                : LangUtil.cachingStringSupplier(event.getMessage()::getFormattedMessage);

        String mrk = event.getMarker() == null ? "" : event.getMarker().getName();
        boolean pass = dispatcher.filter.test(
                instant,
                event.getLoggerName(),
                level,
                mrk,
                msg,
                "",
                event.getThrown()
        );

        Iterator<WeakReference<LogHandler>> iterator = handlers.iterator();
        while (iterator.hasNext()) {
            LogHandler handler = iterator.next().get();
            if (handler == null) {
                iterator.remove();
            } else if (pass) {
                handler.handle(
                        instant,
                        event.getLoggerName(),
                        level,
                        mrk,
                        msg,
                        "",
                        event.getThrown()
                );
            }
        }
    }

    /**
     * Returns the LogDispatcher associated with the LogAppenderLog4j instance.
     *
     * @return the LogDispatcher
     */
    public LogDispatcherLog4J dispatcher() {
        return dispatcher;
    }

}