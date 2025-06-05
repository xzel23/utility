package com.dua3.utility.logging;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link LogBuffer} class.
 */
class LogBufferTest {

    private LogBuffer logBuffer;
    private static final int TEST_CAPACITY = 10;

    @BeforeEach
    void setUp() {
        logBuffer = new LogBuffer(TEST_CAPACITY);
    }

    @Test
    void testInitialState() {
        // Test initial state
        LogBuffer.BufferState state = logBuffer.getBufferState();
        assertEquals(0, state.entries().length, "Buffer should be empty initially");
        assertEquals(0, state.totalAdded(), "Total added should be 0 initially");
        assertEquals(0, state.totalRemoved(), "Total removed should be 0 initially");
    }

    @Test
    void testHandleEntry() {
        // Create a test log entry
        LogEntry entry = createTestLogEntry("Test message");

        // Add the entry to the buffer
        logBuffer.handleEntry(entry);

        // Verify the entry was added
        LogBuffer.BufferState state = logBuffer.getBufferState();
        assertEquals(1, state.entries().length, "Buffer should contain 1 entry");
        assertEquals(1, state.totalAdded(), "Total added should be 1");
        assertEquals(0, state.totalRemoved(), "Total removed should be 0");
        assertEquals(entry.message(), state.entries()[0].message(), "Entry message should match");
    }

    @Test
    void testCapacityLimit() {
        // Add more entries than the capacity
        for (int i = 0; i < TEST_CAPACITY + 5; i++) {
            logBuffer.handleEntry(createTestLogEntry("Message " + i));
        }

        // Verify that only the most recent entries are kept
        LogBuffer.BufferState state = logBuffer.getBufferState();
        assertEquals(TEST_CAPACITY, state.entries().length, "Buffer should contain exactly the capacity number of entries");
        assertEquals(TEST_CAPACITY + 5, state.totalAdded(), "Total added should be TEST_CAPACITY + 5");
        assertEquals(5, state.totalRemoved(), "Total removed should be 5");

        // Verify the entries are the most recent ones (the last TEST_CAPACITY entries)
        for (int i = 0; i < TEST_CAPACITY; i++) {
            assertEquals("Message " + (i + 5), state.entries()[i].message(), 
                    "Entry at index " + i + " should be Message " + (i + 5));
        }
    }

    @Test
    void testClear() {
        // Add some entries
        for (int i = 0; i < 5; i++) {
            logBuffer.handleEntry(createTestLogEntry("Message " + i));
        }

        // Clear the buffer
        logBuffer.clear();

        // Verify the buffer is empty
        LogBuffer.BufferState state = logBuffer.getBufferState();
        assertEquals(0, state.entries().length, "Buffer should be empty after clear");
        assertEquals(5, state.totalAdded(), "Total added should still be 5");
        assertEquals(5, state.totalRemoved(), "Total removed should be 5");
    }

    @Test
    void testLogBufferListener() {
        // Create a test listener
        AtomicInteger removedCount = new AtomicInteger(0);
        AtomicInteger addedCount = new AtomicInteger(0);
        AtomicInteger clearCount = new AtomicInteger(0);

        LogBuffer.LogBufferListener listener = new LogBuffer.LogBufferListener() {
            @Override
            public void entries(int removed, int added) {
                removedCount.addAndGet(removed);
                addedCount.addAndGet(added);
            }

            @Override
            public void clear() {
                clearCount.incrementAndGet();
            }
        };

        // Add the listener
        logBuffer.addLogBufferListener(listener);

        // Add some entries
        for (int i = 0; i < 5; i++) {
            logBuffer.handleEntry(createTestLogEntry("Message " + i));
        }

        // Verify the listener was notified
        assertEquals(0, removedCount.get(), "Removed count should be 0");
        assertEquals(5, addedCount.get(), "Added count should be 5");
        assertEquals(0, clearCount.get(), "Clear count should be 0");

        // Add more entries than capacity
        for (int i = 0; i < TEST_CAPACITY; i++) {
            logBuffer.handleEntry(createTestLogEntry("Message " + (i + 5)));
        }

        // Verify the listener was notified about removed entries
        assertEquals(5, removedCount.get(), "Removed count should be 5");
        assertEquals(15, addedCount.get(), "Added count should be 15");

        // Clear the buffer
        logBuffer.clear();

        // Verify the listener was notified about the clear
        assertEquals(1, clearCount.get(), "Clear count should be 1");

        // Remove the listener
        logBuffer.removeLogBufferListener(listener);

        // Add an entry and clear again
        logBuffer.handleEntry(createTestLogEntry("Test"));
        logBuffer.clear();

        // Verify the listener was not notified
        assertEquals(5, removedCount.get(), "Removed count should still be 5");
        assertEquals(15, addedCount.get(), "Added count should still be 15");
        assertEquals(1, clearCount.get(), "Clear count should still be 1");
    }

    @Test
    void testToArray() {
        // Add some entries
        List<LogEntry> entries = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            LogEntry entry = createTestLogEntry("Message " + i);
            entries.add(entry);
            logBuffer.handleEntry(entry);
        }

        // Get the array
        LogEntry[] array = logBuffer.toArray();

        // Verify the array
        assertEquals(5, array.length, "Array should contain 5 entries");
        for (int i = 0; i < 5; i++) {
            assertEquals(entries.get(i).message(), array[i].message(), 
                    "Entry at index " + i + " should match the original entry");
        }
    }

    @Test
    void testGet() {
        // Add some entries
        List<LogEntry> entries = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            LogEntry entry = createTestLogEntry("Message " + i);
            entries.add(entry);
            logBuffer.handleEntry(entry);
        }

        // Get entries by index
        for (int i = 0; i < 5; i++) {
            LogEntry entry = logBuffer.get(i);
            assertEquals(entries.get(i).message(), entry.message(), 
                    "Entry at index " + i + " should match the original entry");
        }

        // Test index out of bounds
        assertThrows(IndexOutOfBoundsException.class, () -> logBuffer.get(5), 
                "Should throw IndexOutOfBoundsException for invalid index");
    }

    @Test
    void testSubList() {
        // Add some entries
        for (int i = 0; i < 10; i++) {
            logBuffer.handleEntry(createTestLogEntry("Message " + i));
        }

        // Get a sublist
        List<LogEntry> subList = logBuffer.subList(2, 7);

        // Verify the sublist
        assertEquals(5, subList.size(), "Sublist should contain 5 entries");
        for (int i = 0; i < 5; i++) {
            assertEquals("Message " + (i + 2), subList.get(i).message(), 
                    "Entry at index " + i + " should be Message " + (i + 2));
        }

        // Test invalid indices
        assertThrows(IndexOutOfBoundsException.class, () -> logBuffer.subList(-1, 5), 
                "Should throw IndexOutOfBoundsException for negative fromIndex");
        assertThrows(IndexOutOfBoundsException.class, () -> logBuffer.subList(5, 11), 
                "Should throw IndexOutOfBoundsException for toIndex > size");
        assertThrows(IndexOutOfBoundsException.class, () -> logBuffer.subList(7, 5), 
                "Should throw IndexOutOfBoundsException for fromIndex > toIndex");
    }

    /**
     * Helper method to create a test log entry.
     */
    private LogEntry createTestLogEntry(String message) {
        return new SimpleLogEntry(
                message,
                "TestLogger",
                Instant.now(),
                LogLevel.INFO,
                "",  // Empty string instead of null for marker
                null,
                null
        );
    }
}
