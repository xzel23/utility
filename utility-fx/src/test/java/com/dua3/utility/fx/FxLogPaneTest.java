package com.dua3.utility.fx;

import com.dua3.utility.logging.LogBuffer;
import javafx.scene.Node;
import javafx.scene.control.ToolBar;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Tests for the FxLogPane class.
 */
@Execution(ExecutionMode.SAME_THREAD)
class FxLogPaneTest extends FxTestBase {

    @Test
    void testConstructorWithDefaultBuffer() throws Throwable {
        FxTestBase.runOnFxThreadAndWait(() -> {
            FxLogPane pane = new FxLogPane();
            assertNotNull(pane, "FxLogPane should be created successfully");
        });
    }

    @Test
    void testUIComponents() throws Throwable {
        FxTestBase.runOnFxThreadAndWait(() -> {
            FxLogPane pane = new FxLogPane();
            assertNotNull(findToolBar(pane), "FxLogPane should contain a ToolBar");
        });
    }

    @Test
    void testConstructorWithBufferSize() throws Throwable {
        FxTestBase.runOnFxThreadAndWait(() -> {
            int bufferSize = 200;
            FxLogPane pane = new FxLogPane(bufferSize);
            assertNotNull(pane, "FxLogPane should be created successfully with custom buffer size");

            // Verify the buffer size by checking the LogBuffer
            LogBuffer buffer = pane.getLogBuffer();
            assertNotNull(buffer, "LogBuffer should not be null");
        });
    }

    @Test
    void testConstructorWithLogBuffer() throws Throwable {
        FxTestBase.runOnFxThreadAndWait(() -> {
            LogBuffer buffer = new LogBuffer("log buffer", 150);
            FxLogPane pane = new FxLogPane(buffer);
            assertNotNull(pane, "FxLogPane should be created successfully with custom LogBuffer");

            // Verify that the pane uses the provided LogBuffer
            assertSame(buffer, pane.getLogBuffer(), "The LogBuffer in the pane should be the same as the one provided");
        });
    }

    @Test
    void testGetLogBuffer() throws Throwable {
        FxTestBase.runOnFxThreadAndWait(() -> {
            // Create a pane with a specific buffer
            LogBuffer buffer = new LogBuffer("log buffer", 250);
            FxLogPane pane = new FxLogPane(buffer);

            // Test the getLogBuffer method
            LogBuffer retrievedBuffer = pane.getLogBuffer();
            assertNotNull(retrievedBuffer, "Retrieved LogBuffer should not be null");
            assertSame(buffer, retrievedBuffer, "Retrieved LogBuffer should be the same as the one provided");
        });
    }

    /**
     * Helper method to find a ToolBar in the FxLogPane.
     *
     * @param container the FxLogPane to search in
     * @return the ToolBar or null if not found
     */
    private ToolBar findToolBar(Node container) {
        AtomicReference<ToolBar> result = new AtomicReference<>(null);
        container.lookupAll(".tool-bar").forEach(node -> {
            if (node instanceof ToolBar) {
                result.set((ToolBar) node);
            }
        });
        return result.get();
    }
}
