package com.dua3.utility.concurrent;

import com.dua3.utility.lang.LangUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * {@link TaskProcessor} implementation where tasks are executed asynchronous using an {@link ExecutorService}.
 * <p>
 * When an instance is created, the number of maximum parallel tasks can be passed in.
 */
public class TaskProcessorAsync extends TaskProcessorBase {
    private static final Logger LOG = LogManager.getLogger(TaskProcessorAsync.class);

    private final ExecutorService executor;

    /**
     * Constructor.
     *
     * @param name this processor's name
     * @param maxThreads maximal number of tasks that are executed parallel
     */
    public TaskProcessorAsync(String name, int maxThreads) {
        super(name);
        LangUtil.checkArg(maxThreads > 0, "maxThreads must be a positive integer: %d", maxThreads);
        executor = Executors.newFixedThreadPool(maxThreads, newThreadFactory());
    }

    @Override
    public void shutdownAndAbort() {
        super.shutdownAndAbort();
        executor.shutdownNow();
    }

    @Override
    public boolean waitForCompletion(final long timeout, final TimeUnit timeUnit) {
        boolean rc = super.waitForCompletion(timeout, timeUnit);
        if (rc) {
            LOG.trace("'{}' - shutting down executor", getName());
            executor.shutdown();
        } else {
            LOG.trace("'{}' - timeout waiting for tasks to complete", getName());
        }
        return rc;
    }

    /**
     * Submit a new task that returns a result.
     *
     * @param task the task to execute
     * @return a {@link CompletableFuture} for the task
     * @param <T> the task's result type
     */
    public <T> CompletableFuture<T> submit(Callable<? extends T> task) {
        long id = nextId();
        LOG.debug("'{}' - submitting new task {}", getName(), id);
        registerId(id);
        return CompletableFuture.supplyAsync(() -> {
            try {
                return task.call();
            } catch (Exception e) {
                throw new CompletionException(e);
            } finally {
                LOG.debug("'{}' - task {} completed", getName(), id);
                unregisterId(id);
            }
        }, executor);
    }
}
