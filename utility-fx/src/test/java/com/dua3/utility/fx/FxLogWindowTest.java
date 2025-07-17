package com.dua3.utility.fx;

import com.dua3.utility.logging.LogBuffer;
import javafx.scene.Scene;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests for the FxLogWindow class.
 */
@Execution(ExecutionMode.SAME_THREAD)
class FxLogWindowTest extends FxTestBase {

    @Test
    void testConstructorWithDefaultBuffer() throws Throwable {
        FxTestBase.runOnFxThreadAndWait(() -> {
            FxLogWindow window = new FxLogWindow();
            assertNotNull(window, "FxLogWindow should be created successfully");

            // Check if the window has a scene
            Scene scene = window.getScene();
            assertNotNull(scene, "FxLogWindow should have a Scene");

            // Check if the scene's root is a FxLogPane
            assertNotNull(scene.getRoot(), "Scene should have a root node");
            assertSame(FxLogPane.class, scene.getRoot().getClass(), "Scene's root should be a FxLogPane");
        });
    }

    @Test
    void testConstructorWithTitle() throws Throwable {
        FxTestBase.runOnFxThreadAndWait(() -> {
            String title = "Test Log Window";
            FxLogWindow window = new FxLogWindow(title);
            assertNotNull(window, "FxLogWindow should be created successfully");
            assertEquals(title, window.getTitle(), "Window title should match the provided title");
        });
    }

    @Test
    void testConstructorWithMaxLines() throws Throwable {
        FxTestBase.runOnFxThreadAndWait(() -> {
            int maxLines = 100;
            FxLogWindow window = new FxLogWindow(maxLines);
            assertNotNull(window, "FxLogWindow should be created successfully");
        });
    }

    @Test
    void testConstructorWithTitleAndMaxLines() throws Throwable {
        FxTestBase.runOnFxThreadAndWait(() -> {
            String title = "Test Log Window";
            int maxLines = 100;
            FxLogWindow window = new FxLogWindow(title, maxLines);
            assertNotNull(window, "FxLogWindow should be created successfully");
            assertEquals(title, window.getTitle(), "Window title should match the provided title");
        });
    }

    @Test
    void testConstructorWithLogBuffer() throws Throwable {
        FxTestBase.runOnFxThreadAndWait(() -> {
            LogBuffer buffer = new LogBuffer();
            FxLogWindow window = new FxLogWindow(buffer);
            assertNotNull(window, "FxLogWindow should be created successfully");
            assertSame(buffer, window.getLogBuffer(), "LogBuffer should be the same instance that was passed in");
        });
    }

    @Test
    void testGetLogBuffer() throws Throwable {
        FxTestBase.runOnFxThreadAndWait(() -> {
            LogBuffer buffer = new LogBuffer();
            FxLogWindow window = new FxLogWindow(buffer);
            LogBuffer retrievedBuffer = window.getLogBuffer();
            assertNotNull(retrievedBuffer, "Retrieved LogBuffer should not be null");
            assertSame(buffer, retrievedBuffer, "Retrieved LogBuffer should be the same as the one used to construct the window");
        });
    }
}