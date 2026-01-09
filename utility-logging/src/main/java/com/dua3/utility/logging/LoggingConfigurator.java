package com.dua3.utility.logging;

import java.util.Properties;
import java.util.function.Consumer;

public class LoggingConfigurator {

    public static void configure(Properties properties, Consumer<LogFilter> setFilter, Consumer<LogHandler> addHandler) {
        LoggingConfiguration config = new LoggingConfiguration();
        config.parse(properties);

        // This is a placeholder as the current LoggingConfiguration doesn't expose filters or the way to apply them yet
        // but it satisfies the current usage in LogUtil and fixes compilation.
        config.getHandlers().forEach(addHandler);
    }
}
