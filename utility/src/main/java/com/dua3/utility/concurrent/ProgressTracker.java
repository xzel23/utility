package com.dua3.utility.concurrent;

import java.util.Objects;

/**
 * Interface for progress tracking.
 * @param <T> the task type
 */
public interface ProgressTracker<T> {
    /**
     * Task status.
     */
    enum State {
        SCHEDLULED(false),
        PAUSED(false),
        RUNNING(false),
        COMPLETED_SUCCESS(true),
        COMPLETED_FAILURE(true),
        ABORTED(true);
        
        private final boolean terminal;
        
        State(boolean terminal) {
            this.terminal = terminal;
        }
        
        public boolean isTerminal() {
            return terminal;
        }
    }

    /**
     * Task Updater.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    class TaskUpdater {
        private final ProgressTracker tracker;
        private final Object task;

        static <T> TaskUpdater create(ProgressTracker<T> tracker, T task) {
            return new TaskUpdater(tracker, task);
        }
        
        TaskUpdater(ProgressTracker tracker, Object task) {
            this.tracker = Objects.requireNonNull(tracker);
            this.task = Objects.requireNonNull(task);
        }

        /**
         * Mark task as started.
         */
        public void start() { tracker.start(task); }

        /**
         * Mark task as paused.
         */
        public void pause() { tracker.pause(task); }

        /**
         * Mark task as aborted.
         */
        public void abort() { tracker.abort(task); }

        /**
         * Mark task as completed.
         * @param s terminal status of task
         */
        public void finish(State s) { tracker.finish(task, s); }

        /**
         * Update progress of task.
         * @param total the total amount of work
         * @param done the work done
         */
        public void update(int total, int done) { tracker.update(task, total, done); }

        /**
         * Update progress of task.
         * @param percentDone percentage of work done
         */
        public void update(double percentDone) { tracker.update(task, percentDone); }
    }
    
    static <T> ProgressTracker<T> nopTracker() {
        return new ProgressTracker<T>() {
            @Override
            public void schedule(T task) { /* nop */ }

            @Override
            public void start(T task) { /* nop */ }

            @Override
            public void pause(T task) { /* nop */ }

            @Override
            public void abort(T task) { /* nop */ }

            @Override
            public void finish(T task, State s) { /* nop */ }

            @Override
            public void update(T task, int total, int done) { /* nop */ }

            @Override
            public void update(T task, double percentDone) { /* nop */ }
        };
    }

    /**
     * Schedule task. If this task already exists, this is a no-op.
     * @param task the task
     */
    void schedule(T task);

    /**
     * Start a task.
     * @param task the task
     */
    void start(T task);

    /**
     * Pause task.
     * @param task the task
     */
    void pause(T task);

    /**
     * Abort task.
     * @param task the task
     */
    void abort(T task);

    /**
     * Mark task as completed.
     * @param task the task
     * @param s the status
     */
    void finish(T task, State s);

    /**
     * Update task progress.
     * @param task the task
     * @param total the total amount of work
     * @param done the work done
     */
    void update(T task, int total, int done);

    /**
     * Update task progress.
     * @param task the task
     * @param percentDone percentage of the work done (floating point value between 0 and 1)
     */
    void update(T task, double percentDone);

    /**
     * Create taskupdater.
     * @param task the task
     * @return the task updater
     */
    default TaskUpdater taskUpdater(T task) {
        return TaskUpdater.create(this, task);
    }

    /**
     * Create a no-op taskupdater.
     * @return no-op task updater
     */
    static TaskUpdater nopTaskUpdater() {
        return new TaskUpdater(nopTracker(), "none");
    }
}
