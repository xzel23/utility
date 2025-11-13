package com.dua3.utility.concurrent;

import com.dua3.utility.lang.LangUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.Callable;
import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Base class for {@link TaskProcessor} implementations.
 * <p>
 * The class implements the control of the taks processor life cycle using a {@link Phaser} instance
 * to make sure the ExecutorService is not shut down before all submitted tasks have completed.
 */
public abstract class TaskProcessorBase implements TaskProcessor {
    private static final Logger LOG = LogManager.getLogger(TaskProcessorBase.class);
    private static final AtomicLong ID_COUNTER = new AtomicLong(0);

    private final String name;
    private final Phaser phaser = new Phaser(1);
    private final AtomicBoolean isShutDown = new AtomicBoolean(false);
    private final AtomicBoolean isCompleted = new AtomicBoolean(false);

    private final AtomicInteger tasksSubmitted = new AtomicInteger(0);
    private final AtomicInteger tasksCompleted = new AtomicInteger(0);

    /**
     * Represents the state of a {@code TaskProcessorBase}.
     */
    public enum State {
        /**
         * The task processor is active and accepting tasks.
         */
        RUNNING,
        /**
         * The task processor is shutting down, but tasks are still being completed.
         */
        SHUTDOWN,
        /**
         * The task processor has finished processing all tasks and is fully shut down.
         */
        COMLETED;
    }

    /**
     * A record that represents the statistics of tasks processed by a {@code TaskProcessorBase}.
     *
     * @param submitted the total number of tasks that have been submitted
     * @param completed the total number of tasks that have been completed so far
     * @param state     the current state of the task processor, represented as a {@link State} enum
     */
    public record Stats(int submitted, int completed, State state) {}

    /**
     * Retrieves the statistics of tasks processed by this task processor.
     * @return a {@code Stats} record containing current stats for this processor
     */
    public Stats getStats() {
        return new Stats(tasksSubmitted.get(), tasksCompleted.get(), getState());
    }

    /**
     * Retrieves the current state of the task processor.

     * @return the current state of the task processor as a {@link State} enum value.
     */
    public State getState() {
        if (isShutdown()) {
            return isCompleted.get() ? State.COMLETED : State.SHUTDOWN;
        } else {
            return State.RUNNING;
        }
    }

    /**
     * Constructor.
     *
     * @param name the task processor name.
     */
    protected TaskProcessorBase(String name) {
        this.name = name;
        LOG.debug("'{}' - created", name);
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * Get ID for an object.
     * @return the ID to use for the next task
     */
    protected long nextId() {
        return ID_COUNTER.incrementAndGet();
    }

    /**
     * Register party.
     * @param party the party name
     * @see Phaser#register()
     */
    protected void registerParty(String party) {
        LOG.info("'{}' - registering party: {} for phaser {}", name, party, phaser);
        ensureOpen();
        phaser.register();
    }

    /**
     * Unregister party.
     * @param party the party name
     * @see Phaser#register()
     */
    protected void unregisterParty(String party) {
        phaser.arriveAndDeregister();
        LOG.info("'{}' - unregistered party: {} for phaser {}", name, party, phaser);
    }

    /**
     * Register a task ID.
     * @param id the task ID
     * @see Phaser#register()
     */
    protected void registerId(long id) {
        LOG.info("'{}' - registering ID: {} for phaser {}", name, id, phaser);
        ensureOpen();
        phaser.register();
        tasksSubmitted.incrementAndGet();
    }

    /**
     * Unregister a task ID.
     * @param id the task ID
     * @see Phaser#register()
     */
    protected void unregisterId(long id) {
        phaser.arriveAndDeregister();
        tasksCompleted.incrementAndGet();
        LOG.info("'{}' - unregistered ID: {} for phaser  {}", name, id, phaser);
    }

    @Override
    public void shutdown() {
        LOG.info("'{}' - shutdown(): {}", name, phaser);

        ensureOpen();

        if (!isShutDown.compareAndSet(false, true)) {
            throw new IllegalStateException("'" + name + "' - shutdown() called twice");
        }

        ensureClosed();
    }

    @Override
    public boolean isShutdown() {
        return isShutDown.get();
    }

    @Override
    public boolean isCompleted() {
        return isCompleted.get();
    }

    /**
     * Ensure that the processor has not been shut down, i.e., is accepting tasks.
     *
     * @throws IllegalStateException if the processor has been shut down
     */
    public void ensureOpen() {
        LangUtil.check(!isShutDown.get(), "'%' - is shut down", name);
    }

    /**
     * Ensure that the processor has been shut down.
     *
     * @throws IllegalStateException if the processor has not been shut down
     */
    public void ensureClosed() {
        LangUtil.check(isShutDown.get(), "'%s' - is not shut down", name);
    }

    @Override
    public void shutdownAndAbort() {
        LOG.info("'{}' - shutdownAndAbort(): {}", name, phaser);
        shutdown();
        phaser.forceTermination();
    }

    @Override
    public boolean waitForCompletion(long timeout, TimeUnit timeUnit) {
        LOG.debug("'{}' - waiting for completion", name);
        try {
            phaser.awaitAdvanceInterruptibly(-1, timeout, timeUnit);
            if (!isCompleted.compareAndSet(false, true)) {
                throw new IllegalStateException("'" + name + "' waitForCompletion() called twice");
            }
            LOG.info("'{}' - completed", name);
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("'" + name + "' interrupted", e);
        } catch (TimeoutException e) {
            LOG.debug("'{}' - timeout waiting for completion", name);
            return false;
        }
    }

    /**
     * Submit a task that does nor return a value.
     *
     * @param task the task to execute
     */
    public void submit(LangUtil.RunnableThrows<Exception> task) {
        submit((Callable<Void>) () -> {
            try {
                task.run();
            } catch (Exception e) {
                LOG.warn("TaskProcessor {}: failed with exception {}", getName(), e.getMessage(), e);
            }
            return null;
        });
    }

}
