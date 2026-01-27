package com.dua3.utility.swing;

import com.dua3.utility.concurrent.ProgressTracker;
import com.dua3.utility.concurrent.ProgressView;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Label;

/**
 * A {@link ProgressTracker} implementation for use in Swing applications.
 *
 * @param <T> the task type
 */
public final class SwingProgressView<T> extends JPanel implements ProgressTracker<T> {

    /**
     * The {@link ProgressView} instance used by {@code SwingProgressView} to manage and display
     * the progress of tasks.
     */
    private final transient ProgressView<T> imp;

    /**
     * The number of rows currently tracked or rendered in the progress view.
     */
    private int rowCount = 0;

    /**
     * Constructor.
     */
    public SwingProgressView() {
        this.imp = new ProgressView<>(this::createProgressIndicator);

        // create the layout
        setLayout(new GridBagLayout());
    }

    private ProgressView.ProgressIndicator createProgressIndicator(T t) {
        ProgressBarIndicator pi = new ProgressBarIndicator();
        int row = rowCount++;
        SwingUtilities.invokeLater(() -> {
            GridBagConstraints constraints = new GridBagConstraints();
            constraints.anchor = GridBagConstraints.LINE_END;
            constraints.ipadx = 8;
            constraints.gridx = 0;
            constraints.gridy = row;
            add(new Label(t.toString()), constraints);
            constraints.gridx = 1;
            constraints.gridy = row;
            add(pi.pb, constraints);
            revalidate();
        });
        return pi;
    }

    @Override
    @SafeVarargs
    public final void scheduleTaskGroup(String group, T... tasks) {
        int row = rowCount++;
        SwingUtilities.invokeLater(() -> {
            GridBagConstraints constraints = new GridBagConstraints();
            constraints.anchor = GridBagConstraints.LINE_START;
            constraints.ipadx = 8;
            constraints.gridx = 0;
            constraints.gridy = row;
            constraints.gridwidth = 2;
            JLabel label = new JLabel(group);
            Font labelFont = label.getFont();
            label.setFont(labelFont.deriveFont(labelFont.getStyle() | Font.BOLD));
            add(label, constraints);
        });

        ProgressTracker.super.scheduleTaskGroup(group, tasks);
    }

    @Override
    public void schedule(T task) {
        imp.schedule(task);
    }

    @Override
    public void start(T task) {
        imp.start(task);
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

    /**
     * Implementation of {@code ProgressView.ProgressIndicator} that uses a {@link JProgressBar}
     * to display the progress of a task within a {@link SwingProgressView}.
     */
    private static class ProgressBarIndicator implements ProgressView.ProgressIndicator {

        /**
         * The integer value that corresponds to 100% in percentage mode.
         */
        private static final int MAX = 1000;
        final JProgressBar pb;

        ProgressBarIndicator() {
            this.pb = new JProgressBar();
        }

        @Override
        public void finish(State s) {
            SwingUtilities.invokeLater(() -> {
                if (pb.isIndeterminate()) {
                    pb.setIndeterminate(false);
                    pb.setMaximum(1);
                }

                pb.setValue(pb.getMaximum());
            });
        }

        @Override
        public void update(int total, int done) {
            SwingUtilities.invokeLater(() -> {
                if (total == 0) {
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

        @Override
        public void update(double percentDone) {
            SwingUtilities.invokeLater(() -> {
                if (Double.isNaN(percentDone)) {
                    pb.setIndeterminate(true);
                    pb.setMaximum(1);
                    pb.setValue(0);
                } else {
                    pb.setIndeterminate(false);
                    pb.setMaximum(MAX);
                    //noinspection NumericCastThatLosesPrecision
                    pb.setValue((int) (Math.clamp(percentDone * MAX, 0, MAX) + 0.5));
                }
            });
        }
    }

}
