package com.dua3.utility.logging.log4j;

import com.dua3.utility.logging.LogEntryHandler;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.message.SimpleMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the {@link LogAppenderLog4j} class.
 */
@Execution(ExecutionMode.SAME_THREAD)
class LogAppenderLog4jTest {

    private LogAppenderLog4j appender;
    private LogAppenderLog4j.LogEntryDispatcherLog4J dispatcher;

    @BeforeEach
    void setUp() {
        appender = new LogAppenderLog4j("TestAppender", null, null, false);
        dispatcher = appender.dispatcher();
    }

    @Test
    void testCreateAppender() {
        // Test with null name
        LogAppenderLog4j appender1 = LogAppenderLog4j.createAppender(null, false, null, null);
        assertNotNull(appender1);
        assertEquals("[unnamed]", appender1.getName());

        // Test with name
        LogAppenderLog4j appender2 = LogAppenderLog4j.createAppender("TestAppender", false, null, null);
        assertNotNull(appender2);
        assertEquals("TestAppender", appender2.getName());
    }

    @Test
    void testAppend() {
        // Create a mock handler
        AtomicBoolean handlerCalled = new AtomicBoolean(false);
        LogEntryHandler handler = entry -> handlerCalled.set(true);

        // Add the handler to the dispatcher
        dispatcher.addLogEntryHandler(handler);

        // Create a log event
        LogEvent event = Log4jLogEvent.newBuilder().setLoggerName("TestLogger").setLevel(Level.INFO).setMessage(new SimpleMessage("Test message")).build();

        // Append the event
        appender.append(event);

        // Verify that the handler was called
        assertTrue(handlerCalled.get());
    }

    @Test
    void testDispatcher() {
        // Verify that the dispatcher is not null
        assertNotNull(dispatcher);

        // Verify that the dispatcher's appender is the same as the appender
        assertSame(appender, dispatcher.getAppender());
    }

    @Test
    void testAddRemoveLogEntryHandler() {
        // Create a mock handler
        LogEntryHandler handler = entry -> {};

        // Add the handler to the dispatcher
        dispatcher.addLogEntryHandler(handler);

        // Verify that the handler was added
        Collection<LogEntryHandler> handlers = dispatcher.getLogEntryHandlers();
        assertTrue(handlers.contains(handler));

        // Remove the handler
        dispatcher.removeLogEntryHandler(handler);

        // Verify that the handler was removed
        handlers = dispatcher.getLogEntryHandlers();
        assertFalse(handlers.contains(handler));
    }

    @Test
    void testSetGetFilter() {
        // Create a filter that always returns true
        com.dua3.utility.logging.LogEntryFilter trueFilter = entry -> true;

        // Set the filter
        dispatcher.setFilter(trueFilter);

        // Verify that the filter was set
        assertSame(trueFilter, dispatcher.getFilter());
    }
}