package com.dua3.utility.logging.log4j;

import com.dua3.utility.logging.LogDispatcher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Unit tests for the {@link LogDispatcherFactoryLog4j} class.
 */
@Execution(ExecutionMode.SAME_THREAD)
class LogDispatcherFactoryLog4jTest {

    @Test
    void testGetDispatcher() {
        // Create a factory
        LogDispatcherFactoryLog4j factory = new LogDispatcherFactoryLog4j();

        // Get the dispatcher
        LogDispatcher dispatcher = factory.getDispatcher();

        // Verify that the dispatcher is not null
        assertNotNull(dispatcher);

        // Verify that the dispatcher is an instance of LogAppenderLog4j.LogDispatcherLog4J
        assertInstanceOf(LogAppenderLog4j.LogDispatcherLog4J.class, dispatcher);

        // Verify that the dispatcher is the same as the global appender's dispatcher
        assertSame(LogUtilLog4J.GLOBAL_APPENDER.dispatcher(), dispatcher);
    }

    @Test
    void testStaticInitializer() {
        // It's difficult to test the static initializer directly
        // The static initializer calls LogUtilLog4J.updateLoggers(), which is difficult to verify
        // Just create a factory to ensure the static initializer runs without exceptions
        LogDispatcherFactoryLog4j factory = new LogDispatcherFactoryLog4j();
        assertNotNull(factory);
    }
}