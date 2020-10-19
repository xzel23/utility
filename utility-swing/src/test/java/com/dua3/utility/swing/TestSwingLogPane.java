package com.dua3.utility.swing;

import com.dua3.utility.logging.Category;
import com.dua3.utility.logging.LogBuffer;
import com.dua3.utility.logging.LogEntry;

import javax.swing.*;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class TestSwingLogPane extends JFrame {

    private volatile boolean done = false;
    private final Thread thread;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SwingUtil.setNativeLookAndFeel();
            TestSwingLogPane instance = new TestSwingLogPane();
            instance.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            instance.setVisible(true);
        });
    }

    public TestSwingLogPane() {
        LogBuffer buffer = new LogBuffer();
        SwingLogPane logPane = new SwingLogPane(buffer);
        setContentPane(logPane);
        setSize(800,600);
        thread = new Thread(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            while (!done) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                buffer.add(createEntry());
            }
        });
        thread.start();
    }

    private final Random random = new Random();
    private final AtomicInteger n = new AtomicInteger();
    
    private LogEntry createEntry() {
        int lvl = random.nextInt(Category.values().length);
        Category cat = Category.values()[lvl];
        long millis = System.currentTimeMillis();
        String msg = String.format("Message %d.", n.incrementAndGet());
        
        return new LogEntry() {
            @Override
            public Category category() {
                return cat;
            }

            @Override
            public String level() {
                return Integer.toString(lvl);
            }

            @Override
            public String logger() {
                return "logger.with.no.name";
            }

            @Override
            public long millis() {
                return millis;
            }

            @Override
            public String message() {
                return msg;
            }

            @Override
            public StackTraceElement[] stacktrace() {
                return new StackTraceElement[0];
            }
        };
    }

    @Override
    public void dispose() {
        done = true;
        super.dispose();
    }
}
