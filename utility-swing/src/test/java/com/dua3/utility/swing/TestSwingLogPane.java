package com.dua3.utility.swing;

import com.dua3.utility.logging.*;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TestSwingLogPane extends JFrame {

    private static final Logger JUL_LOGGER = java.util.logging.Logger.getLogger("JUL."+TestSwingLogPane.class.getName());
    private static final ch.qos.logback.classic.Logger LGB_LOGGER = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger("SLF4J." + TestSwingLogPane.class.getName());

    public static final int SLEEP_MILLIS = 100;
    private volatile boolean done = false;

    public static void main(String[] args) {
        JUL_LOGGER.setLevel(Level.ALL);
        JUL_LOGGER.info("starting up");
        
        LGB_LOGGER.setLevel(ch.qos.logback.classic.Level.ALL);
        
        SwingUtilities.invokeLater(() -> {
            SwingUtil.setNativeLookAndFeel();
            TestSwingLogPane instance = new TestSwingLogPane();
            instance.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            instance.setVisible(true);
        });
    }

    public TestSwingLogPane() {
        LogBuffer buffer = new LogBuffer();
        JULAdapter.addListener(JUL_LOGGER, buffer);
        LogbackAdapter.addListener(LGB_LOGGER, buffer);
        
        SwingLogPane logPane = new SwingLogPane(buffer);
        
        setContentPane(logPane);
        setSize(800,600);
        Level[] levels = { Level.FINER, Level.FINE, Level.INFO, Level.WARNING, Level.SEVERE};
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
                } else {
                    String msg = "Message {}.";
                    Object[] args = { nr };
                    switch (random.nextInt(5)) {
                        case 0:
                            LGB_LOGGER.trace(msg, args);
                            break;
                        case 1:
                            LGB_LOGGER.debug(msg, args);
                            break;
                        case 2:
                            LGB_LOGGER.info(msg, args);
                            break;
                        case 3:
                            LGB_LOGGER.warn(msg, args);
                            break;
                        case 4:
                            LGB_LOGGER.error("Ouch! this is message "+nr+".", generateThrowable());
                            break;
                        default:
                            throw new IllegalStateException();
                    }
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

    private final Random random = new Random();
    private final AtomicInteger n = new AtomicInteger();
    
    @Override
    public void dispose() {
        done = true;
        super.dispose();
    }
}
