package com.dua3.utility.logging;

import org.junit.jupiter.api.Test;
import java.util.Properties;
import static org.junit.jupiter.api.Assertions.assertEquals;

class LoggingConfigurationTest {

    @Test
    void testAddToProperties() {
        Properties props = new Properties();
        props.setProperty("logging.level", "DEBUG");
        props.setProperty("logging.handlers", "h1,h2");
        props.setProperty("logging.handler.h1.type", "console");
        props.setProperty("logging.handler.h1.stream", "system.out");
        props.setProperty("logging.handler.h1.colored", "true");
        props.setProperty("logging.handler.h2.type", "console");
        props.setProperty("logging.handler.h2.stream", "system.err");
        props.setProperty("logging.handler.h2.colored", "false");

        LoggingConfiguration config = LoggingConfiguration.parse(props);

        Properties result = new Properties();
        config.addToProperties(result);

        assertEquals("DEBUG", result.getProperty("logging.level"));
        assertEquals("h1,h2", result.getProperty("logging.handlers"));
        assertEquals("console", result.getProperty("logging.handler.h1.type"));
        assertEquals("system.out", result.getProperty("logging.handler.h1.stream"));
        assertEquals("true", result.getProperty("logging.handler.h1.colored"));
        assertEquals("console", result.getProperty("logging.handler.h2.type"));
        assertEquals("system.err", result.getProperty("logging.handler.h2.stream"));
        assertEquals("false", result.getProperty("logging.handler.h2.colored"));
    }
}
