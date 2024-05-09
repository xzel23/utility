package com.dua3.utility.fx;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.util.ArrayList;
import java.util.List;

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

    public void addTaskTracker(FxTaskTracker t) {
        taskTrackers.add(t);
    }

    public void removeTaskTracker(FxTaskTracker t) {
        taskTrackers.remove(t);
    }

}
