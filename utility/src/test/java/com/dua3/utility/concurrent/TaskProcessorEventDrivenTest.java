package com.dua3.utility.concurrent;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

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
                task -> registry.get(task)
        );

        CallableWithId<Integer> task = new CallableWithId<>("A", () -> 21 + 21);
        // register mapping for external key
        registry.put(task, task.id());

        CompletableFuture<Integer> future = processor.submit(task);
        assertFalse(future.isDone());

        // non-completion update must not finish the future
        processor.updateTask(task.id(), 0, false);
        assertFalse(future.isDone());

        // now complete
        processor.updateTask(task.id(), 42, true);
        assertEquals(42, future.get(1, TimeUnit.SECONDS));
    }

    @Test
    void testUnknownKeyUpdateIsIgnored() {
        Map<Object, String> registry = new ConcurrentHashMap<>();
        processor = new TaskProcessorEventDriven<>("ev2", task -> registry.get(task));

        // updating unknown key must not throw
        assertDoesNotThrow(() -> processor.updateTask("unknown", 1, true));
    }

    @Test
    void testSubmitAfterShutdownThrows() {
        Map<Object, String> registry = new ConcurrentHashMap<>();
        processor = new TaskProcessorEventDriven<>("ev3", task -> registry.get(task));
        processor.shutdown();
        CallableWithId<Integer> task = new CallableWithId<>("K", () -> 1);
        registry.put(task, task.id());
        assertThrows(IllegalStateException.class, () -> processor.submit(task));
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
