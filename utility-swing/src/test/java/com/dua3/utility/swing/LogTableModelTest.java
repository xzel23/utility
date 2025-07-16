package com.dua3.utility.swing;

import com.dua3.utility.logging.LogBuffer;
import com.dua3.utility.logging.LogEntry;
import com.dua3.utility.logging.LogLevel;
import com.dua3.utility.logging.SimpleLogEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Tests for the LogTableModel class.
 */
class LogTableModelTest {

    private LogBuffer buffer;
    private LogTableModel model;

    @BeforeEach
    void setUp() {
        buffer = new LogBuffer(100);
        model = new LogTableModel(buffer);
    }

    @Test
    void testInitialState() {
        assertEquals(0, model.getRowCount(), "Initial row count should be 0");
        assertEquals(SwingLogPane.COLUMNS.length, model.getColumnCount(), "Column count should match SwingLogPane.COLUMNS length");
    }

    @Test
    void testAddLogEntry() {
        // Add a log entry to the buffer
        LogEntry entry = new SimpleLogEntry("Test message", "test.logger", Instant.now(), LogLevel.INFO, "", null, null);
        buffer.handleEntry(entry);

        // Wait a bit for the update thread to process the change
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        assertEquals(1, model.getRowCount(), "Row count should be 1 after adding an entry");
        assertEquals(entry, model.getValueAt(0, 0), "The entry at row 0 should be the one we added");
    }

    @Test
    void testClearBuffer() {
        // Add some log entries
        for (int i = 0; i < 5; i++) {
            buffer.handleEntry(new SimpleLogEntry("Test message " + i, "test.logger", Instant.now(), LogLevel.INFO, "", null, null));
        }

        // Wait a bit for the update thread to process the changes
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        assertEquals(5, model.getRowCount(), "Row count should be 5 after adding 5 entries");

        // Clear the buffer
        buffer.clear();

        // Wait a bit for the update thread to process the clear
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        assertEquals(0, model.getRowCount(), "Row count should be 0 after clearing the buffer");
    }

    @Test
    void testGetColumnName() {
        for (int i = 0; i < model.getColumnCount(); i++) {
            String columnName = model.getColumnName(i);
            assertNotNull(columnName, "Column name should not be null");
            assertEquals(SwingLogPane.COLUMNS[i].field().name(), columnName, "Column name should match the field name in SwingLogPane.COLUMNS");
        }
    }

    @Test
    void testGetColumnClass() {
        for (int i = 0; i < model.getColumnCount(); i++) {
            Class<?> columnClass = model.getColumnClass(i);
            assertSame(SwingLogPane.LogEntryField.class, columnClass, "Column class should be LogEntryField");
        }
    }

    @Test
    void testMultipleEntries() {
        // Add multiple log entries with different levels
        LogEntry entry1 = new SimpleLogEntry("Info message", "test.logger", Instant.now(), LogLevel.INFO, "", null, null);
        LogEntry entry2 = new SimpleLogEntry("Warning message", "test.logger", Instant.now(), LogLevel.WARN, "", null, null);
        LogEntry entry3 = new SimpleLogEntry("Error message", "test.logger", Instant.now(), LogLevel.ERROR, "", null, null);

        buffer.handleEntry(entry1);
        buffer.handleEntry(entry2);
        buffer.handleEntry(entry3);

        // Wait a bit for the update thread to process the changes
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        assertEquals(3, model.getRowCount(), "Row count should be 3 after adding 3 entries");
        assertEquals(entry1, model.getValueAt(0, 0), "First entry should be at row 0");
        assertEquals(entry2, model.getValueAt(1, 0), "Second entry should be at row 1");
        assertEquals(entry3, model.getValueAt(2, 0), "Third entry should be at row 2");
    }
}
