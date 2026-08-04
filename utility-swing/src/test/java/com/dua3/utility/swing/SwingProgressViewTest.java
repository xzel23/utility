package com.dua3.utility.swing;

import com.dua3.utility.concurrent.ProgressTracker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import javax.swing.JLabel;
import javax.swing.JProgressBar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the SwingProgressView class.
 */
@Timeout(value = 10, unit = TimeUnit.SECONDS)
class SwingProgressViewTest {

    @Test
    void testConstructor() {
        SwingProgressView<String> view = SwingTestUtil.getOnEdt(SwingProgressView::new);
        assertNotNull(view, "SwingProgressView should be created successfully");
    }

    @Test
    void testScheduleTask() {
        SwingProgressView<String> view = SwingTestUtil.getOnEdt(SwingProgressView::new);
        String task = "Test Task";

        // Schedule the task
        view.schedule(task);

        // This action runs after the update queued by schedule().
        boolean foundTaskLabel = SwingTestUtil.getOnEdt(() -> {
            List<JLabel> labels = SwingTestUtil.findComponentsOfType(view, JLabel.class);
            return labels.stream().anyMatch(label -> task.equals(label.getText()));
        });

        assertTrue(foundTaskLabel, "Task label should be added to the view");
    }

    @Test
    void testUpdateTask() {
        SwingProgressView<String> view = SwingTestUtil.getOnEdt(SwingProgressView::new);
        String task = "Test Task";

        // Schedule and start the task
        view.schedule(task);
        view.start(task);

        // Update the task progress
        view.update(task, 100, 50);

        SwingTestUtil.runOnEdt(() -> {
            List<JProgressBar> progressBars = SwingTestUtil.findComponentsOfType(view, JProgressBar.class);
            assertFalse(progressBars.isEmpty(), "Progress bar should be added to the view");

            JProgressBar progressBar = progressBars.getFirst();
            assertEquals(100, progressBar.getMaximum(), "Progress bar maximum should be 100");
            assertEquals(50, progressBar.getValue(), "Progress bar value should be 50");
        });
    }

    @Test
    void testUpdateTaskWithPercentage() {
        SwingProgressView<String> view = SwingTestUtil.getOnEdt(SwingProgressView::new);
        String task = "Test Task";

        // Schedule and start the task
        view.schedule(task);
        view.start(task);

        // Update the task progress with percentage
        view.update(task, 0.75);

        SwingTestUtil.runOnEdt(() -> {
            List<JProgressBar> progressBars = SwingTestUtil.findComponentsOfType(view, JProgressBar.class);
            assertFalse(progressBars.isEmpty(), "Progress bar should be added to the view");

            JProgressBar progressBar = progressBars.getFirst();
            assertEquals(1000, progressBar.getMaximum(), "Progress bar maximum should be 1000");
            assertEquals(750, progressBar.getValue(), "Progress bar value should be 750 (75%)");
        });
    }

    @Test
    void testFinishTask() {
        SwingProgressView<String> view = SwingTestUtil.getOnEdt(SwingProgressView::new);
        String task = "Test Task";

        // Schedule, start, and finish the task
        view.schedule(task);
        view.start(task);
        view.finish(task, ProgressTracker.State.COMPLETED_SUCCESS);

        SwingTestUtil.runOnEdt(() -> {
            List<JProgressBar> progressBars = SwingTestUtil.findComponentsOfType(view, JProgressBar.class);
            assertFalse(progressBars.isEmpty(), "Progress bar should be added to the view");

            JProgressBar progressBar = progressBars.getFirst();
            assertEquals(progressBar.getMaximum(), progressBar.getValue(), "Progress bar value should be at maximum after task completion");
        });
    }
}
