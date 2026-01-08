package com.dua3.utility.logging;

import com.dua3.utility.logging.backend.universal.UniversalDispatcher;
import com.dua3.utility.logging.backend.jul.JulHandler;
import org.apache.logging.log4j.LogManager;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

/**
 * Utility class for logging operations.
 */
public final class LogUtil {
    private LogUtil() { /* utility class */ }

    private static @Nullable LogDispatcher globalDispatcher;

    private static synchronized void init() {
        if (globalDispatcher == null) {
            ServiceLoader<LogDispatcherFactory> serviceLoader = ServiceLoader.load(LogDispatcherFactory.class);

            for (LogDispatcherFactory factory : serviceLoader) {
                try {
                    LogDispatcher dispatcher = factory.getDispatcher();
                    if (dispatcher != null) {
                        globalDispatcher = dispatcher;
                        LogManager.getLogger(LogUtil.class).trace("created dispatcher of class {} using factory {}", dispatcher.getClass(), factory.getClass());
                        return;
                    }
                } catch (Exception e) {
                }
            }

            throw new ServiceConfigurationError("no factories left to try - could not create a dispatcher");
        }
    }

    /**
     * Initializes the unified logging system by setting up various logging frameworks
     * to work with a global dispatch mechanism.
     * <p>
     * Once invoked, this method performs the following:
     * 1. Acquires an instance of the global dispatcher.
     * 2. Wires several logging frameworks, including:
     *    - Log4j
     *    - SLF4J
     *    - Java Util Logging (JUL)
     *    - Jakarta Commons Logging (JCL)
     * 3. Optionally configures logging properties using a custom logging configurator
     *    if logging properties are present.
     * <p>
     * This method ensures that all supported logging frameworks are unified under a
     * single global dispatcher, allowing centralized logging management. It is not
     * necessary to install any bridge libraries for the various frameworks.
     */
    public static synchronized void initUnifiedLogging() {
        globalDispatcher = UniversalDispatcher.getInstance();

        // LOG4J
        wireLog4j();

        // SLF4J
        wireSlf4j();

        // JUL
        wireJul();

        // JCL
        wireJcl();

        getLoggingProperties().ifPresent(properties ->
                LoggingConfigurator.configure(properties, globalDispatcher::setFilter, globalDispatcher::addLogHandler)
        );
    }

    private static void wireJul() {
        java.util.logging.Logger root = java.util.logging.LogManager.getLogManager().getLogger("");
        // Remove existing handlers to avoid duplicates
        for (var h : root.getHandlers()) root.removeHandler(h);
        // Add your bridge
        root.addHandler(new JulHandler());
        root.setLevel(java.util.logging.Level.ALL);
    }

    private static void wireJcl() {
        System.setProperty("org.apache.commons.logging.LogFactory", "org.apache.commons.logging.impl.LogFactoryImpl");
        System.setProperty("org.apache.commons.logging.Log", "com.dua3.utility.logging.backend.jcl.LoggerJcl");
    }

    private static void wireLog4j() {
        System.setProperty("log4j2.loggerContextFactory", "com.dua3.utility.logging.backend.log4j.Log4jLoggerContextFactory");
    }

    private static void wireSlf4j() {
        System.setProperty("slf4j.provider", "com.dua3.utility.logging.backend.slf4j.LoggingServiceProviderSlf4j");
    }

    /**
     * Checks if the globalDispatcher variable is null and initializes it by calling the init() method if necessary.
     *
     * @throws ServiceConfigurationError if no factories can create a LogDispatcher
     */
    public static void assureInitialized() {
        if (globalDispatcher == null) {
            init();
        }
    }

    /**
     * Returns the global LogDispatcher by using the available ILogDispatcherFactory implementations loaded
     * through ServiceLoader and connects all known loggers to it.
     *
     * @return The global LogDispatcher instance.
     * @throws ServiceConfigurationError if no factories can create a LogDispatcher.
     */
    public static LogDispatcher getGlobalDispatcher() {
        assureInitialized();
        assert globalDispatcher != null;
        return globalDispatcher;
    }


    private static Optional<Properties> getLoggingProperties() {
        Properties properties = new Properties();
        try (InputStream in = ClassLoader.getSystemResourceAsStream("logging.properties")) {
            if (in == null) {
                return Optional.empty();
            } else {
                properties.load(in);
                return Optional.of(properties);
            }
        } catch (IOException e) {
            e.printStackTrace(System.err);
            return Optional.empty();
        }
    }

}
