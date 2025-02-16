package com.dua3.utility.fx;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract class that extends the JavaFX Service class to manage tasks with progress, state, and title trackers.
 * Allows for the registration and removal of task trackers that implement the FxTaskTracker interface.
 *
 * @param <T> The result type of the service.
 */
public abstract class FxService<T> extends Service<T> {

    private final List<FxTaskTracker> taskTrackers = new ArrayList<>();

    protected FxService() {
    }

    @Override
    protected final Task<T> createTask() {
        Task<T> task = doCreateTask();
        task.progressProperty().addListener((v, o, n) -> updateTaskProgress(task, n.doubleValue()));
        task.stateProperty().addListener((v, o, n) -> updateTaskState(task, n));
        task.titleProperty().addListener((v, o, n) -> updateTaskTitle(task, n));
        return task;
    }

    /**
     * Creates and returns a custom task to be executed by the FxService.
     * This method must be implemented by subclasses to define the specific behavior
     * and logic of the task being performed by the service.
     *
     * @return a Task instance of type T representing the operation to be performed
     */
    protected abstract Task<T> doCreateTask();

    private void updateTaskProgress(Task<T> task, double arg) {
        taskTrackers.forEach(t -> t.updateTaskProgress(task, arg));
    }

    private void updateTaskState(Task<T> task, State arg) {
        taskTrackers.forEach(t -> t.updateTaskState(task, arg));
    }

    private void updateTaskTitle(Task<T> task, String arg) {
        taskTrackers.forEach(t -> t.updateTaskTitle(task, arg));
    }

    /**
     * Adds an FxTaskTracker to the list of task trackers. The added tracker will receive updates
     * about task progress, state, and title.
     *
     * @param t the FxTaskTracker to be added to the list of task trackers
     */
    public void addTaskTracker(FxTaskTracker t) {
        taskTrackers.add(t);
    }

    /**
     * Removes an FxTaskTracker from the list of task trackers. The removed tracker will no longer receive updates
     * about task progress, state, and title.
     *
     * @param t the FxTaskTracker to be removed from the list of task trackers
     */
    public void removeTaskTracker(FxTaskTracker t) {
        taskTrackers.remove(t);
    }

}
