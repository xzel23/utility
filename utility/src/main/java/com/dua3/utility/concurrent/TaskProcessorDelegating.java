package com.dua3.utility.concurrent;

import com.dua3.utility.lang.LangUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * A {@link TaskProcessor} implementation that delegates tasks to other processors.
 *
 * @param <K> class of the key that determines which delegate to use
 */
public class TaskProcessorDelegating<K> extends TaskProcessorBase {
    private static final Logger LOG = LogManager.getLogger(TaskProcessorDelegating.class);

    private final Map<K, TaskProcessor> delegates = new ConcurrentHashMap<>();
    private final Function<K, ? extends TaskProcessor> createDelegate;
    private final Function<Object, K> getDelegateKey;

    /**
     * Constructor.
     * @param name the name of the processor
     * @param createDelegate factory function for creating delegates
     * @param getDelegateKey delegate key determination function
     */
    public TaskProcessorDelegating(String name, Function<K, ? extends TaskProcessor> createDelegate, Function<Object, K> getDelegateKey) {
        super(name);
        this.createDelegate = createDelegate;
        this.getDelegateKey = getDelegateKey;
    }

    @Override
    public void shutdown() {
        super.shutdown();
        delegates.values().forEach(TaskProcessor::shutdown);
    }

    @Override
    public void shutdownAndAbort() {
        super.shutdown();
        delegates.values().forEach(TaskProcessor::shutdownAndAbort);
        terminate();
    }

    @Override
    public boolean waitForCompletion(long timeout, TimeUnit timeUnit) {
        ensureClosed();
        List<TaskProcessor> activeDelegates = new ArrayList<>(delegates.values());
        activeDelegates.removeIf(TaskProcessor::isCompleted);

        Instant startWait = Instant.now();
        Instant deadline = startWait.plus(Duration.ofMillis(timeUnit.toMillis(timeout)));
        while (!activeDelegates.isEmpty() && Instant.now().isBefore(deadline)) {
            for (TaskProcessor delegate : List.copyOf(activeDelegates)) {
                Instant now = Instant.now();
                Duration maxWait = Duration.between(now, deadline);
                if (maxWait.isNegative()) {
                    return false;
                }
                boolean rc = delegate.waitForCompletion(maxWait.toMillis(), TimeUnit.MILLISECONDS);
                if (rc) {
                    activeDelegates.remove(delegate);
                }
            }
        }

        if (!activeDelegates.isEmpty()) {
            return false;
        }

        Instant now = Instant.now();
        Duration maxWait = Duration.between(now, deadline);
        return super.waitForCompletion(maxWait.toMillis(), TimeUnit.MILLISECONDS);
    }

    /**
     * Get delegate task processor for delegate key.
     * @param key the delegate key
     * @return the delegate task processor
     */
    protected TaskProcessor getDelegate(K key) {
        return delegates.computeIfAbsent(key, k -> {
            ensureOpen();
            return createDelegate.apply(k);
        });
    }

    @Override
    public <T> CompletableFuture<T> submit(Callable<? extends T> task) {
        K key = getDelegateKey.apply(task);
        TaskProcessor delegate = getDelegate(key);
        LOG.trace("'{}' - submitting callable task to delegate {}", getName(), delegate);
        return delegate.submit(task);
    }

    @Override
    public void submit(LangUtil.RunnableThrows<Exception> task) {
        K key = getDelegateKey.apply(task);
        TaskProcessor delegate = getDelegate(key);
        LOG.trace("'{}' - submitting runnable task to delegate {}", getName(), delegate);
        delegate.submit(task);
    }

}
