package com.dua3.utility.concurrent;

import com.dua3.utility.lang.LangUtil;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * A generic interface to control different kinds of task processors.
 */
public interface TaskProcessor {
    /**
     * Get name of processor.
     * @return the task processor's name
     */
    String getName();

    /**
     * Shutdown and abort all submitted tasks.
     */
    void shutdownAndAbort();

    /**
     * Shutdown the processor.
     * <p>
     * Once a processor is shut down, it will not accept new tasks.
     */
    void shutdown();

    /**
     * Check if processor has been shut down.
     *
     * @return true, if {@link #shutdown()} has been called
     */
    boolean isShutdown();

    /**
     * Check if all submitted tasks have been completed.
     *
     * @return true, if all submitted tasks have been completed.
     */
    boolean isCompleted();

    /**
     * Wait until all submitted tasks have completed or a timeout occurs.
     *
     * @param timeout the timeout value
     * @param timeUnit the unit to apply to the timeout value
     * @return true, if all tasks have completed, otherwise false (timeout)
     */
    boolean waitForCompletion(long timeout, TimeUnit timeUnit);

    /**
     * Submit a task that that returns a result.
     *
     * @param <T> the task result type
     * @param task the task to execute
     * @return a {@link CompletableFuture} for the task
     */
    <T> CompletableFuture<T> submit(Callable<? extends T> task);

    /**
     * Submit a task that does nor return a value.
     *
     * @param task the task to execute
     */
    void submit(LangUtil.RunnableThrows<Exception> task);
}
