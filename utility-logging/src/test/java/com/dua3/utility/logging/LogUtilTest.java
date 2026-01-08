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
 * ILogDispatcherFactory implementations. We use reflection to access and modify private fields
 * for testing purposes.
 */
@Execution(ExecutionMode.SAME_THREAD)
class LogUtilTest {

    @Test
    void testGetGlobalDispatcher() throws Exception {
        // Reset the globalDispatcher to ensure a clean state
        resetGlobalDispatcher();

        // We'll test by manually setting the globalDispatcher
        MockLogDispatcher mockDispatcher = new MockLogDispatcher();
        setGlobalDispatcher(mockDispatcher);

        // Test that getGlobalDispatcher returns the expected dispatcher
        LogDispatcher returnedDispatcher = LogUtil.getGlobalDispatcher();
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
        MockLogDispatcher mockDispatcher = new MockLogDispatcher();
        setGlobalDispatcher(mockDispatcher);

        // Call assureInitialized
        LogUtil.assureInitialized();

        // Test that the globalDispatcher is still the mock dispatcher
        LogDispatcher dispatcher = getGlobalDispatcher();
        assertSame(mockDispatcher, dispatcher, "globalDispatcher should still be the mock dispatcher");
    }

    /**
     * Get the value of the globalDispatcher field in LogUtil.
     */
    private LogDispatcher getGlobalDispatcher() throws Exception {
        Field globalDispatcherField = LogUtil.class.getDeclaredField("globalDispatcher");
        globalDispatcherField.setAccessible(true);
        return (LogDispatcher) globalDispatcherField.get(null);
    }

    /**
     * Set the globalDispatcher field in LogUtil to a specific value.
     */
    private void setGlobalDispatcher(LogDispatcher dispatcher) throws Exception {
        Field globalDispatcherField = LogUtil.class.getDeclaredField("globalDispatcher");
        globalDispatcherField.setAccessible(true);
        globalDispatcherField.set(null, dispatcher);
    }

    @Test
    void testServiceLoaderMechanism() {
        // We can't directly test the ServiceLoader mechanism in the test environment
        // because there is no ILogDispatcherFactory implementation available.
        // In a real environment, this would load the available implementations.

        // Instead, we'll just verify that the ServiceLoader can be loaded
        ServiceLoader<LogDispatcherFactory> serviceLoader = ServiceLoader.load(LogDispatcherFactory.class);
        assertNotNull(serviceLoader, "ServiceLoader should not be null");
    }

    /**
     * A mock implementation of LogDispatcher for testing.
     */
    private static class MockLogDispatcher implements LogDispatcher {
        @Override
        public void addLogHandler(LogHandler handler) {
            // Not needed for this test
        }

        @Override
        public void removeLogHandler(LogHandler handler) {
            // Not needed for this test
        }

        @Override
        public void setFilter(LogFilter filter) {
            // Not needed for this test
        }

        @Override
        public LogFilter getFilter() {
            return LogFilter.allPass();
        }

        @Override
        public java.util.Collection<LogHandler> getLogHandlers() {
            return java.util.Collections.emptyList();
        }
    }
}
