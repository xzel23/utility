package com.dua3.utility.swing;

import com.dua3.utility.logging.LogBuffer;
import com.dua3.utility.logging.LogUtil;

import javax.swing.JFrame;

public class SwingLogFrame extends JFrame {
    public SwingLogFrame() {
        this("Log");
    }

    public SwingLogFrame(String title) {
        this(title, createBuffer());
    }

    public SwingLogFrame(String title, LogBuffer buffer) {
        super(title);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setContentPane(new SwingLogPane(buffer));
    }

    private static LogBuffer createBuffer() {
        LogBuffer logBuffer = new LogBuffer();
        LogUtil.getGlobalDispatcher().addLogEntryHandler(logBuffer);
        return logBuffer;
    }
}
