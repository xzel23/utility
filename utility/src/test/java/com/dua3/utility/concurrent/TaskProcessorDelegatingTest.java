package com.dua3.utility.concurrent;

import com.dua3.utility.lang.LangUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

class TaskProcessorDelegatingTest {

    private TaskProcessorDelegating<String> processor;

    @AfterEach
    void tearDown() {
        if (processor != null && !processor.isShutdown()) {
            processor.shutdown();
        }
        if (processor != null && !processor.isCompleted()) {
            processor.waitForCompletion(100, TimeUnit.MILLISECONDS);
        }
    }

    @Test
    void testDelegationByKeyCreatesOneDelegatePerKey() throws Exception {
        AtomicInteger created = new AtomicInteger();
        Set<String> createdKeys = ConcurrentHashMap.newKeySet();

        Function<String, TaskProcessor> factory = key -> {
            created.incrementAndGet();
            createdKeys.add(key);
            return new TaskProcessorAsync("delegate-" + key, 1);
        };

        processor = new TaskProcessorDelegating<>(
                "delegating",
                factory,
                task -> ((KeyedCallable<?>) task).key
        );

        KeyedCallable<Integer> tA1 = new KeyedCallable<>("A", () -> 1);
        KeyedCallable<Integer> tA2 = new KeyedCallable<>("A", () -> 2);
        KeyedCallable<Integer> tB1 = new KeyedCallable<>("B", () -> 3);

        Assertions.assertEquals(1, processor.submit(tA1).get(1, TimeUnit.SECONDS));
        Assertions.assertEquals(2, processor.submit(tA2).get(1, TimeUnit.SECONDS));
        Assertions.assertEquals(3, processor.submit(tB1).get(1, TimeUnit.SECONDS));

        // exactly two delegates should have been created, for keys A and B
        Assertions.assertEquals(2, created.get());
        Assertions.assertTrue(createdKeys.containsAll(Set.of("A", "B")));
    }

    @Test
    void testShutdownPropagatesToDelegatesAndPreventsFurtherSubmissions() throws Exception {
        // wrapping delegate recording shutdown calls
        RecordingDelegateFactory factory = new RecordingDelegateFactory();

        processor = new TaskProcessorDelegating<>(
                "delegating-shutdown",
                factory,
                task -> ((KeyedCallable<?>) task).key
        );

        // create two delegates by submitting two keys
        CompletableFuture<Integer> f1 = processor.submit(new KeyedCallable<>("X", () -> 10));
        CompletableFuture<Integer> f2 = processor.submit(new KeyedCallable<>("Y", () -> 20));
        Assertions.assertEquals(10, f1.get(1, TimeUnit.SECONDS));
        Assertions.assertEquals(20, f2.get(1, TimeUnit.SECONDS));

        processor.shutdown();

        // both created delegates should have been shut down
        Assertions.assertEquals(2, factory.shutdownCalls.get());

        // submitting after shutdown must throw
        Assertions.assertThrows(IllegalStateException.class, () -> processor.submit(new KeyedCallable<>("X", () -> 1)));
    }

    @Test
    void testShutdownAndAbortWhenNoTasks() {
        Function<String, TaskProcessor> factory = key -> new TaskProcessorAsync("d-abort-none-" + key, 1);
        processor = new TaskProcessorDelegating<>(
                "delegating-abort-none",
                factory,
                task -> ((KeyedCallable<?>) task).key
        );

        Assertions.assertDoesNotThrow(() -> processor.shutdownAndAbort());
        Assertions.assertTrue(processor.isShutdown());
        Assertions.assertTrue(processor.waitForCompletion(200, TimeUnit.MILLISECONDS));
        Assertions.assertTrue(processor.isCompleted());
        Assertions.assertThrows(IllegalStateException.class, () -> processor.submit(new KeyedCallable<>("X", () -> 1)));
    }

    @Test
    void testShutdownAndAbortAfterCompletedTasks() throws Exception {
        Function<String, TaskProcessor> factory = key -> new TaskProcessorAsync("d-abort-completed-" + key, 1);
        processor = new TaskProcessorDelegating<>(
                "delegating-abort-completed",
                factory,
                task -> ((KeyedCallable<?>) task).key
        );

        CompletableFuture<Integer> f1 = processor.submit(new KeyedCallable<>("A", () -> 1));
        CompletableFuture<Integer> f2 = processor.submit(new KeyedCallable<>("B", () -> 2));
        Assertions.assertEquals(1, f1.get(1, TimeUnit.SECONDS));
        Assertions.assertEquals(2, f2.get(1, TimeUnit.SECONDS));

        processor.shutdownAndAbort();
        Assertions.assertTrue(processor.isShutdown());
        Assertions.assertTrue(processor.waitForCompletion(500, TimeUnit.MILLISECONDS));
        Assertions.assertTrue(processor.isCompleted());
        Assertions.assertThrows(IllegalStateException.class, () -> processor.submit(new KeyedCallable<>("A", () -> 3)));
    }

    @Test
    void testShutdownAndAbortWhileTasksRunning() {
        Function<String, TaskProcessor> factory = key -> new TaskProcessorAsync("d-abort-run-" + key, 1);
        processor = new TaskProcessorDelegating<>(
                "delegating-abort-running",
                factory,
                task -> ((KeyedCallable<?>) task).key
        );

        CompletableFuture<Integer> slowA = processor.submit(new KeyedCallable<>("A", () -> {
            try {
                Thread.sleep(5_000);
                return 1;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }));
        CompletableFuture<Integer> slowB = processor.submit(new KeyedCallable<>("B", () -> {
            try {
                Thread.sleep(5_000);
                return 2;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }));

        Assertions.assertDoesNotThrow(() -> processor.shutdownAndAbort());

        Assertions.assertThrows(Exception.class, slowA::join);
        Assertions.assertThrows(Exception.class, slowB::join);

        Assertions.assertTrue(processor.waitForCompletion(1_000, TimeUnit.MILLISECONDS));
        Assertions.assertTrue(processor.isCompleted());
    }

    private static final class KeyedCallable<T> implements java.util.concurrent.Callable<T> {
        private final String key;
        private final java.util.concurrent.Callable<T> delegate;

        private KeyedCallable(String key, java.util.concurrent.Callable<T> delegate) {
            this.key = key;
            this.delegate = delegate;
        }

        @Override
        public T call() throws Exception {
            return delegate.call();
        }
    }

    private static final class RecordingDelegateFactory implements Function<String, TaskProcessor> {
        private final AtomicInteger shutdownCalls = new AtomicInteger();

        @Override
        public TaskProcessor apply(String key) {
            return new RecordingTaskProcessor("rec-" + key, shutdownCalls);
        }
    }

    private static final class RecordingTaskProcessor implements TaskProcessor {
        private final String name;
        private final TaskProcessorAsync delegate;
        private final AtomicInteger shutdownCounter;

        private RecordingTaskProcessor(String name, AtomicInteger shutdownCounter) {
            this.name = name;
            this.delegate = new TaskProcessorAsync(name + "-async", 1);
            this.shutdownCounter = shutdownCounter;
        }

        @Override
        public String getName() { return name; }

        @Override
        public void shutdownAndAbort() { delegate.shutdownAndAbort(); }

        @Override
        public void shutdown() {
            delegate.shutdown();
            shutdownCounter.incrementAndGet();
        }

        @Override
        public boolean isShutdown() { return delegate.isShutdown(); }

        @Override
        public boolean isCompleted() { return delegate.isCompleted(); }

        @Override
        public boolean waitForCompletion(long timeout, TimeUnit timeUnit) {
            return delegate.waitForCompletion(timeout, timeUnit);
        }

        @Override
        public <T> CompletableFuture<T> submit(java.util.concurrent.Callable<? extends T> task) {
            return delegate.submit(task);
        }

        @Override
        public void submit(LangUtil.RunnableThrows<Exception> task) { delegate.submit(task); }
    }
}
