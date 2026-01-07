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

    private static @Nullable LogEntryDispatcher globalDispatcher;

    private static synchronized void init() {
        if (globalDispatcher == null) {
            ServiceLoader<ILogEntryDispatcherFactory> serviceLoader = ServiceLoader.load(ILogEntryDispatcherFactory.class);

            for (ILogEntryDispatcherFactory factory : serviceLoader) {
                try {
                    LogEntryDispatcher dispatcher = factory.getDispatcher();
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
                LoggingConfigurator.configure(properties, globalDispatcher::setFilter, globalDispatcher::addLogEntryHandler)
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
     * @throws ServiceConfigurationError if no factories can create a LogEntryDispatcher
     */
    public static void assureInitialized() {
        if (globalDispatcher == null) {
            init();
        }
    }

    /**
     * Returns the global LogEntryDispatcher by using the available ILogEntryDispatcherFactory implementations loaded
     * through ServiceLoader and connects all known loggers to it.
     *
     * @return The global LogEntryDispatcher instance.
     * @throws ServiceConfigurationError if no factories can create a LogEntryDispatcher.
     */
    public static LogEntryDispatcher getGlobalDispatcher() {
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
