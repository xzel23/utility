package com.dua3.utility.fx.controls;

import com.dua3.utility.concurrent.ProgressTracker;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

/**
 * ProgressView is a UI component that displays the progress of tasks in a grid layout.
 * It extends GridPane and implements the ProgressTracker interface.
 *
 * @param <T> the type of the tasks being tracked
 */
public final class ProgressView<T> extends GridPane implements ProgressTracker<T> {

    private final com.dua3.utility.concurrent.ProgressView<T> imp;

    /**
     * Constructs a new ProgressView object.
     * This constructor sets up the necessary column constraints for the grid layout of the progress view
     * and initializes the underlying progress view mechanism.
     */
    public ProgressView() {
        setHgap(8);

        ColumnConstraints column1 = new ColumnConstraints();
        column1.setHgrow(Priority.SOMETIMES);
        ColumnConstraints column2 = new ColumnConstraints();
        column2.setHgrow(Priority.ALWAYS);
        getColumnConstraints().addAll(column1, column2); // first column gets any extra width

        this.imp = new com.dua3.utility.concurrent.ProgressView<>(this::createProgressIndicator);
    }

    private com.dua3.utility.concurrent.ProgressView.ProgressIndicator createProgressIndicator(T t) {
        FxProgressBarIndicator pi = new FxProgressBarIndicator();

        int row = getChildren().size();
        Label label = new Label(t.toString());
        setConstraints(label, 0, row);
        ProgressBar pb = pi.pb;
        pb.setMaxWidth(Double.POSITIVE_INFINITY);
        setConstraints(pb, 1, row);
        getChildren().addAll(label, pb);

        return pi;
    }

    @Override
    public void schedule(T task) {
        imp.schedule(task);
    }

    @Override
    @SafeVarargs
    public final void scheduleTaskGroup(String group, T... tasks) {
        int row = getChildren().size();
        Label label = new Label(group);
        label.setStyle("-fx-font-weight: bold; -fx-font-size: 1.2em;");
        setConstraints(label, 0, row, 2, 1);
        getChildren().addAll(label);

        ProgressTracker.super.scheduleTaskGroup(group, tasks);
    }

    @Override
    public void start(T task) {
        imp.update(task, 0, 0);
    }

    @Override
    public void pause(T task) {
        imp.pause(task);
    }

    @Override
    public void abort(T task) {
        imp.abort(task);
    }

    @Override
    public void finish(T task, State s) {
        imp.finish(task, s);
    }

    @Override
    public void update(T task, int total, int done) {
        imp.update(task, total, done);
    }

    @Override
    public void update(T task, double percentDone) {
        imp.update(task, percentDone);
    }

    private static class FxProgressBarIndicator implements com.dua3.utility.concurrent.ProgressView.ProgressIndicator {

        private final ProgressBar pb;

        FxProgressBarIndicator() {
            this.pb = new ProgressBar();
        }

        @Override
        public void finish(State s) {
            Platform.runLater(() -> pb.setProgress(1));
        }

        @Override
        public void update(int total, int done) {
            Platform.runLater(() -> {
                if (total == 0) {
                    pb.setProgress(-1);
                } else {
                    pb.setProgress((double) done / total);
                }
            });
        }

        @Override
        public void update(double percentDone) {
            Platform.runLater(() -> pb.setProgress(percentDone));
        }
    }

}
