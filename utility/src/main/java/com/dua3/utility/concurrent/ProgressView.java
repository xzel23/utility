package com.dua3.utility.concurrent;

import com.dua3.utility.lang.LangUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class ProgressView<T> implements ProgressTracker<T> {

    public interface ProgressIndicator {
        void finish(@NotNull State s);
        void update(int done, int total);
        void update(double percentDone);
    }
    
    private static class TaskRecord {
        private final @NotNull ProgressIndicator progressIndicator;
        @NotNull State state = State.SCHEDULED;

        TaskRecord(@NotNull ProgressIndicator progressIndicator) {
            this.progressIndicator = Objects.requireNonNull(progressIndicator);
        }

        public void update(int done, int total) {
            progressIndicator.update(done, total);
        }

        public void finish(@NotNull State s) {
            progressIndicator.finish(s);
        }

        public void update(double percentDone) {
            progressIndicator.update(percentDone);
        }
    }

    private final Function<T,ProgressIndicator> createProgessIndicator;
    
    private final Map<T, TaskRecord> tasks = Collections.synchronizedMap(new LinkedHashMap<>());

    public ProgressView(@NotNull Function<T,ProgressIndicator> createProgessIndicator) {
        this.createProgessIndicator = Objects.requireNonNull(createProgessIndicator);
    }
    
    @SafeVarargs
    public final void addTasks(T @NotNull ... tasks) {
        for (T task: tasks) {
            getTaskRecord(task);
        }
    }

    private @NotNull TaskRecord getTaskRecord(@NotNull T task) {
        return tasks.computeIfAbsent(task, t -> {
            ProgressIndicator pi = createProgessIndicator.apply(t);
            return new TaskRecord(pi);
        });
    }

    @Override
    public void schedule(@NotNull T task) {
        // getTaskRecord() will enter an entry for the task if it is not yet present
        getTaskRecord(task);
    }

    @Override
    public void start(@NotNull T task) {
        TaskRecord r = getTaskRecord(task);
        r.state = State.RUNNING;
        update(task, 0, 0);
    }

    @Override
    public void pause(@NotNull T task) {
        TaskRecord r = getTaskRecord(task);
        LangUtil.check(r.state == State.SCHEDULED, "task not scheduled: %s (%s)", task, r.state);
        r.state = State.PAUSED;
    }

    @Override
    public void abort(@NotNull T task) {
        TaskRecord r = getTaskRecord(task);
        LangUtil.check(!r.state.isTerminal(), "task already completed: %s (%s)", task, r.state);
        r.state = State.ABORTED;
    }

    @Override
    public void finish(@NotNull T task, @NotNull State s) {
        LangUtil.check(s.isTerminal(), "not a terminal state: %s", s);

        TaskRecord r = getTaskRecord(task);
        LangUtil.check(!r.state.isTerminal(), "task already terminated: %s (%s)", task, r.state);

        r.finish(s);
    }

    @Override
    public void update(@NotNull T task, int total, int done) {
        assert 0 <= done && done<=total;
        getTaskRecord(task).update(done, total);
    }

    @Override
    public void update(@NotNull T task, double percentDone) {
        assert 0 <= percentDone && percentDone<=1.0;
        getTaskRecord(task).update(percentDone);
    }

}
