package com.dua3.utility.swing;

import com.dua3.utility.concurrent.ProgressTracker;
import com.dua3.utility.lang.LangUtil;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class SwingProgressView<T> extends JPanel implements ProgressTracker<T> {
    
    private static class TaskRecord {
        final JProgressBar progressBar;
        State state = State.SCHEDLULED;

        TaskRecord(JProgressBar progressBar) {
            this.progressBar = Objects.requireNonNull(progressBar);
        }
    }

    private final Map<T, TaskRecord> tasks = Collections.synchronizedMap(new LinkedHashMap<>());
    
    @SafeVarargs
    public SwingProgressView(T... tasks) {
        // create the layout
        setLayout(new MigLayout());
        
        // add progress bars for tasks
        for (T task: tasks) {
            getTaskRecord(task);
        }
    }

    private TaskRecord getTaskRecord(T task) {
        return tasks.computeIfAbsent(task, t -> {
            JProgressBar pb = new JProgressBar();
            pb.setMaximum(1);
            pb.setValue(0);
            add(new JLabel(Objects.toString(t)));
            add(pb, "wrap, growx");
            return new TaskRecord(pb);
        });
    }
    
    @Override
    public void schedule(T task) {
        // getTaskRecord() will enter an entry for the task if it is not yet present
        getTaskRecord(task);
    }
    
    @Override
    public void start(T task) {
        TaskRecord r = getTaskRecord(task);
        r.state = State.RUNNING;
        update(task, 0.0);
    }

    @Override
    public void pause(T task) {
        TaskRecord r = getTaskRecord(task);
        LangUtil.check(r.state == State.SCHEDLULED, "task not scheduled: %s (%s)", task, r.state);
        r.state = State.PAUSED;
    }

    @Override
    public void abort(T task) {
        TaskRecord r = getTaskRecord(task);
        LangUtil.check(!r.state.isTerminal(), "task already completed: %s (%s)", task, r.state);
        r.state = State.ABORTED;
    }

    @Override
    public void finish(T task, State s) {
        LangUtil.check(s.isTerminal(), "not a terminal state: %s", s);
        
        TaskRecord r = getTaskRecord(task);
        LangUtil.check(!r.state.isTerminal(), "task already terminated: %s (%s)", task, r.state);

        JProgressBar pb = r.progressBar;
        if (pb.isIndeterminate()) {
            pb.setIndeterminate(false);
            pb.setMaximum(1);
        }
        
        pb.setValue(pb.getMaximum());
    }

    @Override
    public void update(T task, int total, int done) {
        assert 0 <= done && done<=total;

        SwingUtilities.invokeLater(() -> {
            TaskRecord r = getTaskRecord(task);
            r.state = State.RUNNING;
            
            JProgressBar pb = r.progressBar;

            if (pb.isIndeterminate()) {
                pb.setIndeterminate(false);
            }

            pb.setMaximum(total);
            pb.setValue(done);
        });
    }

    @Override
    public void update(T task, double percentDone) {
        assert 0 <= percentDone && percentDone<=1.0;
        
        if (percentDone<0) {
            // indeterminate
            SwingUtilities.invokeLater(() -> getTaskRecord(task).progressBar.setIndeterminate(true));
        } else {
            // determinate
            int max = 10_000;
            update(task, max, (int) (max*percentDone));
        }
    }

}
