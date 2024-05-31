package com.dua3.utility.logging.log4j;

import com.dua3.utility.lang.LangUtil;
import com.dua3.utility.logging.LogEntryDispatcher;
import com.dua3.utility.logging.LogLevel;
import com.dua3.utility.logging.LogUtil;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.spi.StandardLevel;

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
    // NOTE: do not use logging in this class as it interferes with LogManager creation!

    private LogUtilLog4J() {
    }

    /**
     * Check if the default dispatcher factory implementation is the Log4J implementation.
     * @return true, if the Log4J implementation is used
     */
    public static boolean isDefaultImplementation() {
        return LogUtil.getGlobalDispatcher() instanceof LogAppenderLog4j.LogEntryDispatcherLog4J;
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
    public static LogAppenderLog4j.LogEntryDispatcherLog4J getGlobalDispatcher() {
        LogEntryDispatcher dispatcher = LogUtil.getGlobalDispatcher();
        if (dispatcher instanceof LogAppenderLog4j.LogEntryDispatcherLog4J log4jDispatcher) {
            return log4jDispatcher;
        }
        throw new IllegalStateException("wrong implementation: " + dispatcher.getClass());
    }

    /**
     * Translates a Log4J Level object to a custom LogLevel object.
     * <p>
     * This method takes a Log4J Level object as parameter and returns the corresponding custom LogLevel object.
     * The translation is based on the integer level value of the Log4J Level object.
     *
     * @param level the Log4J Level object to be translated
     * @return the custom LogLevel object that corresponds to the given Log4J Level object
     */
    public static LogLevel translate(Level level) {
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
     * Translates a {@link LogLevel} object to a Log4J {@link Level} object.
     * <p>
     * This method takes a LogLevel Level object as parameter and returns the corresponding Log4J Level object.
     *
     * @param level the LogLevel object to be translated
     * @return the Log4j Level object that corresponds to the given LogLevel object
     */
    public static Level translate(LogLevel level) {
        return switch (level) {
            case TRACE -> Level.TRACE;
            case DEBUG -> Level.DEBUG;
            case INFO -> Level.INFO;
            case WARN -> Level.WARN;
            case ERROR -> Level.ERROR;
        };
    }

    /**
     * Configures the Log4J bridge implementations that are available.
     * @param rootLevel the root level to set
     */
    public static void init(LogLevel rootLevel) {
        // configure the JUL bridge
        setPropertyIfOnClassPath("java.util.logging.manager", "org.apache.logging.log4j.jul.LogManager");
        // configure the commons-logging bridge
        setPropertyIfOnClassPath("org.apache.commons.logging.LogFactory", "org.apache.logging.log4j.jcl.LogFactoryImpl");
        // no configuration necessary for SLF4J

        // set the root logger level
        Configurator.setRootLevel(translate(rootLevel));
    }

    private static void setPropertyIfOnClassPath(String propertyName, String className) {
        if (isClassOnClasspath(className)) {
            System.setProperty(propertyName, className);
        }
    }

    /**
     * Check if a class is on the classpath without loading it.
     *
     * @param className the fully qualified name of the class
     * @return true, if the class is on the classpath
     */
    // DO NOT MOVE TO LANGUTIL OR CALL LANGUTIL METHODS!!!
    // LANGUTIL INSTANTIATES A LOGGER BUT INITIALISATION MOST BE DONE BEFORE FIRST LOGGER IS INSTANTIATED!
    public static boolean isClassOnClasspath(String className) {
        if (!className.matches("^([a-zA-Z_$][a-zA-Z\\d_$]*\\.)*[a-zA-Z_$][a-zA-Z\\d_$]*(\\.[a-zA-Z_$][a-zA-Z\\d_$]*)*$")) {
            return false;
        }

        String classAsResource = className.replace('.', '/') + ".class";
        return ClassLoader.getSystemClassLoader().getResource(classAsResource) != null;
    }
}
