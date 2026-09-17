package com.dua3.utility.concurrent;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TaskProcessorBaseTest {

    private static class TestTaskProcessor extends TaskProcessorBase {
        TestTaskProcessor(String name) {
            super(name);
        }

        @Override
        public <T> CompletableFuture<T> submit(Callable<? extends T> task) {
            long id = nextId();
            registerId(id);
            CompletableFuture<T> cf = new CompletableFuture<>();
            try {
                T res = task.call();
                cf.complete(res);
            } catch (Exception e) {
                cf.completeExceptionally(e);
            } finally {
                unregisterId(id);
            }
            return cf;
        }

        public void publicRegisterParty(String party) {
            registerParty(party);
        }

        public void publicUnregisterParty(String party) {
            unregisterParty(party);
        }
    }

    @Test
    void testProcessorLifecycleAndStats() {
        TestTaskProcessor processor = new TestTaskProcessor("lifecycle-proc");
        assertEquals("lifecycle-proc", processor.getName());

        assertEquals(TaskProcessorBase.State.RUNNING, processor.getState());
        TaskProcessorBase.Stats initialStats = processor.getStats();
        assertSame(processor, initialStats.owner());
        assertEquals(0, initialStats.submitted());
        assertEquals(0, initialStats.completed());
        assertEquals(TaskProcessorBase.State.RUNNING, initialStats.state());

        processor.submit(() -> 42);

        TaskProcessorBase.Stats afterTaskStats = processor.getStats();
        assertEquals(1, afterTaskStats.submitted());
        assertEquals(1, afterTaskStats.completed());

        processor.shutdown();
        assertTrue(processor.isShutdown());
        assertEquals(TaskProcessorBase.State.SHUTDOWN, processor.getState());

        assertTrue(processor.waitForCompletion(1, TimeUnit.SECONDS));
        assertTrue(processor.isCompleted());
        assertEquals(TaskProcessorBase.State.COMLETED, processor.getState());

        TaskProcessorBase.Stats completedStats = processor.getStats();
        assertEquals(TaskProcessorBase.State.COMLETED, completedStats.state());
    }

    @Test
    void testListenersNotification() {
        TestTaskProcessor processor = new TestTaskProcessor("listener-proc");
        List<TaskProcessorBase.Stats> statHistory = new ArrayList<>();

        Consumer<TaskProcessor> listener = p -> statHistory.add(((TaskProcessorBase) p).getStats());
        processor.addListener(listener);

        processor.submit(() -> "ok");

        // Submit registers ID (1st notification) and unregisters ID (2nd notification)
        assertEquals(2, statHistory.size());
        assertEquals(1, statHistory.get(0).submitted());
        assertEquals(0, statHistory.get(0).completed());
        assertEquals(1, statHistory.get(1).submitted());
        assertEquals(1, statHistory.get(1).completed());

        // Remove listener
        processor.removeListener(listener);
        processor.submit(() -> "ok2");
        assertEquals(2, statHistory.size()); // No new events added
    }

    @Test
    void testListenerThrowingExceptionIsHandledGracefully() {
        TestTaskProcessor processor = new TestTaskProcessor("throwing-listener-proc");
        Consumer<TaskProcessor> badListener = p -> {
            throw new RuntimeException("Listener error");
        };

        processor.addListener(badListener);

        // Submitting should succeed despite bad listener
        assertDoesNotThrow(() -> processor.submit(() -> "success"));
    }

    @Test
    void testDoubleShutdownThrows() {
        TestTaskProcessor processor = new TestTaskProcessor("double-shutdown");
        processor.shutdown();

        assertThrows(IllegalStateException.class, processor::shutdown);
    }

    @Test
    void testDoubleWaitForCompletionThrows() {
        TestTaskProcessor processor = new TestTaskProcessor("double-wait");
        processor.shutdown();
        assertTrue(processor.waitForCompletion(1, TimeUnit.SECONDS));

        assertThrows(IllegalStateException.class, () -> processor.waitForCompletion(1, TimeUnit.SECONDS));
    }

    @Test
    void testEnsureOpenAndEnsureClosed() {
        TestTaskProcessor processor = new TestTaskProcessor("open-closed-check");
        assertDoesNotThrow(processor::ensureOpen);
        assertThrows(IllegalStateException.class, processor::ensureClosed);

        processor.shutdown();

        assertThrows(IllegalStateException.class, processor::ensureOpen);
        assertDoesNotThrow(processor::ensureClosed);
    }

    @Test
    void testSubmitRunnable() {
        TestTaskProcessor processor = new TestTaskProcessor("runnable-proc");
        AtomicBoolean executed = new AtomicBoolean(false);

        processor.submit(() -> executed.set(true));
        assertTrue(executed.get());

        // Submitting throwing runnable is caught and logged
        assertDoesNotThrow(() -> processor.submit(() -> {
            throw new Exception("Runnable failed");
        }));
    }

    @Test
    void testRegisterAndUnregisterParty() {
        TestTaskProcessor processor = new TestTaskProcessor("party-proc");

        processor.publicRegisterParty("party1");
        processor.publicUnregisterParty("party1");

        processor.shutdown();
        assertTrue(processor.waitForCompletion(1, TimeUnit.SECONDS));
    }
}
