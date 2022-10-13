package com.dua3.utility.swing.test;

import com.dua3.utility.concurrent.ProgressTracker;
import com.dua3.utility.logging.LogBuffer;
import com.dua3.utility.swing.SwingLogPane;
import com.dua3.utility.swing.SwingProgressView;
import com.dua3.utility.swing.SwingUtil;

import org.slf4j.event.Level;
import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import javax.swing.JFrame;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings({"ClassWithMultipleLoggers", "BusyWait"})
public class SwingComponentsSample extends JFrame {

    public static final String TASK_INDETERMINATE_1 = "Indeterminate Task";
    public static final String TASK_INDETERMINATE_2 = "Another Indeterminate Task";

    static {
        java.util.logging.LogManager.getLogManager().reset();
        SLF4JBridgeHandler.install();
    }

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger("SLF4J." + SwingComponentsSample.class.getName());
    private static final java.util.logging.Logger JUL_LOGGER = java.util.logging.Logger.getLogger("JUL." + SwingComponentsSample.class.getName());
    private static final org.apache.logging.log4j.Logger LOG4J_LOGGER = org.apache.logging.log4j.LogManager.getLogger("LOG4J." + SwingComponentsSample.class.getName());

    public static final int SLEEP_MILLIS = 25;
    private volatile boolean done = false;

    @SuppressWarnings("UnsecureRandomNumberGeneration") // used only to create a random sequence of log levels in tests
    private final Random random = new Random();
    private final AtomicInteger n = new AtomicInteger();

    public static void main(String[] args) {
        LOG.info("starting up");

        SwingUtil.setNativeLookAndFeel();

        SwingUtilities.invokeLater(() -> {
            SwingComponentsSample instance = new SwingComponentsSample();
            instance.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            instance.setVisible(true);
        });
    }

    public SwingComponentsSample() {
        setLayout(new GridBagLayout());
        setSize(800, 600);

        init();
    }

    private void init() {
        // -- SwingProcessView
        SwingProgressView<Object> progress = new SwingProgressView<>();
        int max = 200;

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(8, 8, 8, 8);
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;
        add(progress, constraints);

        HashMap<Level, Integer> counter = new HashMap<>();
        Arrays.stream(Level.values()).forEach(lvl -> {
            counter.put(lvl, 0);
            progress.start(lvl);
        });
        progress.start(TASK_INDETERMINATE_1);

        // -- Spacer
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;
        JSeparator separator = new JSeparator(JSeparator.HORIZONTAL);
        separator.setMinimumSize(new Dimension(8, 8));
        add(separator, constraints);

        // -- SwingLogPane

        // setup logging
        ILoggerFactory loggerFactory = LoggerFactory.getILoggerFactory();
        if (!(loggerFactory instanceof com.dua3.utility.logging.LoggerFactory)) {
            throw new IllegalStateException("wrong logging implementaion!");
        }
        LogBuffer buffer = ((com.dua3.utility.logging.LoggerFactory) loggerFactory).getLogBuffer()
                .orElseThrow(() -> new IllegalStateException("buffer not configured"));

        // create the log pane
        SwingLogPane logPane = new SwingLogPane(buffer);

        constraints = new GridBagConstraints();
        constraints.insets = new Insets(8, 8, 8, 8);
        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1;
        constraints.weighty = 1;
        add(logPane, constraints);

        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            while (!done) {
                try {
                    Thread.sleep(SLEEP_MILLIS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                int nr = n.incrementAndGet();
                String msg = "Message " + nr + ".";

                int implementation = random.nextInt(3);
                int bound = implementation == 1 ? 6 : 5;
                int levelInt = random.nextInt(bound);
                Level level = Level.values()[implementation == 1 ? Math.max(0, levelInt - 1) : levelInt];

                switch (implementation) {
                    case 0:
                        switch (levelInt) {
                            case 0 -> LOG.trace(msg);
                            case 1 -> LOG.debug(msg);
                            case 2 -> LOG.info(msg);
                            case 3 -> LOG.warn(msg);
                            case 4 -> LOG.error(msg, generateThrowable());
                            default -> throw new IllegalStateException("integer out of range");
                        }
                        break;

                    case 1:
                        switch (levelInt) {
                            case 0 -> JUL_LOGGER.finest(msg);
                            case 1 -> JUL_LOGGER.finer(msg);
                            case 2 -> JUL_LOGGER.fine(msg);
                            case 3 -> JUL_LOGGER.info(msg);
                            case 4 -> JUL_LOGGER.warning(msg);
                            case 5 -> JUL_LOGGER.log(java.util.logging.Level.SEVERE, msg, generateThrowable());
                            default -> throw new IllegalStateException("integer out of range");
                        }
                        break;

                    case 2:
                        switch (levelInt) {
                            case 0 -> LOG4J_LOGGER.trace(msg);
                            case 1 -> LOG4J_LOGGER.debug(msg);
                            case 2 -> LOG4J_LOGGER.info(msg);
                            case 3 -> LOG4J_LOGGER.warn(msg);
                            case 4 -> LOG4J_LOGGER.error(msg, generateThrowable());
                            default -> throw new IllegalStateException("integer out of range");
                        }
                        break;
                }
                progress.update(level, max, counter.compute(level, (lvl, old) -> Math.min(old + 1, max)));

                int current = n.get();
                if (current % 100 == 0) {
                    System.err.format("That was %d messages%n", current);
                } else if (current % 10 == 0) {
                    System.out.format("That was %d messages%n", current);
                }
            }
        });
        thread.start();

        Thread thread2 = new Thread(() -> {
            try {
                Thread.sleep(10_000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            progress.finish(TASK_INDETERMINATE_1, ProgressTracker.State.COMPLETED_SUCCESS);

            progress.start(TASK_INDETERMINATE_2);
            try {
                Thread.sleep(10_000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            progress.finish(TASK_INDETERMINATE_2, ProgressTracker.State.COMPLETED_SUCCESS);
        });
        thread2.start();
    }

    private IllegalStateException generateThrowable() {
        if (random.nextBoolean()) {
            return new IllegalStateException("Why?", new UnsupportedOperationException("Because of me!"));
        } else {
            return new IllegalStateException("What happened?");
        }
    }

    @Override
    public void dispose() {
        done = true;
        super.dispose();
    }
}
