package com.dua3.utility.concurrent;

import com.dua3.utility.lang.LangUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

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

        assertEquals(1, processor.submit(tA1).get(1, TimeUnit.SECONDS));
        assertEquals(2, processor.submit(tA2).get(1, TimeUnit.SECONDS));
        assertEquals(3, processor.submit(tB1).get(1, TimeUnit.SECONDS));

        // exactly two delegates should have been created, for keys A and B
        assertEquals(2, created.get());
        assertTrue(createdKeys.containsAll(Set.of("A", "B")));
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
        assertEquals(10, f1.get(1, TimeUnit.SECONDS));
        assertEquals(20, f2.get(1, TimeUnit.SECONDS));

        processor.shutdown();

        // both created delegates should have been shut down
        assertEquals(2, factory.shutdownCalls.get());

        // submitting after shutdown must throw
        assertThrows(IllegalStateException.class, () -> processor.submit(new KeyedCallable<>("X", () -> 1)));
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
