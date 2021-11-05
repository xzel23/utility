package com.dua3.utility.swing;

import com.dua3.utility.concurrent.ProgressTracker;
import com.dua3.utility.concurrent.ProgressView;
import com.dua3.utility.math.MathUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Label;

/**
 * A {@link ProgressTracker} implementation for use in Swing applications.
 * @param <T> the task type
 */
public class SwingProgressView<T> extends JPanel implements ProgressTracker<T> {
    
    private final @NotNull ProgressView<T> imp;
    private int rowCount = 0;

    private static class ProgressBarIndicator implements ProgressView.ProgressIndicator {

        final @NotNull JProgressBar pb;

        ProgressBarIndicator() {
            this.pb = new JProgressBar();    
        }

        @Override
        public void finish(@NotNull State s) {
            SwingUtilities.invokeLater( () -> {
                if (pb.isIndeterminate()) {
                    pb.setIndeterminate(false);
                    pb.setMaximum(1);
                }
                
                pb.setValue(pb.getMaximum());
            });
        }

        @Override
        public void update(int done, int total) {
            SwingUtilities.invokeLater( () -> {
                if (total==0) {
                    pb.setIndeterminate(true);
                    pb.setMaximum(1);
                    pb.setValue(0);
                } else {
                    pb.setIndeterminate(false);
                    pb.setMaximum(total);
                    pb.setValue(done);
                }
            });
        }
        
        /**
         * The integer value that corresponds to 100% in percentage mode. 
         */
        private static final int MAX = 1000;
        
        @Override
        public void update(double percentDone) {
            SwingUtilities.invokeLater( () -> {
                pb.setIndeterminate(false);
                pb.setMaximum(MAX);
                pb.setValue((int) (MathUtil.clamp(0, MAX, percentDone * MAX) + 0.5));
            });
        }
    }
    
    public SwingProgressView() {
        this.imp = new ProgressView<>(this::createProgressIndicator);

        // create the layout
        setLayout(new GridBagLayout());
    }

    private <T> ProgressView.@NotNull ProgressIndicator createProgressIndicator(@NotNull T t) {
        ProgressBarIndicator pi = new ProgressBarIndicator();
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.LINE_END;
        constraints.ipadx = 8;
        constraints.gridx = 0;
        constraints.gridy = rowCount;
        add(new Label(t.toString()), constraints);
        constraints.gridx = 1;
        constraints.gridy = rowCount;
        add(pi.pb, constraints);
        rowCount++;
        return pi;
    }

    @Override
    public void schedule(@NotNull T task) {
        imp.schedule(task);
    }

    @Override
    public void start(@NotNull T task) {
        imp.start(task);
    }

    @Override
    public void pause(@NotNull T task) {
        imp.pause(task);
    }

    @Override
    public void abort(@NotNull T task) {
        imp.abort(task);
    }

    @Override
    public void finish(@NotNull T task, @NotNull State s) {
        imp.finish(task, s);
    }

    @Override
    public void update(@NotNull T task, int total, int done) {
        imp.update(task, total, done);
    }

    @Override
    public void update(@NotNull T task, double percentDone) {
        imp.update(task, percentDone);
    }

}
