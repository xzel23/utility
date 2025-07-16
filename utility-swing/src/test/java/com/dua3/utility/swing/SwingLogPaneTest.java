package com.dua3.utility.swing;

import com.dua3.utility.logging.LogBuffer;
import com.dua3.utility.logging.LogEntry;
import com.dua3.utility.logging.LogLevel;
import com.dua3.utility.logging.SimpleLogEntry;
import org.junit.jupiter.api.Test;

import javax.swing.JTable;
import javax.swing.JTextArea;
import java.awt.GraphicsEnvironment;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests for the SwingLogPane class.
 */
class SwingLogPaneTest {

    @Test
    void testConstructorWithDefaultBuffer() {
        // Skip test in headless environment
        if (GraphicsEnvironment.isHeadless()) {
            return;
        }

        SwingLogPane pane = new SwingLogPane();
        assertNotNull(pane, "SwingLogPane should be created successfully");

        // Check if the pane contains a JTable
        List<JTable> tables = SwingTestUtil.findComponentsOfType(pane, JTable.class);
        assertFalse(tables.isEmpty(), "SwingLogPane should contain a JTable");

        // Check if the pane contains a JTextArea for details
        List<JTextArea> textAreas = SwingTestUtil.findComponentsOfType(pane, JTextArea.class);
        assertFalse(textAreas.isEmpty(), "SwingLogPane should contain a JTextArea for details");
    }

    @Test
    void testConstructorWithCustomBufferSize() {
        // Skip test in headless environment
        if (GraphicsEnvironment.isHeadless()) {
            return;
        }

        int bufferSize = 50;
        SwingLogPane pane = new SwingLogPane(bufferSize);
        assertNotNull(pane, "SwingLogPane should be created successfully with custom buffer size");
    }

    @Test
    void testConstructorWithCustomBuffer() {
        // Skip test in headless environment
        if (GraphicsEnvironment.isHeadless()) {
            return;
        }

        LogBuffer buffer = new LogBuffer(100);
        SwingLogPane pane = new SwingLogPane(buffer);
        assertNotNull(pane, "SwingLogPane should be created successfully with custom buffer");
    }

    @Test
    void testAddLogEntryToBuffer() {
        // Skip test in headless environment
        if (GraphicsEnvironment.isHeadless()) {
            return;
        }

        LogBuffer buffer = new LogBuffer(100);
        SwingLogPane pane = new SwingLogPane(buffer);

        // Add a log entry to the buffer
        LogEntry entry = new SimpleLogEntry("Test message", "test.logger", Instant.now(), LogLevel.INFO, "", null, null);
        buffer.handleEntry(entry);

        // Wait a bit for the UI to update
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Get the table from the pane
        List<JTable> tables = SwingTestUtil.findComponentsOfType(pane, JTable.class);
        assertFalse(tables.isEmpty(), "SwingLogPane should contain a JTable");
        JTable table = tables.get(0);

        // Check if the table has one row
        assertEquals(1, table.getModel().getRowCount(), "Table should have one row after adding an entry");
    }

    @Test
    void testClearBuffer() {
        // Skip test in headless environment
        if (GraphicsEnvironment.isHeadless()) {
            return;
        }

        LogBuffer buffer = new LogBuffer(100);
        SwingLogPane pane = new SwingLogPane(buffer);

        // Add some log entries
        for (int i = 0; i < 5; i++) {
            buffer.handleEntry(new SimpleLogEntry("Test message " + i, "test.logger", Instant.now(), LogLevel.INFO, "", null, null));
        }

        // Wait a bit for the UI to update
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Get the table from the pane
        List<JTable> tables = SwingTestUtil.findComponentsOfType(pane, JTable.class);
        assertFalse(tables.isEmpty(), "SwingLogPane should contain a JTable");
        JTable table = tables.get(0);

        // Check if the table has five rows
        assertEquals(5, table.getModel().getRowCount(), "Table should have five rows after adding 5 entries");

        // Clear the buffer
        pane.clearBuffer();

        // Wait a bit for the UI to update
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Check if the table is empty
        assertEquals(0, table.getModel().getRowCount(), "Table should be empty after clearing the buffer");
    }

    @Test
    void testScrollRowIntoView() {
        // Skip test in headless environment
        if (GraphicsEnvironment.isHeadless()) {
            return;
        }

        assertDoesNotThrow(() -> {
            LogBuffer buffer = new LogBuffer(100);
            SwingLogPane pane = new SwingLogPane(buffer);

            // Add some log entries
            for (int i = 0; i < 20; i++) {
                buffer.handleEntry(new SimpleLogEntry("Test message " + i, "test.logger", Instant.now(), LogLevel.INFO, "", null, null));
            }

            // Wait a bit for the UI to update
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // Scroll to a specific row
            pane.scrollRowIntoView(10);

            // No assertion here as we can't easily check the scroll position
            // This is more of a smoke test to ensure the method doesn't throw exceptions
        });
    }

    @Test
    void testSetDividerLocation() {
        // Skip test in headless environment
        if (GraphicsEnvironment.isHeadless()) {
            return;
        }

        assertDoesNotThrow(() -> {
            SwingLogPane pane = new SwingLogPane();

            // Set divider location as a proportion
            double location = 0.7;
            pane.setDividerLocation(location);

            // Set divider location as an absolute value
            pane.setDividerLocation(300);

            // No assertion here as we can't easily check the divider location
            // This is more of a smoke test to ensure the methods don't throw exceptions
        });
    }
}