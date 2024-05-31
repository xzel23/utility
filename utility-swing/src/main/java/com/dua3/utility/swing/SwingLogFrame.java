package com.dua3.utility.swing;

import com.dua3.cabe.annotations.Nullable;
import com.dua3.utility.logging.LogBuffer;

import javax.swing.JFrame;

public class SwingLogFrame extends JFrame {
    public SwingLogFrame() {
        this("Log");
    }

    public SwingLogFrame(String title) {
        this(title, null);
    }

    public SwingLogFrame(String title, @Nullable LogBuffer buffer) {
        super(title);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setContentPane(new SwingLogPane(buffer));
        setSize(800, 600);
    }
}
