package com.dua3.utility.concurrent;

import com.dua3.cabe.annotations.Nullable;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A builder class for creating customized ThreadFactory instances.
 */
public class ThreadFactoryBuilder {
    private ThreadGroup group = null;
    private long stackSize = 0;
    private String prefix = "";
    private boolean daemon = false;
    private int priority = Thread.NORM_PRIORITY;

    private ThreadFactoryBuilder() {
    }

    /**
     * Returns a ThreadFactoryBuilder object that allows creating customized ThreadFactory instances.
     *
     * @return the ThreadFactoryBuilder instance
     */
    public static ThreadFactoryBuilder builder() {
        return new ThreadFactoryBuilder();
    }

    /**
     * Sets the thread group for the ThreadFactoryBuilder. The threads created by this ThreadFactoryBuilder
     * will be associated with the specified thread group.
     *
     * @param group the thread group to set
     * @return the ThreadFactoryBuilder instance
     */
    public ThreadFactoryBuilder group(@Nullable ThreadGroup group) {
        this.group = group;
        return this;
    }

    /**
     * Sets the stack size for threads created by the ThreadFactoryBuilder.
     *
     * @param stackSize the stack size in bytes
     * @return the ThreadFactoryBuilder object
     */
    public ThreadFactoryBuilder stackSize(long stackSize) {
        this.stackSize = stackSize;
        return this;
    }

    /**
     * Sets the prefix for the thread names created by the ThreadFactory.
     *
     * @param prefix the prefix to set for the thread names
     * @return the updated ThreadFactoryBuilder instance
     */
    public ThreadFactoryBuilder prefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    /**
     * Sets the daemon flag for the ThreadFactoryBuilder instance.
     *
     * @param daemon the value to set for the daemon flag. If true, the threads created by the ThreadFactory will be daemon threads. If false, they will be non-daemon threads.
     * @return the ThreadFactoryBuilder instance with the updated daemon flag
     */
    public ThreadFactoryBuilder daemon(boolean daemon) {
        this.daemon = daemon;
        return this;
    }

    /**
     * Sets the priority of the threads created by this ThreadFactory.
     *
     * @param priority the priority of the threads
     * @return the ThreadFactoryBuilder instance to allow method chaining
     */
    public ThreadFactoryBuilder priority(int priority) {
        this.priority = priority;
        return this;
    }

    /**
     * Builds a CustomThreadFactory instance with the specified parameters.
     *
     * @return The CustomThreadFactory instance.
     */
    public CustomThreadFactory build() {
        return new CustomThreadFactory(group, stackSize, prefix, daemon, priority);
    }

    private static class CustomThreadFactory implements ThreadFactory {
        private final ThreadGroup group;
        private final long stackSize;
        private final String prefix;
        private final boolean daemon;
        private final int priority;
        private final AtomicLong count = new AtomicLong(0);

        /**
         * A custom thread factory implementation that allows creating customized ThreadFactory instances.
         */
        public CustomThreadFactory(ThreadGroup group, long stackSize, String prefix, boolean daemon, int priority) {
            this.group = group;
            this.stackSize = stackSize;
            this.prefix = prefix;
            this.daemon = daemon;
            this.priority = priority;
        }

        /**
         * Creates a new daemon thread.
         *
         * @param r the Runnable object to be executed by the new thread
         * @return the newly created daemon thread
         */
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(group, r, generateName(), stackSize);
            thread.setDaemon(daemon);
            thread.setPriority(priority);
            return thread;
        }

        private String generateName() {
            return prefix + count.incrementAndGet();
        }
    }
}
