package com.dua3.utility.concurrent;

/**
 * Interface for progress tracking.
 *
 * @param <T> the task type
 */
public interface ProgressTracker<T> {
    /**
     * A 'no-operation' ProgressTracker implementation to be used as a dummy.
     *
     * @param <T> the task type
     * @return new dummy ProgressTracker instance
     */
    static <T> ProgressTracker<T> nopTracker() {
        return new ProgressTracker<>() {
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
     * Create a no-op task updater.
     *
     * @return no-op task updater
     */
    static TaskUpdater nopTaskUpdater() {
        return new TaskUpdater(nopTracker(), "none");
    }

    /**
     * Schedule task. If this task already exists, this is a no-op.
     *
     * @param task the task
     */
    void schedule(T task);

    /**
     * Start a task.
     *
     * @param task the task
     */
    void start(T task);

    /**
     * Pause task.
     *
     * @param task the task
     */
    void pause(T task);

    /**
     * Abort task.
     *
     * @param task the task
     */
    void abort(T task);

    /**
     * Mark task as completed.
     *
     * @param task the task
     * @param s    the status
     */
    void finish(T task, State s);

    /**
     * Update task progress.
     *
     * @param task  the task
     * @param total the total amount of work
     * @param done  the work done
     */
    void update(T task, int total, int done);

    /**
     * Update task progress.
     *
     * @param task        the task
     * @param percentDone percentage of the work done (floating point value between 0 and 1)
     */
    void update(T task, double percentDone);

    /**
     * Create task updater.
     *
     * @param task the task
     * @return the task updater
     */
    default TaskUpdater taskUpdater(T task) {
        return TaskUpdater.create(this, task);
    }

    /**
     * Task status.
     */
    enum State {
        /**
         * Indicates that the task is scheduled and waiting to be executed.
         * This is a non-terminal state.
         */
        SCHEDULED(false),
        /**
         * Indicates that the task is currently paused.
         * This is a non-terminal state.
         */
        PAUSED(false),
        /**
         * Represents a running task.
         * Indicates that the task is currently in progress.
         * This is a non-terminal state.
         */
        RUNNING(false),
        /**
         * Represents a task state where the task has completed successfully.
         * This state is terminal, meaning no further transitions should occur
         * from this state.
         */
        COMPLETED_SUCCESS(true),
        /**
         * The state indicating that the task has completed but encountered a failure.
         * This state is terminal, meaning no further transitions should occur
         * from this state.
         */
        COMPLETED_FAILURE(true),
        /**
         * Represents a task that has been aborted.
         * This state is terminal, meaning no further transitions should occur
         * from this state.
         */
        ABORTED(true);

        private final boolean terminal;

        State(boolean terminal) {
            this.terminal = terminal;
        }

        /**
         * Determines if the task is in a terminal state, i.e., no further transitions will occur.
         *
         * @return {@code true} if the task is in a terminal state, {@code false} otherwise.
         */
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

        TaskUpdater(ProgressTracker tracker, Object task) {
            this.tracker = tracker;
            this.task = task;
        }

        static <T> TaskUpdater create(ProgressTracker<T> tracker, T task) {
            return new TaskUpdater(tracker, task);
        }

        /**
         * Mark task as started.
         */
        public void start() {
            tracker.start(task);
        }

        /**
         * Mark task as paused.
         */
        public void pause() {
            tracker.pause(task);
        }

        /**
         * Mark task as aborted.
         */
        public void abort() {
            tracker.abort(task);
        }

        /**
         * Mark task as completed.
         *
         * @param s terminal status of task
         */
        public void finish(State s) {
            tracker.finish(task, s);
        }

        /**
         * Update progress of task. Use update(0,0) for indeterminate progress.
         *
         * @param total the total amount of work
         * @param done  the work done
         */
        public void update(int total, int done) {
            tracker.update(task, total, done);
        }

        /**
         * Update progress of task.
         *
         * @param percentDone percentage of work done
         */
        public void update(double percentDone) {
            tracker.update(task, percentDone);
        }
    }
}
