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
    void testAddLogEntry() throws InterruptedException {
        // Add a log entry to the buffer
        LogEntry entry = new SimpleLogEntry("Test message", "test.logger", Instant.now(), LogLevel.INFO, "", null, null);
        buffer.handleEntry(entry);

        // Wait for the model to update asynchronously
        waitForRowCount(1);

        assertEquals(1, model.getRowCount(), "Row count should be 1 after adding an entry");
        assertEquals(entry, model.getValueAt(0, 0), "The entry at row 0 should be the one we added");
    }

    /**
     * Helper method to wait for the model to reach the expected row count.
     * This is necessary because the LogTableModel updates asynchronously.
     */
    private void waitForRowCount(int expectedCount) throws InterruptedException {
        long startTime = System.currentTimeMillis();
        long timeout = 5000; // 5 second timeout

        while (model.getRowCount() != expectedCount) {
            if (System.currentTimeMillis() - startTime > timeout) {
                throw new AssertionError("Timeout waiting for row count to reach " + expectedCount + ", current count: " + model.getRowCount());
            }
            Thread.sleep(10); // Wait a bit before checking again
        }
    }

    @Test
    void testClearBuffer() throws InterruptedException {
        // Add some log entries one by one with small delays to ensure proper processing
        for (int i = 0; i < 5; i++) {
            buffer.handleEntry(new SimpleLogEntry("Test message " + i, "test.logger", Instant.now(), LogLevel.INFO, "", null, null));
            // Small delay to help ensure each entry is processed
            Thread.sleep(10);
        }

        // Wait for the model to update asynchronously
        waitForRowCount(5);

        assertEquals(5, model.getRowCount(), "Row count should be 5 after adding 5 entries");

        // Clear the buffer
        buffer.clear();

        // Wait for the model to update asynchronously
        waitForRowCount(0);

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
    void testMultipleEntries() throws InterruptedException {
        // Add multiple log entries with different levels
        LogEntry entry1 = new SimpleLogEntry("Info message", "test.logger", Instant.now(), LogLevel.INFO, "", null, null);
        LogEntry entry2 = new SimpleLogEntry("Warning message", "test.logger", Instant.now(), LogLevel.WARN, "", null, null);
        LogEntry entry3 = new SimpleLogEntry("Error message", "test.logger", Instant.now(), LogLevel.ERROR, "", null, null);

        buffer.handleEntry(entry1);
        Thread.sleep(10); // Small delay
        buffer.handleEntry(entry2);
        Thread.sleep(10); // Small delay
        buffer.handleEntry(entry3);

        // Wait for the model to update asynchronously
        waitForRowCount(3);

        assertEquals(3, model.getRowCount(), "Row count should be 3 after adding 3 entries");
        assertEquals(entry1, model.getValueAt(0, 0), "First entry should be at row 0");
        assertEquals(entry2, model.getValueAt(1, 0), "Second entry should be at row 1");
        assertEquals(entry3, model.getValueAt(2, 0), "Third entry should be at row 2");
    }

    /**
     * Alternative test that focuses on the final state rather than individual updates
     */
    @Test
    void testBatchAddEntries() throws InterruptedException {
        // Add all entries at once
        LogEntry[] entries = new LogEntry[5];
        for (int i = 0; i < 5; i++) {
            entries[i] = new SimpleLogEntry("Test message " + i, "test.logger", Instant.now(), LogLevel.INFO, "", null, null);
        }

        // Add all entries in a synchronized block to ensure they're added as a batch
        synchronized (buffer) {
            for (LogEntry entry : entries) {
                buffer.handleEntry(entry);
            }
        }

        // Wait for the model to stabilize
        waitForStableRowCount(5);

        assertEquals(5, model.getRowCount(), "Row count should be 5 after adding 5 entries");

        // Verify all entries are present
        for (int i = 0; i < 5; i++) {
            assertEquals(entries[i], model.getValueAt(i, 0), "Entry at row " + i + " should match");
        }
    }

    /**
     * Helper method to wait for the model to stabilize at the expected row count.
     * This waits for the count to be stable for a short period.
     */
    private void waitForStableRowCount(int expectedCount) throws InterruptedException {
        long startTime = System.currentTimeMillis();
        long timeout = 5000; // 5 second timeout
        long stableStartTime = -1;
        long stableTimeout = 100; // 100ms stable period

        while (true) {
            int currentCount = model.getRowCount();

            if (currentCount == expectedCount) {
                if (stableStartTime == -1) {
                    stableStartTime = System.currentTimeMillis();
                } else if (System.currentTimeMillis() - stableStartTime >= stableTimeout) {
                    // Count has been stable for the required period
                    return;
                }
            } else {
                stableStartTime = -1; // Reset stable timer
            }

            if (System.currentTimeMillis() - startTime > timeout) {
                throw new AssertionError("Timeout waiting for stable row count of " + expectedCount + ", current count: " + currentCount);
            }
            Thread.sleep(10); // Wait a bit before checking again
        }
    }
}