package com.dua3.utility.incubator;

/**
 * Interface for progress tracking.
 * @param <T> the task type
 */
public interface ProgressTracker<T> {
    /**
     * Task status.
     */
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
    
    static ProgressTracker nopTracker() {
        return new ProgressTracker() {
            @Override
            public void start(Object task) { /* nop */ }

            @Override
            public void pause(Object task) { /* nop */ }

            @Override
            public void abort(Object task) { /* nop */ }

            @Override
            public void finish(Object task, Status s) { /* nop */ }

            @Override
            public void update(Object task, int total, int done) { /* nop */ }

            @Override
            public void update(Object task, double percentDone) { /* nop */ }
        };
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
