package com.dua3.utility.swing;

import com.dua3.utility.concurrent.ProgressTracker;
import com.dua3.utility.concurrent.ProgressView;
import com.dua3.utility.math.MathUtil;

import javax.swing.JPanel;
import javax.swing.JProgressBar;
import java.awt.GridBagLayout;

public class SwingProgressView<T> extends JPanel implements ProgressTracker<T> {
    
    private final ProgressView<T> imp;

    private static class ProgressBarIndicator implements ProgressView.ProgressIndicator {

        final JProgressBar pb;

        ProgressBarIndicator() {
            this.pb = new JProgressBar();    
        }

        @Override
        public void finish(State s) {
            pb.setValue(pb.getMaximum());            
        }

        @Override
        public void update(int done, int total) {
            if (total==0) {
                pb.setIndeterminate(true);
            } else {
                pb.setIndeterminate(false);
                pb.setMaximum(total);
                pb.setValue(done);
            }
        }

        /**
         * The integer value that corresponds to 100% in percentage mode. 
         */
        private static final int MAX = 10000;
        
        @Override
        public void update(double percentDone) {
            pb.setIndeterminate(true);
            pb.setMaximum(MAX);
            pb.setValue((int) (MathUtil.clamp(0, MAX, percentDone * MAX)+0.5));
        }
    }
    
    public SwingProgressView() {
        this.imp = new ProgressView<>(this::createProgressIndicator);

        // create the layout
        setLayout(new GridBagLayout());
    }

    private <T> ProgressView.ProgressIndicator createProgressIndicator(T t) {
        ProgressBarIndicator pi = new ProgressBarIndicator();
        add(pi.pb);
        return pi;
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

}
