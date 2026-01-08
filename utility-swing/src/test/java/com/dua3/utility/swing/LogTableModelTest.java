package com.dua3.utility.swing;

import com.dua3.utility.logging.LogBuffer;
import com.dua3.utility.logging.LogEntry;
import com.dua3.utility.logging.LogLevel;
import com.dua3.utility.logging.SimpleLogEntry;
import org.junit.jupiter.api.AfterEach;
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
        buffer = new LogBuffer("LogTableModelTest", 100);
        model = new LogTableModel(buffer);
    }

    @AfterEach
    void tearDown() {
        model.shutdown();
        buffer = null;
        model = null;
    }

    @Test
    void testInitialState() {
        assertEquals(0, model.getRowCount(), "Initial row count should be 0");
        assertEquals(SwingLogPane.COLUMNS.length, model.getColumnCount(), "Column count should match SwingLogPane.COLUMNS length");
    }

    @Test
    void testAddLogEntry() {
        // Add a log entry to the buffer
        LogEntry entry = new SimpleLogEntry(Instant.now(), "test.logger", LogLevel.INFO, "", "Test message", "", null);
        buffer.handle(
                entry.time(),
                entry.loggerName(),
                entry.level(),
                entry.marker(),
                entry::message,
                entry.location(),
                entry.throwable()
        );

        // Wait for the model to update asynchronously
        waitForRowCount(1);

        assertEquals(1, model.getRowCount(), "Row count should be 1 after adding an entry");
        assertEquals(entry, model.getValueAt(0, 0), "The entry at row 0 should be the one we added");
    }

    /**
     * Helper method to wait for the model to reach the expected row count.
     * This is necessary because the LogTableModel updates asynchronously.
     */
    private void waitForRowCount(int expectedCount) {
        long startTime = System.currentTimeMillis();
        long timeout = 10000;

        while (model.getRowCount() != expectedCount) {
            if (System.currentTimeMillis() - startTime > timeout) {
                throw new AssertionError("Timeout waiting for row count to reach " + expectedCount + ", current count: " + model.getRowCount());
            }
        }
    }

    @Test
    void testClearBuffer() {
        // Add some log entries one by one with small delays to ensure proper processing
        for (int i = 0; i < 5; i++) {
            LogEntry entry = new SimpleLogEntry(Instant.now(), "test.logger", LogLevel.INFO, "", "Test message " + i, "", null);
            buffer.handle(
                    entry.time(),
                    entry.loggerName(),
                    entry.level(),
                    entry.marker(),
                    entry::message,
                    entry.location(),
                    entry.throwable()
            );
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
            assertSame(SwingLogPane.SimpleLogEntryField.class, columnClass, "Column class should be SimpleLogEntryField");
        }
    }

    @Test
    void testMultipleEntries() {
        // Add multiple log entries with different levels
        LogEntry entry1 = new SimpleLogEntry(Instant.now(), "test.logger", LogLevel.INFO, "", "Info message", "", null);
        LogEntry entry2 = new SimpleLogEntry(Instant.now(), "test.logger", LogLevel.WARN, "", "Warning message", "", null);
        LogEntry entry3 = new SimpleLogEntry(Instant.now(), "test.logger", LogLevel.ERROR, "", "Error message", "", null);

        buffer.handle(
                entry1.time(),
                entry1.loggerName(),
                entry1.level(),
                entry1.marker(),
                entry1::message,
                entry1.location(),
                entry1.throwable()
        );
        buffer.handle(
                entry2.time(),
                entry2.loggerName(),
                entry2.level(),
                entry2.marker(),
                entry2::message,
                entry2.location(),
                entry2.throwable()
        );
        buffer.handle(
                entry3.time(),
                entry3.loggerName(),
                entry3.level(),
                entry3.marker(),
                entry3::message,
                entry3.location(),
                entry3.throwable()
        );

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
    void testBatchAddEntries() {
        // Add all entries at once
        LogEntry[] entries = new LogEntry[5];
        for (int i = 0; i < 5; i++) {
            entries[i] = new SimpleLogEntry(Instant.now(), "test.logger", LogLevel.INFO, "", "Test message " + i, "", null);
        }

        // Add all entries in a synchronized block to ensure they're added as a batch
        for (LogEntry entry : entries) {
            buffer.handle(
                    entry.time(),
                    entry.loggerName(),
                    entry.level(),
                    entry.marker(),
                    entry::message,
                    entry.location(),
                    entry.throwable()
            );
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
    private void waitForStableRowCount(int expectedCount) {
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
        }
    }
}