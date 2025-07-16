package com.dua3.utility.swing;

import com.dua3.utility.concurrent.ProgressTracker;
import org.junit.jupiter.api.Test;

import javax.swing.JLabel;
import javax.swing.JProgressBar;
import java.awt.GraphicsEnvironment;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/**
 * Tests for the SwingProgressView class.
 */
class SwingProgressViewTest {

    @Test
    void testConstructor() {
        // Skip test in headless environment
        assumeFalse(GraphicsEnvironment.isHeadless());

        SwingProgressView<String> view = new SwingProgressView<>();
        assertNotNull(view, "SwingProgressView should be created successfully");
    }

    @Test
    void testScheduleTask() {
        // Skip test in headless environment
        assumeFalse(GraphicsEnvironment.isHeadless());

        SwingProgressView<String> view = new SwingProgressView<>();
        String task = "Test Task";

        // Schedule the task
        view.schedule(task);

        // Wait a bit for the UI to update
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Check if the task label was added
        List<JLabel> labels = SwingTestUtil.findComponentsOfType(view, JLabel.class);
        boolean foundTaskLabel = false;
        for (JLabel label : labels) {
            if (task.equals(label.getText())) {
                foundTaskLabel = true;
                break;
            }
        }

        assertTrue(foundTaskLabel, "Task label should be added to the view");
    }

    @Test
    void testUpdateTask() {
        // Skip test in headless environment
        assumeFalse(GraphicsEnvironment.isHeadless());

        SwingProgressView<String> view = new SwingProgressView<>();
        String task = "Test Task";

        // Schedule and start the task
        view.schedule(task);
        view.start(task);

        // Update the task progress
        view.update(task, 100, 50);

        // Wait a bit for the UI to update
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Check if the progress bar was updated
        List<JProgressBar> progressBars = SwingTestUtil.findComponentsOfType(view, JProgressBar.class);
        assertFalse(progressBars.isEmpty(), "Progress bar should be added to the view");

        // The first progress bar should be for our task
        JProgressBar progressBar = progressBars.get(0);
        assertEquals(100, progressBar.getMaximum(), "Progress bar maximum should be 100");
        assertEquals(50, progressBar.getValue(), "Progress bar value should be 50");
    }

    @Test
    void testUpdateTaskWithPercentage() {
        // Skip test in headless environment
        assumeFalse(GraphicsEnvironment.isHeadless());

        SwingProgressView<String> view = new SwingProgressView<>();
        String task = "Test Task";

        // Schedule and start the task
        view.schedule(task);
        view.start(task);

        // Update the task progress with percentage
        view.update(task, 0.75);

        // Wait a bit for the UI to update
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Check if the progress bar was updated
        List<JProgressBar> progressBars = SwingTestUtil.findComponentsOfType(view, JProgressBar.class);
        assertFalse(progressBars.isEmpty(), "Progress bar should be added to the view");

        // The first progress bar should be for our task
        JProgressBar progressBar = progressBars.get(0);
        assertEquals(1000, progressBar.getMaximum(), "Progress bar maximum should be 1000");
        assertEquals(750, progressBar.getValue(), "Progress bar value should be 750 (75%)");
    }

    @Test
    void testFinishTask() {
        // Skip test in headless environment
        assumeFalse(GraphicsEnvironment.isHeadless());

        SwingProgressView<String> view = new SwingProgressView<>();
        String task = "Test Task";

        // Schedule, start, and finish the task
        view.schedule(task);
        view.start(task);
        view.finish(task, ProgressTracker.State.COMPLETED_SUCCESS);

        // Wait a bit for the UI to update
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Check if the progress bar was updated to 100%
        List<JProgressBar> progressBars = SwingTestUtil.findComponentsOfType(view, JProgressBar.class);
        assertFalse(progressBars.isEmpty(), "Progress bar should be added to the view");

        // The first progress bar should be for our task
        JProgressBar progressBar = progressBars.get(0);
        assertEquals(progressBar.getMaximum(), progressBar.getValue(), "Progress bar value should be at maximum after task completion");
    }
}
