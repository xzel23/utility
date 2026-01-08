package com.dua3.utility.swing;

import com.dua3.utility.logging.LogBuffer;
import org.junit.jupiter.api.Test;

import javax.swing.WindowConstants;
import java.awt.GraphicsEnvironment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/**
 * Tests for the SwingLogFrame class.
 */
class SwingLogFrameTest {

    @Test
    void testConstructorWithDefaultTitle() {
        // Skip test in headless environment
        assumeFalse(GraphicsEnvironment.isHeadless());

        SwingLogFrame frame = null;
        try {
            frame = new SwingLogFrame();
            assertEquals("Log", frame.getTitle(), "Default title should be 'Log'");
            assertInstanceOf(SwingLogPane.class, frame.getContentPane(), "Content pane should be a SwingLogPane");
            assertEquals(WindowConstants.DISPOSE_ON_CLOSE, frame.getDefaultCloseOperation(), "Default close operation should be DISPOSE_ON_CLOSE");
        } finally {
            if (frame != null) {
                frame.dispose();
            }
        }
    }

    @Test
    void testConstructorWithCustomTitle() {
        // Skip test in headless environment
        assumeFalse(GraphicsEnvironment.isHeadless());

        SwingLogFrame frame = null;
        try {
            String customTitle = "Custom Log Title";
            frame = new SwingLogFrame(customTitle);
            assertEquals(customTitle, frame.getTitle(), "Title should be the custom title");
            assertInstanceOf(SwingLogPane.class, frame.getContentPane(), "Content pane should be a SwingLogPane");
            assertEquals(WindowConstants.DISPOSE_ON_CLOSE, frame.getDefaultCloseOperation(), "Default close operation should be DISPOSE_ON_CLOSE");
        } finally {
            if (frame != null) {
                frame.dispose();
            }
        }
    }

    @Test
    void testConstructorWithCustomTitleAndBuffer() {
        // Skip test in headless environment
        assumeFalse(GraphicsEnvironment.isHeadless());

        SwingLogFrame frame = null;
        try {
            String customTitle = "Custom Log Title";
            LogBuffer buffer = new LogBuffer(customTitle, 100);
            frame = new SwingLogFrame(customTitle, buffer);
            assertEquals(customTitle, frame.getTitle(), "Title should be the custom title");
            assertInstanceOf(SwingLogPane.class, frame.getContentPane(), "Content pane should be a SwingLogPane");
            assertEquals(WindowConstants.DISPOSE_ON_CLOSE, frame.getDefaultCloseOperation(), "Default close operation should be DISPOSE_ON_CLOSE");
        } finally {
            if (frame != null) {
                frame.dispose();
            }
        }
    }

    @Test
    void testFrameSize() {
        // Skip test in headless environment
        assumeFalse(GraphicsEnvironment.isHeadless());

        SwingLogFrame frame = null;
        try {
            frame = new SwingLogFrame();
            assertEquals(800, frame.getSize().width, "Frame width should be 800");
            assertEquals(600, frame.getSize().height, "Frame height should be 600");
        } finally {
            if (frame != null) {
                frame.dispose();
            }
        }
    }
}