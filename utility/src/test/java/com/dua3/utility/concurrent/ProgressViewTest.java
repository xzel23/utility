package com.dua3.utility.concurrent;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.util.ArrayList;
import java.util.List;

import static com.dua3.utility.concurrent.ProgressView.PROGRESS_INDETERMINATE;
import static org.junit.jupiter.api.Assertions.*;
import static com.dua3.utility.concurrent.ProgressTracker.State;

class ProgressViewTest {

    private ProgressView<String> progressView;
    private MockProgressIndicator mockIndicator;
    private List<MockProgressIndicator> createdIndicators;

    @BeforeEach
    void setUp() {
        createdIndicators = new ArrayList<>();
        progressView = new ProgressView<>(task -> {
            mockIndicator = new MockProgressIndicator();
            createdIndicators.add(mockIndicator);
            return mockIndicator;
        });
    }

    @Test
    void testScheduleTask() {
        // Schedule a task
        progressView.schedule("task1");

        // Verify that a progress indicator was created
        assertEquals(1, createdIndicators.size());

        // Schedule the same task again, should not create a new indicator
        progressView.schedule("task1");
        assertEquals(1, createdIndicators.size());

        // Schedule a different task
        progressView.schedule("task2");
        assertEquals(2, createdIndicators.size());
    }

    @Test
    void testAddTasks() {
        // Add multiple tasks at once
        progressView.addTasks("task1", "task2", "task3");

        // Verify that progress indicators were created for each task
        assertEquals(3, createdIndicators.size());
    }

    @Test
    void testTaskLifecycle() {
        // Schedule and start a task
        progressView.schedule("task1");
        progressView.start("task1");

        // Verify that the task was started and progress was updated
        assertEquals(1, mockIndicator.updateCalls);

        // Update progress
        progressView.update("task1", 100, 10);
        assertEquals(2, mockIndicator.updateCalls);
        assertEquals(10, mockIndicator.lastDone);
        assertEquals(100, mockIndicator.lastTotal);

        // Update progress with percentage
        progressView.update("task1", 0.5);
        assertEquals(3, mockIndicator.updateCalls);
        assertEquals(0.5, mockIndicator.lastPercentDone);

        // Finish the task
        progressView.finish("task1", State.COMPLETED_SUCCESS);
        assertEquals(State.COMPLETED_SUCCESS, mockIndicator.lastState);
        assertTrue(mockIndicator.finishCalled);
    }

    @Test
    void testPauseTask() {
        // Schedule a task
        progressView.schedule("task1");

        // Pause the task
        progressView.pause("task1");
        assertEquals(State.PAUSED, mockIndicator.lastState);
    }

    @Test
    void testAbortTask() {
        // Schedule and start a task
        progressView.schedule("task1");
        progressView.start("task1");

        // Abort the task
        progressView.abort("task1");
        assertEquals(State.ABORTED, mockIndicator.lastState);
    }

    @Test
    void testFinishWithNonTerminalState() {
        // Schedule a task
        progressView.schedule("task1");

        // Trying to finish with a non-terminal state should throw an exception
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            progressView.finish("task1", State.SCHEDULED);
        });
        assertTrue(exception.getMessage().contains("not a terminal state"));
    }

    @Test
    void testFinishAlreadyTerminatedTask() {
        // Schedule and finish a task
        progressView.schedule("task1");
        progressView.finish("task1", State.COMPLETED_SUCCESS);

        assertTrue(mockIndicator.finishCalled);
        assertEquals(State.COMPLETED_SUCCESS, mockIndicator.lastState);
    }

    @Test
    void testIndeterminateProgress() {
        // Schedule and start a task
        progressView.schedule("task1");
        progressView.start("task1");

        progressView.update("task1", PROGRESS_INDETERMINATE);
        assertEquals(PROGRESS_INDETERMINATE, mockIndicator.lastPercentDone);
    }

    /**
     * A mock implementation of ProgressIndicator that tracks method calls
     */
    private static class MockProgressIndicator implements ProgressView.ProgressIndicator {
        int updateCalls = 0;
        int lastDone = 0;
        int lastTotal = 0;
        double lastPercentDone = 0.0;
        State lastState = null;
        boolean finishCalled = false;

        @Override
        public void finish(State s) {
            finishCalled = true;
            lastState = s;
        }

        @Override
        public void pause() {
            lastState = State.PAUSED;
        }

        @Override
        public void update(int total, int done) {
            updateCalls++;
            lastDone = done;
            lastTotal = total;
        }

        @Override
        public void update(double percentDone) {
            updateCalls++;
            lastPercentDone = percentDone;
        }
    }
}
