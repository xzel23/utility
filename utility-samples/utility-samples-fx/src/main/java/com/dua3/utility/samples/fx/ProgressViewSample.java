package com.dua3.utility.samples.fx;

import com.dua3.utility.concurrent.ProgressTracker;
import com.dua3.utility.fx.controls.ProgressView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.slb4j.SLB4J;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Demonstrates the usage of the ProgressView component in a JavaFX application.
 * This class creates a simple UI to monitor the progress of several tasks.
 * It uses an ExecutorService to manage a fixed pool of threads for task execution.
 */
public class ProgressViewSample extends Application {

    static {
        SLB4J.init();
    }

    final ExecutorService pool = Executors.newFixedThreadPool(3,
            Thread.ofVirtual().name("sample-").factory()
    );

    /**
     * The main entry point for the application.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Constructor.
     */
    public ProgressViewSample() { /* nothing to do */ }

    @Override
    public void start(Stage primaryStage) {
        ProgressView<SampleTask> pv = new ProgressView<>();
        pv.setMaxWidth(Double.POSITIVE_INFINITY);
        pv.setPrefWidth(5000);
        pv.setMaxHeight(Double.POSITIVE_INFINITY);

        StackPane root = new StackPane(pv);

        Scene scene = new Scene(root, 300, 250);

        primaryStage.setTitle("ProgressView");
        primaryStage.setScene(scene);
        primaryStage.show();

        SampleTask sampleTask1 = newTask(pv, "SampleTask 1", 100, ProgressTracker.State.COMPLETED_SUCCESS);
        SampleTask sampleTask2 = newTask(pv, "SampleTask 2", 50, ProgressTracker.State.COMPLETED_SUCCESS);
        SampleTask sampleTask3 = newTask(pv, "SampleTask 3", -75, ProgressTracker.State.COMPLETED_SUCCESS);

        pv.scheduleTaskGroup("Sample Tasks", sampleTask1, sampleTask2, sampleTask3);
    }

    @SuppressWarnings("FutureReturnValueIgnored")
    private SampleTask newTask(ProgressTracker<SampleTask> pv, String name, int steps, ProgressTracker.State s) {
        boolean indeterminate = steps < 0;
        int max = steps >= 0 ? steps : -steps;

        SampleTask task = new SampleTask(name, indeterminate, max);

        pool.submit(() -> {
            sleep(500);
            pv.start(task);
            sleep(500);
            for (int i = 0; i <= max; i++) {
                if (!indeterminate) {
                    pv.update(task, max, i);
                }
                sleep(100);
            }
            pv.finish(task, s);
        });

        return task;
    }

    private static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    record SampleTask(String name, boolean indeterminate, int max) {
        @Override
        public String toString() {
            return name;
        }
    }
}

