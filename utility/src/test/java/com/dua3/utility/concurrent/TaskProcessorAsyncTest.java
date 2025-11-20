package com.dua3.utility.concurrent;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;

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

        Assertions.assertEquals(42, f.get(1, TimeUnit.SECONDS));

        Assertions.assertFalse(processor.isShutdown());
        processor.shutdown();
        Assertions.assertTrue(processor.isShutdown());
        // as implemented, this returns quickly and marks completed
        Assertions.assertTrue(processor.waitForCompletion(200, TimeUnit.MILLISECONDS));
        Assertions.assertTrue(processor.isCompleted());
    }

    @Test
    void testExceptionPropagation() {
        processor = new TaskProcessorAsync("async-ex", 1);

        CompletableFuture<Void> f = processor.submit(() -> { throw new IllegalStateException("boom"); });

        CompletionException ex = Assertions.assertThrows(CompletionException.class, f::join);
        Assertions.assertNotNull(ex.getCause());
        Assertions.assertTrue(ex.getCause() instanceof IllegalStateException);
        Assertions.assertEquals("boom", ex.getCause().getMessage());
    }

    @Test
    void testSubmitAfterShutdownThrows() {
        processor = new TaskProcessorAsync("async-shutdown", 1);
        processor.shutdown();
        Assertions.assertThrows(IllegalStateException.class, () -> processor.submit(() -> 1));
    }

    @Test
    void testShutdownAndAbortWhenNoTasks() {
        processor = new TaskProcessorAsync("async-abort-none", 1);

        Assertions.assertDoesNotThrow(() -> processor.shutdownAndAbort());
        Assertions.assertTrue(processor.isShutdown());
        Assertions.assertTrue(processor.waitForCompletion(200, TimeUnit.MILLISECONDS));
        Assertions.assertTrue(processor.isCompleted());

        Assertions.assertThrows(IllegalStateException.class, () -> processor.submit(() -> 1));
    }

    @Test
    void testShutdownAndAbortAfterCompletedTasks() throws Exception {
        processor = new TaskProcessorAsync("async-abort-completed", 2);

        CompletableFuture<Integer> f1 = processor.submit(() -> 1);
        CompletableFuture<Integer> f2 = processor.submit(() -> 2);
        Assertions.assertEquals(1, f1.get(1, TimeUnit.SECONDS));
        Assertions.assertEquals(2, f2.get(1, TimeUnit.SECONDS));

        processor.shutdownAndAbort();
        Assertions.assertTrue(processor.isShutdown());
        Assertions.assertTrue(processor.waitForCompletion(200, TimeUnit.MILLISECONDS));
        Assertions.assertTrue(processor.isCompleted());

        Assertions.assertThrows(IllegalStateException.class, () -> processor.submit(() -> 3));
    }

    @Test
    void testShutdownAndAbortWhileTaskRunning() {
        processor = new TaskProcessorAsync("async-abort-running", 1);

        CompletableFuture<Integer> longTask = processor.submit(() -> {
            try {
                Thread.sleep(5_000);
                return 42;
            } catch (InterruptedException e) {
                // propagate as runtime to be wrapped by CompletionException in executor path
                throw new RuntimeException(e);
            }
        });

        Assertions.assertDoesNotThrow(() -> processor.shutdownAndAbort());

        CompletionException ex = Assertions.assertThrows(CompletionException.class, longTask::join);
        Assertions.assertNotNull(ex.getCause());

        Assertions.assertTrue(processor.waitForCompletion(1_000, TimeUnit.MILLISECONDS));
        Assertions.assertTrue(processor.isCompleted());
    }
}
