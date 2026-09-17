package com.dua3.utility.concurrent;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProgressTrackerTest {

    @ParameterizedTest
    @EnumSource(ProgressTracker.State.class)
    void testStateTerminal(ProgressTracker.State state) {
        assertNotNull(state);
        switch (state) {
            case SCHEDULED, PAUSED, RUNNING -> assertFalse(state.isTerminal());
            case COMPLETED_SUCCESS, COMPLETED_FAILURE, ABORTED -> assertTrue(state.isTerminal());
        }
    }

    @Test
    void testNopTracker() {
        ProgressTracker<String> tracker = ProgressTracker.nopTracker();
        assertNotNull(tracker);

        assertDoesNotThrow(() -> {
            tracker.schedule("task1");
            tracker.start("task1");
            tracker.pause("task1");
            tracker.abort("task1");
            tracker.finish("task1", ProgressTracker.State.COMPLETED_SUCCESS);
            tracker.update("task1", 100, 50);
            tracker.update("task1", 0.5);
            tracker.scheduleTaskGroup("grp", "t1", "t2");
        });
    }

    @Test
    void testNopTaskUpdater() {
        ProgressTracker.TaskUpdater updater = ProgressTracker.nopTaskUpdater();
        assertNotNull(updater);

        assertDoesNotThrow(() -> {
            updater.start();
            updater.pause();
            updater.abort();
            updater.finish(ProgressTracker.State.COMPLETED_SUCCESS);
            updater.update(100, 50);
            updater.update(0.5);
        });
    }

    @SuppressWarnings("java:S1186")
    @Test
    void testScheduleTaskGroup() {
        List<String> scheduled = new ArrayList<>();
        ProgressTracker<String> tracker = new ProgressTracker<>() {
            @Override
            public void schedule(String task) {
                scheduled.add(task);
            }

            @Override
            public void start(String task) {}

            @Override
            public void pause(String task) {}

            @Override
            public void abort(String task) {}

            @Override
            public void finish(String task, State s) {}

            @Override
            public void update(String task, int total, int done) {}

            @Override
            public void update(String task, double percentDone) {}
        };

        tracker.scheduleTaskGroup("groupA", "task1", "task2", "task3");
        assertEquals(List.of("task1", "task2", "task3"), scheduled);
    }

    @Test
    void testTaskUpdaterDelegation() {
        List<String> events = new ArrayList<>();
        ProgressTracker<String> tracker = new ProgressTracker<>() {
            @Override
            public void schedule(String task) {
                events.add("schedule:" + task);
            }

            @Override
            public void start(String task) {
                events.add("start:" + task);
            }

            @Override
            public void pause(String task) {
                events.add("pause:" + task);
            }

            @Override
            public void abort(String task) {
                events.add("abort:" + task);
            }

            @Override
            public void finish(String task, State s) {
                events.add("finish:" + task + ":" + s);
            }

            @Override
            public void update(String task, int total, int done) {
                events.add("updateInt:" + task + ":" + done + "/" + total);
            }

            @Override
            public void update(String task, double percentDone) {
                events.add("updateDouble:" + task + ":" + percentDone);
            }
        };

        ProgressTracker.TaskUpdater updater = tracker.taskUpdater("myTask");
        updater.start();
        updater.update(100, 20);
        updater.update(0.2);
        updater.pause();
        updater.abort();
        updater.finish(ProgressTracker.State.COMPLETED_SUCCESS);

        assertEquals(List.of(
                "start:myTask",
                "updateInt:myTask:20/100",
                "updateDouble:myTask:0.2",
                "pause:myTask",
                "abort:myTask",
                "finish:myTask:COMPLETED_SUCCESS"
        ), events);
    }
}
