package com.dua3.utility.fx;

import javafx.concurrent.Task;
import javafx.concurrent.Worker.State;

public interface FxTaskTracker {
    void updateTaskState(Task<?> task, State state);

    void updateTaskProgress(Task<?> task, double progress);

    void updateTaskTitle(Task<?> task, String title);

    void updateTaskMessage(Task<?> task, String message);
}
