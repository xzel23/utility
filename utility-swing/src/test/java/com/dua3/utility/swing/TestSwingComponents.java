package com.dua3.utility.swing;

import com.dua3.utility.logging.JULAdapter;
import com.dua3.utility.logging.LogBuffer;
import com.dua3.utility.logging.Log4jAdapter;
import com.dua3.utility.logging.SystemAdapter;
import org.apache.logging.log4j.LogManager;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("ClassWithMultipleLoggers")
public class TestSwingComponents extends JFrame {

    private static final Logger JUL_LOGGER = java.util.logging.Logger.getLogger("JUL." + TestSwingComponents.class.getName());
    private static final org.apache.logging.log4j.Logger LOG4J_LOGGER = LogManager.getLogger("LOG4J."+TestSwingComponents.class.getName());

    public static final int SLEEP_MILLIS = 10;
    private volatile boolean done = false;

    private final Random random = new Random();
    private final AtomicInteger n = new AtomicInteger();

    public static void main(String[] args) {
        JUL_LOGGER.setLevel(Level.ALL);
        JUL_LOGGER.info("starting up");
        
        SwingUtilities.invokeLater(() -> {
            SwingUtil.setNativeLookAndFeel();
            TestSwingComponents instance = new TestSwingComponents();
            instance.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            instance.setVisible(true);
        });
    }

    public TestSwingComponents() {
        setLayout(new GridBagLayout());
        setSize(800,600);

        final Level[] levels = { Level.FINER, Level.FINE, Level.INFO, Level.WARNING, Level.SEVERE};

        // -- SwingProcessView
        SwingProgressView<Level> progress = new SwingProgressView<>();
        int max = 200;
        
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(8,8,8,8);
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;
        add(progress, constraints);
        
        HashMap<Level,Integer> counter = new HashMap<>();
        Arrays.stream(levels).forEach(lvl -> { counter.put(lvl, 0); progress.start(lvl); });

        // -- Spacer
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;
        JSeparator separator = new JSeparator(JSeparator.HORIZONTAL);
        separator.setMinimumSize(new Dimension(8,8));
        add(separator, constraints);

        // -- SwingLogPane
        
        // setup logging
        LogBuffer buffer = new LogBuffer();
        JULAdapter.addListener(JUL_LOGGER, buffer);
        Log4jAdapter.addListener(LOG4J_LOGGER, buffer);
        SystemAdapter.addSystemListener(buffer);
        
        // create the log pane
        SwingLogPane logPane = new SwingLogPane(buffer);

        constraints = new GridBagConstraints();
        constraints.insets = new Insets(8,8,8,8);
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
                
                if (random.nextBoolean()) {
                    String msg = String.format("Message %d.", nr);
                    Level level = levels[random.nextInt(levels.length)];
                    if (level==Level.SEVERE) {
                        JUL_LOGGER.log(level, msg, generateThrowable());
                    } else {
                        JUL_LOGGER.log(level, msg);
                    }
                    progress.update(level, max, counter.compute(level, (lvl, old) -> Math.min(old+1, max)));
                } else {
                    String msg = "Message {}.";
                    Object[] args = { nr };
                    switch (random.nextInt(5)) {
                        case 0:
                            LOG4J_LOGGER.trace(msg, args);
                            break;
                        case 1:
                            LOG4J_LOGGER.debug(msg, args);
                            break;
                        case 2:
                            LOG4J_LOGGER.info(msg, args);
                            break;
                        case 3:
                            LOG4J_LOGGER.warn(msg, args);
                            break;
                        case 4:
                            LOG4J_LOGGER.error("Ouch! this is message " + nr + ".", generateThrowable());
                            break;
                        default:
                            throw new IllegalStateException();
                    }
                }

                int current = n.get();
                if (current%100==0) {
                    System.err.format("That was %d messages%n", current);
                } else if (current%10==0) {
                    System.out.format("That was %d messages%n", current);
                }
            }
        });
        thread.start();
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
