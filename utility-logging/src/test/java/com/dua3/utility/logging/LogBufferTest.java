package com.dua3.utility.logging;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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

    @Test
    void testWriteExternalEmptyBuffer() throws IOException {
        // Test serializing an empty buffer
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);

        // Write the empty buffer
        logBuffer.writeExternal(oos);
        oos.flush();

        // Verify the serialized data
        byte[] serializedData = baos.toByteArray();
        assertNotNull(serializedData, "Serialized data should not be null");

        // Verify the first 4 bytes represent an integer with value 0 (number of entries)
        ByteArrayInputStream bais = new ByteArrayInputStream(serializedData);
        ObjectInputStream ois = new ObjectInputStream(bais);
        assertEquals(0, ois.readInt(), "Number of entries should be 0");

        ois.close();
        oos.close();
    }

    @Test
    void testWriteExternalWithEntries() throws IOException {
        // Add some entries to the buffer
        for (int i = 0; i < 5; i++) {
            logBuffer.handleEntry(createTestLogEntry("Message " + i));
        }

        // Serialize the buffer
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);

        logBuffer.writeExternal(oos);
        oos.flush();

        // Verify the serialized data
        byte[] serializedData = baos.toByteArray();
        assertNotNull(serializedData, "Serialized data should not be null");

        // Verify the first 4 bytes represent an integer with value 5 (number of entries)
        ByteArrayInputStream bais = new ByteArrayInputStream(serializedData);
        ObjectInputStream ois = new ObjectInputStream(bais);
        assertEquals(5, ois.readInt(), "Number of entries should be 5");

        // We could read more data, but that's tested in the full cycle test

        ois.close();
        oos.close();
    }

    @Test
    void testReadExternalEmptyBuffer() throws IOException, ClassNotFoundException {
        // Create a serialized representation of an empty buffer
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeInt(0); // No entries
        oos.flush();

        // Create a new buffer and deserialize into it
        LogBuffer newBuffer = new LogBuffer();
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);

        newBuffer.readExternal(ois);

        // Verify the buffer state
        LogBuffer.BufferState state = newBuffer.getBufferState();
        assertEquals(0, state.entries().length, "Buffer should be empty");
        assertEquals(0, state.totalAdded(), "Total added should be 0");
        assertEquals(0, state.totalRemoved(), "Total removed should be 0");

        ois.close();
        oos.close();
    }

    @Test
    void testReadExternalWithEntries() throws IOException, ClassNotFoundException {
        // Create a serialized representation of a buffer with entries
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);

        // Write 3 entries
        oos.writeInt(3);

        for (int i = 0; i < 3; i++) {
            oos.writeObject("Message " + i); // message
            oos.writeObject("TestLogger"); // loggerName
            Instant now = Instant.now();
            oos.writeObject(now); // time
            oos.writeObject(LogLevel.INFO); // level
            oos.writeObject(""); // marker
            oos.writeObject("TestLocation"); // location
            oos.writeBoolean(false); // no throwable
        }

        oos.flush();

        // Create a new buffer and deserialize into it
        LogBuffer newBuffer = new LogBuffer();
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);

        newBuffer.readExternal(ois);

        // Verify the buffer state
        LogBuffer.BufferState state = newBuffer.getBufferState();
        assertEquals(3, state.entries().length, "Buffer should have 3 entries");
        assertEquals(3, state.totalAdded(), "Total added should be 3");
        assertEquals(0, state.totalRemoved(), "Total removed should be 0");

        // Verify entry content
        for (int i = 0; i < 3; i++) {
            assertEquals("Message " + i, state.entries()[i].message(), "Entry message should match");
            assertEquals("TestLogger", state.entries()[i].loggerName(), "Entry logger name should match");
            assertEquals(LogLevel.INFO, state.entries()[i].level(), "Entry level should match");
            assertEquals("", state.entries()[i].marker(), "Entry marker should match");
            assertEquals("TestLocation", state.entries()[i].location(), "Entry location should match");
            assertNull(state.entries()[i].throwable(), "Entry throwable should be null");
        }

        ois.close();
        oos.close();
    }

    @Test
    void testSerializationCycle() throws IOException, ClassNotFoundException {
        // Add some entries to the buffer
        for (int i = 0; i < 5; i++) {
            logBuffer.handleEntry(createTestLogEntry("Message " + i));
        }

        // Get the original buffer state
        LogBuffer.BufferState originalState = logBuffer.getBufferState();

        // Serialize the buffer
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        logBuffer.writeExternal(oos);
        oos.flush();

        // Deserialize into a new buffer
        LogBuffer newBuffer = new LogBuffer();
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        newBuffer.readExternal(ois);

        // Get the new buffer state
        LogBuffer.BufferState newState = newBuffer.getBufferState();

        // Verify the states are equivalent
        assertEquals(originalState.entries().length, newState.entries().length, "Number of entries should match");
        assertEquals(originalState.totalAdded(), newState.totalAdded(), "Total added should match");
        assertEquals(originalState.totalRemoved(), newState.totalRemoved(), "Total removed should match");

        // Verify each entry
        for (int i = 0; i < originalState.entries().length; i++) {
            LogEntry originalEntry = originalState.entries()[i];
            LogEntry newEntry = newState.entries()[i];

            assertEquals(originalEntry.message(), newEntry.message(), "Entry message should match");
            assertEquals(originalEntry.loggerName(), newEntry.loggerName(), "Entry logger name should match");
            assertEquals(originalEntry.time(), newEntry.time(), "Entry time should match");
            assertEquals(originalEntry.level(), newEntry.level(), "Entry level should match");
            assertEquals(originalEntry.marker(), newEntry.marker(), "Entry marker should match");
            assertEquals(originalEntry.location(), newEntry.location(), "Entry location should match");

            // For throwable, we can only check if both are null or both are non-null
            if (originalEntry.throwable() == null) {
                assertNull(newEntry.throwable(), "Entry throwable should be null");
            } else {
                assertNotNull(newEntry.throwable(), "Entry throwable should not be null");
            }
        }

        ois.close();
        oos.close();
    }

    @Test
    void testSerializationWithThrowable() throws IOException, ClassNotFoundException {
        // Create an entry with a throwable
        RuntimeException exception = new RuntimeException("Test exception");
        LogEntry entryWithThrowable = new SimpleLogEntry(
                "Exception message",
                "TestLogger",
                Instant.now(),
                LogLevel.ERROR,
                "ERROR_MARKER",
                exception,
                "TestLocation"
        );

        // Add the entry to the buffer
        logBuffer.handleEntry(entryWithThrowable);

        // Serialize the buffer
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        logBuffer.writeExternal(oos);
        oos.flush();

        // Deserialize into a new buffer
        LogBuffer newBuffer = new LogBuffer();
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        newBuffer.readExternal(ois);

        // Get the new buffer state
        LogBuffer.BufferState newState = newBuffer.getBufferState();

        // Verify the entry with throwable
        assertEquals(1, newState.entries().length, "Buffer should have 1 entry");
        LogEntry newEntry = newState.entries()[0];

        assertEquals("Exception message", newEntry.message(), "Entry message should match");
        assertEquals("TestLogger", newEntry.loggerName(), "Entry logger name should match");
        assertEquals(LogLevel.ERROR, newEntry.level(), "Entry level should match");
        assertEquals("ERROR_MARKER", newEntry.marker(), "Entry marker should match");
        assertEquals("TestLocation", newEntry.location(), "Entry location should match");

        // Verify the throwable was deserialized
        assertNotNull(newEntry.throwable(), "Entry throwable should not be null");
        assertTrue(newEntry.throwable() instanceof RuntimeException, "Throwable should be a RuntimeException");
        assertTrue(newEntry.throwable().getMessage().contains("Test exception"), 
                "Throwable message should contain the original exception message");

        ois.close();
        oos.close();
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
