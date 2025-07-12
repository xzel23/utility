package com.dua3.utility.logging.slf4j;

import com.dua3.utility.logging.LogUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Unit tests for the {@link LogUtilSlf4j} class.
 */
@Execution(ExecutionMode.SAME_THREAD)
class LogUtilSlf4jTest {

    @Test
    void testIsDefaultImplementation() {
        // This test may pass or fail depending on the environment
        // Just call the method to ensure it doesn't throw an exception
        assertDoesNotThrow(LogUtilSlf4j::isDefaultImplementation);
    }

    @Test
    void testGetGlobalDispatcher() {
        assumeTrue(LogUtil.getGlobalDispatcher() instanceof LoggerFactorySlf4j);
        LoggerFactorySlf4j dispatcher = LogUtilSlf4j.getGlobalDispatcher();
        assertNotNull(dispatcher, "Global dispatcher should not be null");
    }
}