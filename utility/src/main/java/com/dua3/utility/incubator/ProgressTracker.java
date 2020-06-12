package com.dua3.utility.incubator;

/**
 * Interface for progress tracking.
 * @param <T> the task type
 */
public interface ProgressTracker<T> {
    enum Status {
        SCHEDLULED(false),
        PAUSED(false),
        RUNNING(false),
        COMPLETED_SUCCESS(true),
        COMPLETED_FAILURE(true),
        ABORTED(true);
        
        private final boolean terminal;
        
        Status(boolean terminal) {
            this.terminal = terminal;
        }
        
        public boolean isTerminal() {
            return terminal;
        }
    }

    /**
     * Start a new task.
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
    void finish(T task, Status s);

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
}
