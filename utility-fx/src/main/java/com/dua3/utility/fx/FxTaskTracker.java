package com.dua3.utility.fx;

import javafx.concurrent.Task;
import javafx.concurrent.Worker.State;

/**
 * Interface for tracking and updating the state, progress, title, and message of a task.
 */
public interface FxTaskTracker {
    /**
     * Updates the state of the specified task.
     *
     * @param task the task whose state is to be updated
     * @param state the new state to be set for the task
     * @see com.dua3.utility.concurrent.ProgressTracker.State
     */
    void updateTaskState(Task<?> task, State state);

    /**
     * Updates the progress of a given task.
     *
     * @param task The task for which the progress is being updated.
     * @param progress The current progress of the task, typically a value between 0.0 and 1.0.
     */
    void updateTaskProgress(Task<?> task, double progress);

    /**
     * Updates the title of a given task.
     *
     * @param task the task whose title needs to be updated
     * @param title the new title to set for the task
     */
    void updateTaskTitle(Task<?> task, String title);

    /**
     * Updates the message associated with a specified task.
     *
     * @param task the task whose message is to be updated
     * @param message the new message to set for the task
     */
    void updateTaskMessage(Task<?> task, String message);
}
