package com.dua3.utility.concurrent;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

class TaskProcessorEventDrivenTest {

    private TaskProcessorEventDriven<String> processor;

    @AfterEach
    void tearDown() {
        if (processor != null && !processor.isShutdown()) {
            processor.shutdown();
        }
        if (processor != null && !processor.isCompleted()) {
            processor.waitForCompletion(50, TimeUnit.MILLISECONDS);
        }
    }

    @Test
    void testSubmitAndCompleteViaUpdate() throws Exception {
        Map<Object, String> registry = new ConcurrentHashMap<>();
        processor = new TaskProcessorEventDriven<>(
                "ev",
                registry::get
        );

        CallableWithId<Integer> task = new CallableWithId<>("A", () -> 21 + 21);
        // register mapping for external key
        registry.put(task, task.id());

        CompletableFuture<Integer> future = processor.submit(task);
        Assertions.assertFalse(future.isDone());

        // non-completion update must not finish the future
        processor.updateTask(task.id(), 0, false);
        Assertions.assertFalse(future.isDone());

        // now complete
        processor.updateTask(task.id(), 42, true);
        Assertions.assertEquals(42, future.get(1, TimeUnit.SECONDS));
    }

    @Test
    void testUnknownKeyUpdateIsIgnored() {
        Map<Object, String> registry = new ConcurrentHashMap<>();
        processor = new TaskProcessorEventDriven<>("ev2", registry::get);

        // updating unknown key must not throw
        Assertions.assertDoesNotThrow(() -> processor.updateTask("unknown", 1, true));
    }

    @Test
    void testSubmitAfterShutdownThrows() {
        Map<Object, String> registry = new ConcurrentHashMap<>();
        processor = new TaskProcessorEventDriven<>("ev3", registry::get);
        processor.shutdown();
        CallableWithId<Integer> task = new CallableWithId<>("K", () -> 1);
        registry.put(task, task.id());
        Assertions.assertThrows(IllegalStateException.class, () -> processor.submit(task));
    }

    @Test
    void testShutdownAndAbortWhenNoTasks() {
        Map<Object, String> registry = new ConcurrentHashMap<>();
        processor = new TaskProcessorEventDriven<>("ev-abort-none", registry::get);

        Assertions.assertDoesNotThrow(() -> processor.shutdownAndAbort());
        Assertions.assertTrue(processor.isShutdown());
        Assertions.assertTrue(processor.waitForCompletion(200, TimeUnit.MILLISECONDS));
        Assertions.assertTrue(processor.isCompleted());
        CallableWithId<Integer> task = new CallableWithId<>("K1", () -> 1);
        registry.put(task, task.id());
        Assertions.assertThrows(IllegalStateException.class, () -> processor.submit(task));
    }

    @Test
    void testShutdownAndAbortAfterCompletedTasks() throws Exception {
        Map<Object, String> registry = new ConcurrentHashMap<>();
        processor = new TaskProcessorEventDriven<>("ev-abort-completed", registry::get);

        CallableWithId<Integer> task = new CallableWithId<>("C", () -> 7);
        registry.put(task, task.id());
        CompletableFuture<Integer> cf = processor.submit(task);

        // complete via external update first
        processor.updateTask(task.id(), 7, true);
        Assertions.assertEquals(7, cf.get(1, TimeUnit.SECONDS));

        // then abort (should be no-op regarding already completed tasks)
        Assertions.assertDoesNotThrow(() -> processor.shutdownAndAbort());
        Assertions.assertTrue(processor.isShutdown());
        Assertions.assertTrue(processor.waitForCompletion(200, TimeUnit.MILLISECONDS));
        Assertions.assertTrue(processor.isCompleted());
        CallableWithId<Integer> t2 = new CallableWithId<>("C2", () -> 8);
        registry.put(t2, t2.id());
        Assertions.assertThrows(IllegalStateException.class, () -> processor.submit(t2));
    }

    @Test
    void testShutdownAndAbortWhileTaskRunning() {
        Map<Object, String> registry = new ConcurrentHashMap<>();
        processor = new TaskProcessorEventDriven<>("ev-abort-running", registry::get);

        CallableWithId<Integer> task = new CallableWithId<>("R", () -> 123);
        registry.put(task, task.id());
        CompletableFuture<Integer> cf = processor.submit(task);

        // simulate in-flight: do not call update yet
        Assertions.assertFalse(cf.isDone());

        Assertions.assertDoesNotThrow(() -> processor.shutdownAndAbort());
        Assertions.assertTrue(processor.isShutdown());

        // after abort, the processor should be marked completed after wait
        Assertions.assertTrue(processor.waitForCompletion(500, TimeUnit.MILLISECONDS));
        Assertions.assertTrue(processor.isCompleted());

        // the task future should still be incomplete at this point (no external completion happened)
        Assertions.assertFalse(cf.isDone());
    }

    private static final class CallableWithId<T> implements java.util.concurrent.Callable<T> {
        private final String id;
        private final java.util.concurrent.Callable<T> delegate;

        private CallableWithId(String id, java.util.concurrent.Callable<T> delegate) {
            this.id = id;
            this.delegate = delegate;
        }

        String id() { return id; }

        @Override
        public T call() throws Exception {
            return delegate.call();
        }
    }
}
