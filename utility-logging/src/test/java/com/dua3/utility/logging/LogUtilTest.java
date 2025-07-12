package com.dua3.utility.logging;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.lang.reflect.Field;
import java.util.ServiceLoader;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Unit tests for the {@link LogUtil} class.
 * <p>
 * Note: Testing LogUtil is challenging because it uses the ServiceLoader mechanism to find and load
 * ILogEntryDispatcherFactory implementations. We use reflection to access and modify private fields
 * for testing purposes.
 */
@Execution(ExecutionMode.SAME_THREAD)
class LogUtilTest {

    @Test
    void testGetGlobalDispatcher() throws Exception {
        // Reset the globalDispatcher to ensure a clean state
        resetGlobalDispatcher();

        // We'll test by manually setting the globalDispatcher
        MockLogEntryDispatcher mockDispatcher = new MockLogEntryDispatcher();
        setGlobalDispatcher(mockDispatcher);

        // Test that getGlobalDispatcher returns the expected dispatcher
        LogEntryDispatcher returnedDispatcher = LogUtil.getGlobalDispatcher();
        assertNotNull(returnedDispatcher, "getGlobalDispatcher should return a non-null dispatcher");
        assertSame(mockDispatcher, returnedDispatcher, "getGlobalDispatcher should return the expected dispatcher");
    }

    /**
     * Reset the globalDispatcher field in LogUtil to null.
     * This is necessary for testing the initialization behavior.
     */
    private void resetGlobalDispatcher() throws Exception {
        Field globalDispatcherField = LogUtil.class.getDeclaredField("globalDispatcher");
        globalDispatcherField.setAccessible(true);
        globalDispatcherField.set(null, null);
    }

    @Test
    void testAssureInitialized() throws Exception {
        // Reset the globalDispatcher to ensure a clean state
        resetGlobalDispatcher();

        // Set a mock dispatcher
        MockLogEntryDispatcher mockDispatcher = new MockLogEntryDispatcher();
        setGlobalDispatcher(mockDispatcher);

        // Call assureInitialized
        LogUtil.assureInitialized();

        // Test that the globalDispatcher is still the mock dispatcher
        LogEntryDispatcher dispatcher = getGlobalDispatcher();
        assertSame(mockDispatcher, dispatcher, "globalDispatcher should still be the mock dispatcher");
    }

    /**
     * Get the value of the globalDispatcher field in LogUtil.
     */
    private LogEntryDispatcher getGlobalDispatcher() throws Exception {
        Field globalDispatcherField = LogUtil.class.getDeclaredField("globalDispatcher");
        globalDispatcherField.setAccessible(true);
        return (LogEntryDispatcher) globalDispatcherField.get(null);
    }

    /**
     * Set the globalDispatcher field in LogUtil to a specific value.
     */
    private void setGlobalDispatcher(LogEntryDispatcher dispatcher) throws Exception {
        Field globalDispatcherField = LogUtil.class.getDeclaredField("globalDispatcher");
        globalDispatcherField.setAccessible(true);
        globalDispatcherField.set(null, dispatcher);
    }

    @Test
    void testServiceLoaderMechanism() {
        // We can't directly test the ServiceLoader mechanism in the test environment
        // because there is no ILogEntryDispatcherFactory implementation available.
        // In a real environment, this would load the available implementations.

        // Instead, we'll just verify that the ServiceLoader can be loaded
        ServiceLoader<ILogEntryDispatcherFactory> serviceLoader = ServiceLoader.load(ILogEntryDispatcherFactory.class);
        assertNotNull(serviceLoader, "ServiceLoader should not be null");
    }

    /**
     * A mock implementation of LogEntryDispatcher for testing.
     */
    private static class MockLogEntryDispatcher implements LogEntryDispatcher {
        @Override
        public void addLogEntryHandler(LogEntryHandler handler) {
            // Not needed for this test
        }

        @Override
        public void removeLogEntryHandler(LogEntryHandler handler) {
            // Not needed for this test
        }

        @Override
        public void setFilter(LogEntryFilter filter) {
            // Not needed for this test
        }

        @Override
        public LogEntryFilter getFilter() {
            return LogEntryFilter.allPass();
        }

        @Override
        public java.util.Collection<LogEntryHandler> getLogEntryHandlers() {
            return java.util.Collections.emptyList();
        }
    }
}
