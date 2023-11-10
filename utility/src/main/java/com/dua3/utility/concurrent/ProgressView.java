package com.dua3.utility.concurrent;

import com.dua3.utility.lang.LangUtil;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * View of task progress.
 * This class implements the {@link ProgressTracker} interface.
 *
 * @param <T> the type of the tasks being tracked
 */
public class ProgressView<T> implements ProgressTracker<T> {

    /**
     * Value to pass to {@link ProgressIndicator#update(double)} for an indeterminate progress.
     */
    public static final double PROGRESS_INDETERMINATE = Double.NaN;
    private final Function<T, ProgressIndicator> createProgressIndicator;
    private final Map<T, TaskRecord> tasks = Collections.synchronizedMap(new LinkedHashMap<>());

    /**
     * Constructs a new ProgressView object.
     *
     * @param createProgressIndicator a function that creates a ProgressIndicator based on a given task type T.
     *                                The function must not return null.
     */
    public ProgressView(Function<T, ProgressIndicator> createProgressIndicator) {
        this.createProgressIndicator = Objects.requireNonNull(createProgressIndicator);
    }

    /**
     * Adds multiple tasks to the ProgressView.
     *
     * @param tasks the tasks to be added to the ProgressView. The tasks must be of type T.
     */
    @SafeVarargs
    public final void addTasks(T... tasks) {
        for (T task : tasks) {
            getTaskRecord(task);
        }
    }

    private TaskRecord getTaskRecord(T task) {
        return tasks.computeIfAbsent(task, t -> {
            ProgressIndicator pi = createProgressIndicator.apply(t);
            return new TaskRecord(pi);
        });
    }

    @Override
    public void schedule(T task) {
        // getTaskRecord() will enter an entry for the task if it is not yet present
        getTaskRecord(task);
    }

    @Override
    public void start(T task) {
        TaskRecord r = getTaskRecord(task);
        r.state = State.RUNNING;
        update(task, 0, 0);
    }

    @Override
    public void pause(T task) {
        TaskRecord r = getTaskRecord(task);
        LangUtil.check(r.state == State.SCHEDULED, "task not scheduled: %s (%s)", task, r.state);
        r.state = State.PAUSED;
    }

    @Override
    public void abort(T task) {
        TaskRecord r = getTaskRecord(task);
        LangUtil.check(!r.state.isTerminal(), "task already completed: %s (%s)", task, r.state);
        r.state = State.ABORTED;
    }

    @Override
    public void finish(T task, State s) {
        LangUtil.check(s.isTerminal(), "not a terminal state: %s", s);

        TaskRecord r = getTaskRecord(task);
        LangUtil.check(!r.state.isTerminal(), "task already terminated: %s (%s)", task, r.state);

        r.finish(s);
    }

    @Override
    public void update(T task, int total, int done) {
        assert 0 <= done && done <= total : "invalid arguments for '" + task + "': done=" + done + ", total=" + total;
        getTaskRecord(task).update(done, total);
    }

    @Override
    public void update(T task, double percentDone) {
        assert 0 <= percentDone && percentDone <= 1.0 : "invalid arguments for '" + task + "': percentDone=" + percentDone;
        getTaskRecord(task).update(percentDone);
    }

    /**
     * Indicator for a single task's progress.
     */
    public interface ProgressIndicator {
        /**
         * Mark task as finished
         *
         * @param s the {@link com.dua3.utility.concurrent.ProgressTracker.State} to set
         */
        void finish(State s);

        /**
         * Update task progress.
         *
         * @param done  number of finished steps
         * @param total total number of steps
         */
        void update(int done, int total);

        /**
         * Update task progress.
         *
         * @param percentDone percentage value, 0.0 &le; percentDone &le; 1.0 or use PROGRESS_INDETERMINATE to mark as indeterminate
         */
        void update(double percentDone);
    }

    private static class TaskRecord {
        private final ProgressIndicator progressIndicator;
        State state = State.SCHEDULED;

        TaskRecord(ProgressIndicator progressIndicator) {
            this.progressIndicator = Objects.requireNonNull(progressIndicator);
        }

        public void update(int done, int total) {
            progressIndicator.update(done, total);
        }

        public void finish(State s) {
            progressIndicator.finish(s);
        }

        public void update(double percentDone) {
            progressIndicator.update(percentDone);
        }
    }

}
