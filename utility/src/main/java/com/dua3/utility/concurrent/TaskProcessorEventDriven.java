package com.dua3.utility.concurrent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * An event-driven implementation of a task processor, built on top of {@link TaskProcessorBase}.
 * This processor manages tasks submitted to an external system and processes task updates
 * received asynchronously.
 *
 * @param <K> the type of the unique key associated with each task
 * @param <T> the type of the task result
 */
public class TaskProcessorEventDriven<K, T> extends TaskProcessorBase {
    private static final Logger LOG = LogManager.getLogger(TaskProcessorEventDriven.class);

    private final Function<Callable<?>, K> submitExternal;
    private final Map<K, TaskEntry> futures = new ConcurrentHashMap<>();

    private record TaskEntry(long id, CompletableFuture<? extends Object> completableFuture) {}

    /**
     * Constructs a new instance of TaskProcessorEventDriven.
     *
     * @param name the name of the task processor
     * @param submitExternal a function that takes a {@link Callable} task and returns a unique key
     *                        of type {@code K} associated with the submitted task
     */
    public TaskProcessorEventDriven(String name, Function<Callable<?>, K> submitExternal) {
        super(name);
        this.submitExternal = submitExternal;
    }

    /**
     * Updates the task associated with the given key by either completing the task or logging an update
     * if the task is not marked as completed. If the task is completed, it sets the result and removes
     * the future from the internal task map. If the task is not found or not completed, it logs appropriate messages.
     * <p>
     * Call this method to inform the task processor about processing events received from the external system.
     *
     * @param key the unique identifier for the task being updated
     * @param result the result to be set if the task is successfully completed
     * @param isCompleted a flag indicating whether the task is completed
     */
    public void updateTask(K key, T result, boolean isCompleted) {
        if (isCompleted) {
            // set the result and remove the future
            LOG.debug("task {}: completed", key);
            futures.compute(key, (k, entry) -> {
                if (entry != null) {
                    LOG.debug("task {} with key {}: completing future", entry.id(), k);
                    ((CompletableFuture<T>) entry.completableFuture()).complete(result);
                    return null;
                } else {
                    LOG.debug("task {} with key {}: ignoring completion event, task not found", k);
                    return null;
                }
            });
        } else {
            TaskEntry taskEntry = futures.get(key);
            if (taskEntry != null) {
                LOG.debug("task {} with key {}: ignoring update, task is not completed", taskEntry.id(), key);
            } else {
                LOG.debug("task {} with key {}: ignoring update, task not found", key);
            }
        }
    }

    @Override
    public <T> CompletableFuture<T> submit(Callable<? extends T> task) {
        long id = nextId();
        registerId(id);
        CompletableFuture<T> cf = new CompletableFuture<>();
        K key = submitExternal.apply(task);
        futures.put(key, new TaskEntry(id, cf));
        return cf;
    }
}
