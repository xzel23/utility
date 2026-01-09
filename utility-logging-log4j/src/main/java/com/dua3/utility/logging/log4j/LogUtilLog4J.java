package com.dua3.utility.logging.log4j;

import com.dua3.utility.logging.LogLevel;
import com.dua3.utility.logging.LogUtil;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.spi.StandardLevel;

import java.util.regex.Pattern;

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
 *     based on SLF4J (i.e., SimpleLogger, Logback).
 * </ul>
 *
 * @deprecated will be removed in the next major version and functionality moved to {@link LogUtil}
 */
@Deprecated(forRemoval = true)
@SuppressWarnings("MagicCharacter")
public final class LogUtilLog4J {
    // NOTE: do not use logging in this class as it interferes with LogManager creation!

    static final LogAppenderLog4j GLOBAL_APPENDER = new LogAppenderLog4j(LogAppenderLog4j.class.getSimpleName() + "@global", null, null, false);
    private static final Pattern PATTERN_VALID_CLASS_NAME = Pattern.compile("^([a-zA-Z_$][a-zA-Z\\d_$]*\\.)*+[a-zA-Z_$][a-zA-Z\\d_$]*(\\.[a-zA-Z_$][a-zA-Z\\d_$]*)*+$");

    private LogUtilLog4J() {
    }

    /**
     * Check if the default dispatcher factory implementation is the Log4J implementation.
     * @return true, if the Log4J implementation is used
     */
    public static boolean isDefaultImplementation() {
        return LogUtil.getGlobalDispatcher() instanceof LogAppenderLog4j.LogDispatcherLog4J;
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
        Configuration configuration = LoggerContext.getContext(false).getConfiguration();
        GLOBAL_APPENDER.start();
        configuration.addAppender(GLOBAL_APPENDER);
        LoggerConfig rootLogger = configuration.getRootLogger();
        rootLogger.addAppender(GLOBAL_APPENDER, null, null);
        rootLogger.setLevel(translate(rootLevel));

        LogUtil.assureInitialized();
    }

    /**
     * Updates all loggers to add the global appender and refresh their configurations.
     *
     * <p>
     * This method retrieves the current {@link LoggerContext} from {@link LogManager} and iterates through
     * all available loggers, adding the global appender to each logger. After updating
     * the loggers with the global appender, it calls the updateLoggers method of the {@link LoggerContext}
     * to apply the changes.
     *
     * <p>
     * This method ensures that all loggers in the application are configured with
     * the globally defined appender, which might be necessary for consistent logging
     * behavior across different parts of the application.
     */
    public static void updateLoggers() {
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        ctx.getLoggers().forEach(logger -> logger.addAppender(GLOBAL_APPENDER));
        ctx.updateLoggers();
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
        if (!PATTERN_VALID_CLASS_NAME.matcher(className).matches()) {
            return false;
        }

        String classAsResource = className.replace('.', '/') + ".class";
        return ClassLoader.getSystemClassLoader().getResource(classAsResource) != null;
    }
}
