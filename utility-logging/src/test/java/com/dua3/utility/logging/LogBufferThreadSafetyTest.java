package com.dua3.utility.logging;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Thread-safety tests for the {@link LogBuffer} class.
 */
class LogBufferThreadSafetyTest {

    private static final int BUFFER_CAPACITY = 1000;
    private static final int NUM_THREADS = 10;
    private static final int ENTRIES_PER_THREAD = 1000;

    /**
     * Test concurrent adding of entries to the buffer.
     * This test creates multiple threads that concurrently add entries to the buffer
     * and verifies that all entries are properly accounted for.
     */
    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testConcurrentAdding() throws InterruptedException {
        LogBuffer logBuffer = new LogBuffer(BUFFER_CAPACITY);

        // Create a listener to track added entries
        AtomicInteger listenerAddedCount = new AtomicInteger(0);
        AtomicInteger listenerRemovedCount = new AtomicInteger(0);

        LogBuffer.LogBufferListener listener = new LogBuffer.LogBufferListener() {
            @Override
            public void entries(int removed, int added) {
                listenerRemovedCount.addAndGet(removed);
                listenerAddedCount.addAndGet(added);
            }

            @Override
            public void clear() {
                // Not used in this test
            }
        };

        logBuffer.addLogBufferListener(listener);

        // Create and start threads
        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(NUM_THREADS);

        for (int threadId = 0; threadId < NUM_THREADS; threadId++) {
            final int id = threadId;
            executor.submit(() -> {
                try {
                    // Wait for all threads to be ready
                    startLatch.await();

                    // Add entries
                    for (int i = 0; i < ENTRIES_PER_THREAD; i++) {
                        LogEntry entry = createTestLogEntry("Thread " + id + " Message " + i);
                        logBuffer.handleEntry(entry);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        // Start all threads simultaneously
        startLatch.countDown();

        // Wait for all threads to complete
        doneLatch.await();
        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));

        // Verify the buffer state
        LogBuffer.BufferState state = logBuffer.getBufferState();
        int totalEntries = NUM_THREADS * ENTRIES_PER_THREAD;
        int expectedInBuffer = Math.min(BUFFER_CAPACITY, totalEntries);
        int expectedRemoved = Math.max(0, totalEntries - BUFFER_CAPACITY);

        assertEquals(expectedInBuffer, state.entries().length, 
                "Buffer should contain the expected number of entries");
        assertEquals(totalEntries, state.totalAdded(), 
                "Total added should match the number of entries added");
        assertEquals(expectedRemoved, state.totalRemoved(), 
                "Total removed should match the expected number of removed entries");

        // Verify listener counts
        assertEquals(totalEntries, listenerAddedCount.get(), 
                "Listener added count should match total entries added");
        assertEquals(expectedRemoved, listenerRemovedCount.get(), 
                "Listener removed count should match expected removed entries");
    }

    /**
     * Test concurrent adding and clearing of the buffer.
     * This test creates threads that add entries while another thread periodically clears the buffer.
     */
    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testConcurrentAddingAndClearing() throws InterruptedException {
        LogBuffer logBuffer = new LogBuffer(BUFFER_CAPACITY);

        // Track clear operations
        AtomicInteger clearCount = new AtomicInteger(0);

        LogBuffer.LogBufferListener listener = new LogBuffer.LogBufferListener() {
            @Override
            public void entries(int removed, int added) {
                // Not used in this test
            }

            @Override
            public void clear() {
                clearCount.incrementAndGet();
            }
        };

        logBuffer.addLogBufferListener(listener);

        // Flag to signal threads to stop
        AtomicBoolean running = new AtomicBoolean(true);

        // Create and start producer threads
        ExecutorService producerExecutor = Executors.newFixedThreadPool(NUM_THREADS);
        CountDownLatch producersDone = new CountDownLatch(NUM_THREADS);

        for (int threadId = 0; threadId < NUM_THREADS; threadId++) {
            final int id = threadId;
            producerExecutor.submit(() -> {
                try {
                    int counter = 0;
                    while (running.get() && counter < ENTRIES_PER_THREAD) {
                        LogEntry entry = createTestLogEntry("Thread " + id + " Message " + counter);
                        logBuffer.handleEntry(entry);
                        counter++;

                        // Small delay to reduce contention
                        if (counter % 100 == 0) {
                            Thread.yield();
                        }
                    }
                } finally {
                    producersDone.countDown();
                }
            });
        }

        // Create and start a clearer thread
        ExecutorService clearerExecutor = Executors.newSingleThreadExecutor();
        CountDownLatch clearerDone = new CountDownLatch(1);

        clearerExecutor.submit(() -> {
            try {
                int clearOperations = 0;
                while (running.get() && clearOperations < 10) {
                    // Wait a bit before clearing
                    Thread.sleep(50);

                    // Clear the buffer
                    logBuffer.clear();
                    clearOperations++;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                clearerDone.countDown();
            }
        });

        // Let threads run for a short time
        Thread.sleep(500);
        running.set(false);

        // Wait for all threads to complete
        producersDone.await(5, TimeUnit.SECONDS);
        clearerDone.await(5, TimeUnit.SECONDS);

        producerExecutor.shutdown();
        clearerExecutor.shutdown();

        assertTrue(producerExecutor.awaitTermination(30, TimeUnit.SECONDS));
        assertTrue(clearerExecutor.awaitTermination(30, TimeUnit.SECONDS));

        // Verify that clear operations were performed and counted by the listener
        assertTrue(clearCount.get() > 0, "Clear operations should have been performed");

        // Final buffer state should be consistent
        LogBuffer.BufferState state = logBuffer.getBufferState();
        assertTrue(state.entries().length <= BUFFER_CAPACITY, 
                "Buffer should not exceed capacity");
        assertTrue(state.totalAdded() >= state.entries().length, 
                "Total added should be at least the number of entries in the buffer");
        assertEquals(state.totalAdded() - state.entries().length, state.totalRemoved(), 
                "Total removed should be consistent with total added and current size");
    }

    /**
     * Test concurrent reading and writing to the buffer.
     * This test creates threads that add entries while other threads read from the buffer.
     */
    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testConcurrentReadingAndWriting() throws InterruptedException {
        LogBuffer logBuffer = new LogBuffer(BUFFER_CAPACITY);

        // Flag to signal threads to stop
        AtomicBoolean running = new AtomicBoolean(true);

        // Track any exceptions in reader threads
        List<Throwable> readerExceptions = new ArrayList<>();

        // Create and start writer threads
        ExecutorService writerExecutor = Executors.newFixedThreadPool(NUM_THREADS / 2);
        CountDownLatch writersDone = new CountDownLatch(NUM_THREADS / 2);

        for (int threadId = 0; threadId < NUM_THREADS / 2; threadId++) {
            final int id = threadId;
            writerExecutor.submit(() -> {
                try {
                    int counter = 0;
                    while (running.get() && counter < ENTRIES_PER_THREAD) {
                        LogEntry entry = createTestLogEntry("Thread " + id + " Message " + counter);
                        logBuffer.handleEntry(entry);
                        counter++;

                        // Small delay to reduce contention
                        if (counter % 100 == 0) {
                            Thread.yield();
                        }
                    }
                } finally {
                    writersDone.countDown();
                }
            });
        }

        // Create and start reader threads
        ExecutorService readerExecutor = Executors.newFixedThreadPool(NUM_THREADS / 2);
        CountDownLatch readersDone = new CountDownLatch(NUM_THREADS / 2);

        for (int threadId = 0; threadId < NUM_THREADS / 2; threadId++) {
            readerExecutor.submit(() -> {
                try {
                    Random random = new Random();
                    while (running.get()) {
                        // Perform different read operations
                        try {
                            // Get buffer state
                            LogBuffer.BufferState state = logBuffer.getBufferState();

                            // If buffer has entries, try different read operations
                            if (state.entries().length > 0) {
                                // Get a random entry
                                int randomIndex = random.nextInt(state.entries().length);
                                LogEntry entry = logBuffer.get(randomIndex);
                                assertNotNull(entry, "Entry should not be null");

                                // Get a sublist if possible
                                if (state.entries().length > 2) {
                                    int fromIndex = randomIndex / 2;
                                    int toIndex = Math.min(fromIndex + 2, state.entries().length);
                                    List<LogEntry> subList = logBuffer.subList(fromIndex, toIndex);
                                    assertNotNull(subList, "Sublist should not be null");
                                    assertTrue(subList.size() <= toIndex - fromIndex, 
                                            "Sublist size should be consistent");
                                }

                                // Convert to array
                                LogEntry[] array = logBuffer.toArray();
                                assertEquals(state.entries().length, array.length, 
                                        "Array length should match buffer size");
                            }

                            // Small delay between read operations
                            Thread.sleep(5);
                        } catch (Exception e) {
                            // Capture any exceptions
                            synchronized (readerExceptions) {
                                readerExceptions.add(e);
                            }
                        }
                    }
                } catch (Exception e) {
                    synchronized (readerExceptions) {
                        readerExceptions.add(e);
                    }
                } finally {
                    readersDone.countDown();
                }
            });
        }

        // Let threads run for a short time
        Thread.sleep(1000);
        running.set(false);

        // Wait for all threads to complete
        writersDone.await(5, TimeUnit.SECONDS);
        readersDone.await(5, TimeUnit.SECONDS);

        writerExecutor.shutdown();
        readerExecutor.shutdown();

        assertTrue(writerExecutor.awaitTermination(5, TimeUnit.SECONDS));
        assertTrue(readerExecutor.awaitTermination(5, TimeUnit.SECONDS));

        // Verify no exceptions occurred in reader threads
        if (!readerExceptions.isEmpty()) {
            fail("Reader threads encountered exceptions: " + readerExceptions);
        }

        // Final buffer state should be consistent
        LogBuffer.BufferState state = logBuffer.getBufferState();
        assertTrue(state.entries().length <= BUFFER_CAPACITY, 
                "Buffer should not exceed capacity");
        assertTrue(state.totalAdded() >= state.entries().length, 
                "Total added should be at least the number of entries in the buffer");
        assertEquals(state.totalAdded() - state.entries().length, state.totalRemoved(), 
                "Total removed should be consistent with total added and current size");
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
