package com.dua3.utility.fx;

import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FxServiceTest extends FxTestBase {

    static class SampleService extends FxService<String> {
        @Override
        protected Task<String> doCreateTask() {
            return new Task<>() {
                @Override
                protected String call() {
                    updateTitle("Task Title");
                    updateProgress(50, 100);
                    updateProgress(100, 100);
                    return "Done";
                }
            };
        }
    }

    @Test
    @SuppressWarnings({"java:S4030", "MismatchedQueryAndUpdateOfCollection"})
    void testFxServiceAndTracker() throws Throwable {
        runOnFxThreadAndWait(() -> {
            SampleService service = new SampleService();

            List<Double> progressList = new ArrayList<>();
            List<Worker.State> stateList = new ArrayList<>();
            List<String> titleList = new ArrayList<>();
            List<String> messageList = new ArrayList<>();

            FxTaskTracker tracker = new FxTaskTracker() {
                @Override
                public void updateTaskProgress(Task<?> task, double progress) {
                    progressList.add(progress);
                }

                @Override
                public void updateTaskState(Task<?> task, Worker.State state) {
                    stateList.add(state);
                }

                @Override
                public void updateTaskTitle(Task<?> task, String title) {
                    titleList.add(title);
                }

                @Override
                public void updateTaskMessage(Task<?> task, String message) {
                    messageList.add(message);
                }
            };

            service.addTaskTracker(tracker);

            Task<String> task = service.createTask();
            assertNotNull(task);

            // Removing tracker
            service.removeTaskTracker(tracker);
        });
    }
}
