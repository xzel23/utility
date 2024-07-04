package com.dua3.utility.fx;

import com.dua3.utility.logging.LogBuffer;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * The FxLogWindow class represents a JavaFX window that displays log entries in a table view.
 * It extends the Stage class.
 */
public class FxLogWindow extends Stage {

    public static final String DEFAULT_WINDOW_TITLE = "Log";
    private final LogBuffer logBuffer;
    private final FxLogPane logPane;

    /**
     * Create a new FxLogWindow instance with a new {@link LogBuffer} using the default capacity;
     */
    public FxLogWindow() {
        this(DEFAULT_WINDOW_TITLE);
    }

    /**
     * Create a new FxLogWindow instance with a new {@link LogBuffer} using the default capacity;
     *
     * @param title the window title
     */
    public FxLogWindow(String title) {
        this(title, new LogBuffer());
    }

    /**
     * Constructs a new instance of {@code FxLogWindow} with the specified maximum number of lines.
     *
     * @param maxLines the maximum number of lines to display in the log window
     */
    public FxLogWindow(int maxLines) {
        this(DEFAULT_WINDOW_TITLE, maxLines);
    }

    /**
     * Constructs a new instance of {@code FxLogWindow} with the specified maximum number of lines.
     *
     * @param title the window title
     * @param maxLines the maximum number of lines to display in the log window
     */
    public FxLogWindow(String title, int maxLines) {
        this(title, new LogBuffer(maxLines));
    }

    /**
     * Constructs a new instance of {@code FxLogWindow} using the provided {@link LogBuffer}.
     *
     * @param logBuffer the LogBuffer to use
     */
    public FxLogWindow(LogBuffer logBuffer) {
        this(DEFAULT_WINDOW_TITLE, logBuffer);
    }

    /**
     * Constructs a new instance of {@code FxLogWindow} using the provided {@link LogBuffer}.
     *
     * @param title the window title
     * @param logBuffer the LogBuffer to use
     */
    public FxLogWindow(String title, LogBuffer logBuffer) {
        this.logBuffer = logBuffer;
        logPane = new FxLogPane(this.logBuffer);
        Scene scene = new Scene(logPane, 800, 400);
        setScene(scene);
        setTitle(title);
    }

    /**
     * Retrieves the LogBuffer associated with this FxLogWindow.
     *
     * @return the LogBuffer instance used by this FxLogWindow
     */
    public LogBuffer getLogBuffer() {
        return logBuffer;
    }
}
