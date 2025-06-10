package com.dua3.utility.concurrent;

import com.dua3.utility.lang.LangUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * View of task progress.
 * This class implements the {@link ProgressTracker} interface.
 *
 * @param <T> the type of the tasks being tracked
 */
public class ProgressView<T> implements ProgressTracker<T> {
    private static final Logger LOG = LogManager.getLogger(ProgressView.class);

    /**
     * Value to pass to {@link ProgressIndicator#update(double)} for an indeterminate progress.
     */
    public static final double PROGRESS_INDETERMINATE = Double.NaN;

    private final Function<? super T, ? extends ProgressIndicator> createProgressIndicator;
    private final Map<T, TaskRecord> tasks = Collections.synchronizedMap(new LinkedHashMap<>());

    /**
     * Constructs a new ProgressView object.
     *
     * @param createProgressIndicator a function that creates a ProgressIndicator based on a given task type T.
     *                                The function must not return null.
     */
    public ProgressView(Function<? super T, ? extends ProgressIndicator> createProgressIndicator) {
        this.createProgressIndicator = createProgressIndicator;
    }

    /**
     * Adds multiple tasks to the ProgressView.
     *
     * @param tasks the tasks to be added to the ProgressView. The tasks must be of type T.
     */
    @SafeVarargs
    public final void addTasks(T... tasks) {
        for (T task : tasks) {
            schedule(task);
        }
    }

    private TaskRecord getTaskRecord(T task) {
        return tasks.computeIfAbsent(task, t -> {
            ProgressIndicator pi = createProgressIndicator.apply(t);
            return new TaskRecord(pi);
        });
    }

    /**
     * Determines whether the progress is in an indeterminate state.
     *
     * @param percentDone the progress value to check as a percentage.
     *                    Typically, should be a value between 0 and 100, but
     *                    may also be {@code #PROGRESS_INDETERMINATE} to indicate indeterminate progress.
     * @return {@code true} if the progress is indeterminate,
     *         {@code false} otherwise.
     */
    public static boolean isIndeterminate(double percentDone) {
        return Double.isNaN(percentDone);
    }

    @Override
    public void schedule(T task) {
        // getTaskRecord() will enter an entry for the task if it is not yet present
        State s = getTaskRecord(task).getState();

        if (s != State.SCHEDULED) {
            LOG.warn("task {} already in state {}", task, s);
        } else {
            LOG.debug("scheduled task {}", task);
        }
    }

    @Override
    public void start(T task) {
        TaskRecord r = getTaskRecord(task);
        r.setState(State.RUNNING);
        update(task, 0, 0);
        LOG.debug("started task {}", task);
    }

    @Override
    public void pause(T task) {
        TaskRecord r = getTaskRecord(task);
        State s = r.getState();
        if (s != State.SCHEDULED && s != State.RUNNING) {
            LOG.warn("task {} cannot be paused in state {}", task, s);
        } else {
            r.pause();
            LOG.debug("paused task {}", task);
        }
    }

    @Override
    public void abort(T task) {
        TaskRecord r = getTaskRecord(task);
        State s = r.getState();
        if (r.state.isTerminal()) {
            LOG.warn("task {} cannot be aborted in state {}", task, s);
        } else {
            r.finish(State.ABORTED);
            LOG.debug("aborted task {}", task);
        }
    }

    @Override
    public void finish(T task, State s) {
        LangUtil.check(s.isTerminal(), "not a terminal state: %s", s);

        TaskRecord r = getTaskRecord(task);
        State oldState = r.getState();
        if (oldState.isTerminal()) {
            LOG.warn("task {} already finished with state {}", task, oldState);
        } else {
            r.finish(s);
            LOG.debug("finished task {} with state {}", task, s);
        }
    }

    @Override
    public void update(T task, int total, int done) {
        if (done < 0 || done > total) {
            LOG.warn("invalid arguments for task {}: done={}, total={}", task, done, total);
            done = Math.clamp(done, 0, total);
        }
        getTaskRecord(task).update(total, done);
        LOG.trace("task {} updated: {}/{}", task, done, total);
    }

    @Override
    public void update(T task, double percentDone) {
        boolean indeterminate = isIndeterminate(percentDone);
        if (!indeterminate && !(0 <= percentDone && percentDone <= 1.0)) {
            LOG.warn("invalid argument for task {}: percentDone={}", task, percentDone);
            percentDone = Math.clamp(percentDone, 0.0, 1.0);
        }
        getTaskRecord(task).update(percentDone);
        if (indeterminate) {
            LOG.trace("task {} updated: indeterminate", task);
        } else {
            LOG.trace("task {} updated: {}%%", task, (int) (percentDone * 100));
        }
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
         * Pauses the task. This is a default no-operation implementation, intended
         * to be overridden by concrete implementations if custom behavior is needed.
         */
        default void pause() { /* nothing to do */ }

        /**
         * Update task progress.
         *
         * @param total total number of steps
         * @param done  number of finished steps
         */
        void update(int total, int done);

        /**
         * Update task progress.
         *
         * @param percentDone percentage value, 0.0 &le; percentDone &le; 1.0 or use PROGRESS_INDETERMINATE to mark as indeterminate
         */
        void update(double percentDone);
    }

    private static class TaskRecord {
        private final ProgressIndicator progressIndicator;
        private State state = State.SCHEDULED;

        TaskRecord(ProgressIndicator progressIndicator) {
            this.progressIndicator = progressIndicator;
        }

        public void update(int total, int done) {
            progressIndicator.update(total, done);
        }

        public void pause() {
            setState(State.PAUSED);
            progressIndicator.pause();
        }

        public void finish(State s) {
            setState(s);
            progressIndicator.finish(s);
        }

        public void update(double percentDone) {
            progressIndicator.update(percentDone);
        }

        private void setState(State s) {
            this.state = s;
        }

        public State getState() {
            return state;
        }
    }

}
