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
 */
public class TaskProcessorEventDriven<K> extends TaskProcessorBase {
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
    @SuppressWarnings("unchecked")
    public void updateTask(K key, Object result, boolean isCompleted) {
        if (isCompleted) {
            // set the result and remove the future
            LOG.debug("'{}' - task with key {} completed", getName(), key);
            futures.compute(key, (k, entry) -> {
                if (entry != null) {
                    LOG.trace("'{}' - task {} with key {}: completing future", getName(), entry.id(), k);
                    ((CompletableFuture<Object>) entry.completableFuture()).complete(result);
                } else {
                    LOG.trace("'{}' - task with key {} not found: ignoring completion event", getName(), k);
                }
                return null;
            });
        } else {
            TaskEntry taskEntry = futures.get(key);
            if (taskEntry != null) {
                LOG.trace("'{}' - task {} with key {}: ignoring update, task is not completed", getName(), taskEntry.id(), key);
            } else {
                LOG.trace("'{}' - task with key {} not found: ignoring update", getName(), key);
            }
        }
    }

    @Override
    public <T> CompletableFuture<T> submit(Callable<? extends T> task) {
        long id = nextId();
        K key = submitExternal.apply(task);
        LOG.debug("'{}' - submitting new task {} with key {}", getName(), id, key);
        registerId(id);
        CompletableFuture<T> cf = new CompletableFuture<>();
        futures.put(key, new TaskEntry(id, cf));
        return cf;
    }
}
