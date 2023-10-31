package com.dua3.utility.logging.log4j;

import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.util.Arrays;
import java.util.Collection;

/**
 * A utility class for creating and managing Log4j appenders.
 */
public final class LogUtilLog4J {

    private LogUtilLog4J() {}

    /**
     * Creates a Dua3LogAppenderLog4j instance. NOTE: The appender is not started.
     *
     * @param ctx  the LoggerContext to use
     * @param name the name of the appender
     * @return a Dua3LogAppenderLog4j instance
     */
    public static LogAppenderLog4j createAppender(LoggerContext ctx, String name) {
        PatternLayout layout = PatternLayout.createDefaultLayout(ctx.getConfiguration());
        return LogAppenderLog4j.createAppender(name, true, layout, null);
    }

    /**
     * Creates a Dua3LogAppenderLog4j instance, starts it, attaches it to all loggers in the given LoggerContext, and updates the LoggerContext.
     *
     * @param ctx  the LoggerContext to use
     * @param name the name of the appender
     * @return a Dua3LogAppenderLog4j instance
     */
    public static LogAppenderLog4j createAppenderAndAttachAllLoggers(LoggerContext ctx, String name) {
        return createAppenderAndAttachLoggers(ctx, name, ctx.getConfiguration().getLoggers().values());
    }

    /**
     * Creates a Dua3LogAppenderLog4j instance, starts it, attaches it to the given loggers, and updates the LoggerContext.
     *
     * @param ctx     the LoggerContext to use
     * @param name    the name of the appender
     * @param loggers the loggers to attach to the appender
     * @return a Dua3LogAppenderLog4j instance
     */
    public static LogAppenderLog4j createAppenderAndAttachLoggers(LoggerContext ctx, String name, LoggerConfig... loggers) {
        return createAppenderAndAttachLoggers(ctx, name, Arrays.asList(loggers));
    }

    /**
     * Creates a Dua3LogAppenderLog4j instance, starts it, attaches it to the given loggers, and updates the LoggerContext.
     *
     * @param ctx     the LoggerContext to use
     * @param name    the name of the appender
     * @param loggers a collection of LoggerConfig instances to attach the appender to
     * @return a Dua3LogAppenderLog4j instance
     */
    public static LogAppenderLog4j createAppenderAndAttachLoggers(LoggerContext ctx, String name, Collection<? extends LoggerConfig> loggers) {
        LogAppenderLog4j appender = createAppender(ctx, name);
        appender.start();
        ctx.getConfiguration().addAppender(appender);
        loggers.forEach(logger -> logger.addAppender(appender, null, null));
        ctx.updateLoggers();

        return appender;
    }

}
