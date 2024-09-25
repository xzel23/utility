package com.dua3.utility.swing;

import com.dua3.cabe.annotations.Nullable;
import com.dua3.utility.logging.LogBuffer;

import javax.swing.JFrame;

/**
 * {@code SwingLogFrame} is a {@link JFrame} that provides a user interface component for displaying log messages.
 * It initializes with a specified title and contains a {@link SwingLogPane} to manage the display of log data.
 */
public class SwingLogFrame extends JFrame {
    /**
     * Creates a new instance of {@code SwingLogFrame} with the default title "Log".
     */
    public SwingLogFrame() {
        this("Log");
    }

    /**
     * Constructs a {@code SwingLogFrame} with the specified title.
     *
     * @param title the title for the frame, displayed in the frame's title bar.
     */
    public SwingLogFrame(String title) {
        this(title, null);
    }

    /**
     * Constructs a new {@code SwingLogFrame} with the specified title and log buffer.
     *
     * @param title  the title for the frame
     * @param buffer the log buffer, which may be {@code null}
     */
    public SwingLogFrame(String title, @Nullable LogBuffer buffer) {
        super(title);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setContentPane(new SwingLogPane(buffer));
        setSize(800, 600);
    }
}
