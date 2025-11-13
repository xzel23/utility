package com.dua3.utility.concurrent;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class TaskProcessorAsyncTest {

    private TaskProcessorAsync processor;

    @AfterEach
    void tearDown() {
        if (processor != null && !processor.isShutdown()) {
            processor.shutdown();
        }
        if (processor != null && !processor.isCompleted()) {
            // trigger completion path in TaskProcessorAsync which also shuts down the executor
            processor.waitForCompletion(100, TimeUnit.MILLISECONDS);
        }
    }

    @Test
    void testSubmitReturnsResult() throws Exception {
        processor = new TaskProcessorAsync("async", 2);

        CompletableFuture<Integer> f = processor.submit(() -> 40 + 2);

        assertEquals(42, f.get(1, TimeUnit.SECONDS));

        assertFalse(processor.isShutdown());
        processor.shutdown();
        assertTrue(processor.isShutdown());
        // as implemented, this returns quickly and marks completed
        assertTrue(processor.waitForCompletion(200, TimeUnit.MILLISECONDS));
        assertTrue(processor.isCompleted());
    }

    @Test
    void testExceptionPropagation() {
        processor = new TaskProcessorAsync("async-ex", 1);

        CompletableFuture<Void> f = processor.submit(() -> { throw new IllegalStateException("boom"); });

        CompletionException ex = assertThrows(CompletionException.class, f::join);
        assertNotNull(ex.getCause());
        assertTrue(ex.getCause() instanceof IllegalStateException);
        assertEquals("boom", ex.getCause().getMessage());
    }

    @Test
    void testSubmitAfterShutdownThrows() {
        processor = new TaskProcessorAsync("async-shutdown", 1);
        processor.shutdown();
        assertThrows(IllegalStateException.class, () -> processor.submit(() -> 1));
    }
}
