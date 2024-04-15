package com.dua3.utility.logging.log4j;

import com.dua3.utility.logging.LogEntryDispatcher;
import com.dua3.utility.logging.LogUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.util.Arrays;
import java.util.Collection;
import java.util.ServiceConfigurationError;

/**
 * A utility class for creating and managing Log4j appenders.
 * <p>
 * <b>Rerouting logging to Log4J</b><br>
 * <ul>
 *     <li><b>JUL (java.util.logging):</b> add {@code log4j-jul} to your dependencies and add a static initializer
 *     block before declaring any Logger:
 *     <pre>
 *        static {
 *            System.setProperty("java.util.logging.manager", "org.apache.logging.log4j.jul.LogManager");
 *        }
 *     </pre>
 *     <li><b>SLF4J:</b> add {@code log4j-slf4j2-impl} to your dependencies. Do not add any other logging implementation
 *     based on SLF4J (i. e., SimpleLogger, Logback).
 * </ul>
 */
public final class LogUtilLog4J {

    private LogUtilLog4J() {
    }

    /**
     * Check if the default dispatcher factory implementation is the Log4J implementation.
     * @return true, if the Log4J implementation is used
     */
    public static boolean isDefaultImplementation() {
        return LogUtil.getGlobalDispatcher() instanceof LogAppenderLog4j;
    }

    /**
     * Creates a LogAppenderLog4j instance. NOTE: The appender is not started.
     *
     * @param ctx  the LoggerContext to use
     * @param name the name of the appender
     * @return a LogAppenderLog4j instance
     */
    public static LogAppenderLog4j createAppender(LoggerContext ctx, String name) {
        PatternLayout layout = PatternLayout.createDefaultLayout(ctx.getConfiguration());
        return LogAppenderLog4j.createAppender(name, true, layout, null);
    }

    /**
     * Creates a LogAppenderLog4j instance, starts it, attaches it to all loggers in the given LoggerContext, and updates the LoggerContext.
     *
     * @param ctx  the LoggerContext to use
     * @param name the name of the appender
     * @return a LogAppenderLog4j instance
     */
    public static LogAppenderLog4j createAppenderAndAttachAllLoggers(LoggerContext ctx, String name) {
        return createAppenderAndAttachLoggers(ctx, name, ctx.getConfiguration().getLoggers().values());
    }

    /**
     * Creates a LogAppenderLog4j instance, starts it, attaches it to the given loggers, and updates the LoggerContext.
     *
     * @param ctx     the LoggerContext to use
     * @param name    the name of the appender
     * @param loggers the loggers to attach to the appender
     * @return a LogAppenderLog4j instance
     */
    public static LogAppenderLog4j createAppenderAndAttachLoggers(LoggerContext ctx, String name, LoggerConfig... loggers) {
        return createAppenderAndAttachLoggers(ctx, name, Arrays.asList(loggers));
    }

    /**
     * Creates a LogAppenderLog4j instance, starts it, attaches it to the given loggers, and updates the LoggerContext.
     *
     * @param ctx     the LoggerContext to use
     * @param name    the name of the appender
     * @param loggers a collection of LoggerConfig instances to attach the appender to
     * @return a LogAppenderLog4j instance
     */
    public static LogAppenderLog4j createAppenderAndAttachLoggers(LoggerContext ctx, String name, Collection<? extends LoggerConfig> loggers) {
        LogAppenderLog4j appender = createAppender(ctx, name);
        appender.start();
        ctx.getConfiguration().addAppender(appender);
        loggers.forEach(logger -> logger.addAppender(appender, null, null));
        ctx.updateLoggers();

        return appender;
    }

    /**
     * Returns the global LogEntryDispatcher by using the available ILogEntryDispatcherFactory implementations loaded
     * through ServiceLoader and connects all known loggers to it.
     * <p>
     * NOTE: This method delegates to {@link LogUtil#getGlobalDispatcher()}.
     *
     * @return The global LogEntryDispatcher instance.
     * @throws ServiceConfigurationError if no factories can create a LogEntryDispatcher.
     * @throws IllegalStateException if the implementations do not match
     */
    public static LogAppenderLog4j getGlobalDispatcher() {
        LogEntryDispatcher dispatcher = LogUtil.getGlobalDispatcher();
        if(dispatcher instanceof LogAppenderLog4j logAppenderLog4j) {
            return logAppenderLog4j;
        }
        throw new IllegalStateException("wrong implementation: " + dispatcher.getClass());
    }

}
