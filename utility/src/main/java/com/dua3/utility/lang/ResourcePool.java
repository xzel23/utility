package com.dua3.utility.lang;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Represents a pool of reusable resources for maintaining efficient resource management.
 *
 * @param <T> the type of resource managed by the pool
 */
public interface ResourcePool<T> {
    /**
     * Represents a contract for managing the lifecycle of a resource within a lease-based system.
     *
     * @param <T> the type of the resource being leased
     */
    interface Lease<T> extends AutoCloseable {
        /**
         * Get the managed resource.
         *
         * @return the managed resource
         */
        T get();

        /**
         * Resets the resource for the next use.
         */
        @Override
        void close();
    }

    /**
     * Creates a new resource pool where resources are managed on a per-thread basis.
     * <p>
     * Each thread has its own instance of the resource, which is created using the provided factory
     * and released using the provided releaser when necessary.
     * <p>
     * <strong>Notes</strong>
     * <ul>
     * <li>This pool works exclusively on a per-thread basis and is not re-entrant safe for multiple
     * accesses from the same thread.
     * <li>When thread pools are used, instances from the pool can be reused and might never be cleaned
     *     up. It is the user's responsibility to ensure no data is leaked by not cleaning up resources
     *     after use.
     * </ul>
     *
     * @param <T>      the type of resource managed by the pool
     * @param factory  a {@code Supplier} that creates a new resource when one is needed
     * @param releaser a {@code Consumer} that handles the cleanup or release of the resource
     * @return a {@code ResourcePool} instance for managing thread-local resources
     */
    static <T> ResourcePool<T> newThreadBasedResourcePool(Supplier<T> factory, Consumer<T> releaser) {
        return new ThreadResourcePool<>(factory, releaser);
    }

    /**
     * Creates a fixed-size resource pool that maintains a constant number of resources.
     * The pool ensures that at any point, the number of managed resources equals the specified size.
     * Resources are created using the provided factory and released using the specified releaser.
     *
     * @param <T>      the type of resource managed by the pool
     * @param factory  a {@code Supplier} to create new resource instances when needed
     * @param releaser a {@code Consumer} to handle the cleanup or release of resources
     * @param size     the fixed number of resources to maintain in the pool
     * @return a {@code ResourcePool} instance that manages resources with a fixed size
     * @throws IllegalArgumentException if {@code size} is negative
     */
    static <T> ResourcePool<T> newFixedSizeResourcePool(Supplier<T> factory, Consumer<T> releaser, int size) {
        return new ListBackedResourcePool<>(factory, releaser, size, size);
    }

    /**
     * Creates a resource pool with a specified minimum and maximum capacity for managing reusable resources.
     * The resources are created using the provided factory, and their cleanup or release is managed by
     * the specified releaser.
     *
     * @param <T>          the type of resource managed by the pool
     * @param factory      a {@code Supplier} to create new resource instances as needed
     * @param releaser     a {@code Consumer} to handle the cleanup or release of the resources
     * @param minCapacity  the minimum number of resources to be pre-created in the pool
     * @param maxCapacity  the maximum number of resources allowed in the pool
     * @return             a {@code ResourcePool} instance configured with the specified minimum and maximum capacities
     * @throws IllegalArgumentException if {@code minCapacity} is negative or {@code maxCapacity} is less than {@code minCapacity}
     */
    static <T> ResourcePool<T> newResourcePool(Supplier<T> factory, Consumer<T> releaser, int minCapacity, int maxCapacity) {
        return new ListBackedResourcePool<>(factory, releaser, minCapacity, maxCapacity);
    }

    /**
     * Acquires a resource handle from the pool.
     * <p>
     * This method provides a {@link Lease} which must be closed after use,
     * typically using a try-with-resources block.
     * <p>
     * <b>Example:</b>
     * <pre>{@code
     * try (var lease = pool.acquire()) {
     *     T resource = lease.get();
     *     // use resource
     * }
     * }</pre>
     *
     * @return a lease containing the resource
     * @throws IllegalStateException if the current thread has already acquired
     * a resource from this pool and has not yet released it.
     */
    Lease<T> acquire();

    /**
     * Attempts to acquire a resource from the pool without blocking.
     * <p>
     * If a resource is available, this method returns a {@link Lease} providing
     * access to the resource. If no resources are available, it returns {@code null}.
     * <p>
     * The returned {@link Lease}, if not {@code null}, must be closed after use
     * to ensure proper resource management.
     *
     * @return a {@link Lease} containing the resource if one is available, or {@code null}
     *         if no resources are currently available
     */
    @Nullable Lease<T> tryAcquire();
}

/**
 * Implementation of the {@link ResourcePool.Lease} interface for managing the lifecycle of a resource.
 *
 * @param <T> the type of the resource being leased
 */
class LeaseImpl<T> implements ResourcePool.Lease<T> {
    private static final Logger LOG = LogManager.getLogger(LeaseImpl.class);

    final T resource;
    final Consumer<T> releaser;
    boolean leased = false;

    /**
     * Constructs a new {@code LeaseImpl} instance for managing the lifecycle of a resource.
     *
     * @param resource the resource being leased
     * @param releaser a {@link Consumer} to handle releasing the resource when the lease ends
     */
    LeaseImpl(T resource, Consumer<T> releaser) {
        this.resource = resource;
        this.releaser = releaser;
    }

    @Override
    public T get() {
        return resource;
    }

    @Override
    public void close() {
        if (!leased) {throw new IllegalStateException("resource not leased");}
        try {
            releaser.accept(resource);
        } catch (RuntimeException e) {
            LOG.warn("Failed to release resource, exception ignored: {}", e.getMessage(), e);
        } finally {
            leased = false;
        }
    }

    LeaseImpl<T> acquire() {
        if (leased) {throw new IllegalStateException("resource already leased");}
        leased = true;
        return this;
    }
}

/**
 * Implementation of a thread-local resource pool that provides a unique resource per thread.
 * Each thread has exclusive access to its resource, which is managed via the {@link Lease} interface.
 *
 * @param <T> the type of resource managed by the pool
 */
final class ThreadResourcePool<T> implements ResourcePool<T> {
    private final ThreadLocal<LeaseImpl<T>> threadLocalLease;

    ThreadResourcePool(Supplier<T> factory, Consumer<T> releaser) {
        // We store the wrapper itself in the ThreadLocal
        this.threadLocalLease = ThreadLocal.withInitial(() ->
                new LeaseImpl<>(factory.get(), releaser)
        );
    }

    @Override
    public Lease<T> acquire() {
        return threadLocalLease.get().acquire();
    }

    @Override
    public @Nullable Lease<T> tryAcquire() {
        LeaseImpl<T> lease = threadLocalLease.get();

        if (lease.leased) {
            return null;
        } else {
            lease.leased = true;
            return lease;
        }
    }
}

/**
 * A thread-safe, list-backed implementation of the {@link ResourcePool} interface for managing
 * a pool of reusable resources. This implementation maintains a fixed-size pool of resources
 * within the defined capacity limits.
 *
 * @param <T> the type of resource managed by the pool
 */
final class ListBackedResourcePool<T> implements ResourcePool<T> {

    private final Object lock = new Object();
    private final Supplier<T> factory;
    private final Consumer<T> releaser;
    private final BlockingQueue<BlockingLeaseImpl> queue;
    private final int minCapacity;
    private final int maxCapacity;
    private int resourceCount;
    private int waitingCount;

    /**
     * A private implementation of the {@code LeaseImpl} class that extends its functionality to operate
     * within a blocking resource pool context. This class is responsible for managing the lifecycle of
     * leased resources and ensuring they are returned to the resource pool upon closure.
     */
    private final class BlockingLeaseImpl extends LeaseImpl<T> {
        /**
         * Constructs a new {@code LeaseImpl} instance for managing the lifecycle of a resource.
         *
         * @param resource the resource being leased
         * @param releaser a {@link Consumer} to handle releasing the resource when the lease ends
         */
        BlockingLeaseImpl(T resource, Consumer<T> releaser) {
            super(resource, releaser);
        }

        @Override
        public void close() {
            try {
                super.close();
            } finally {
                putBack(this);
            }
        }
    }

    /**
     * Constructs a ListBackedResourcePool with the specified factory, releaser, and capacities.
     *
     * @param factory the supplier responsible for creating new resource instances.
     * @param releaser the consumer responsible for cleaning up or releasing resources.
     * @param minCapacity the minimum number of resources to be pre-allocated in the pool.
     * @param maxCapacity the maximum number of resources allowed in the pool.
     * @throws IllegalArgumentException if minCapacity is negative or maxCapacity is less than minCapacity.
     */
    ListBackedResourcePool(Supplier<T> factory, Consumer<T> releaser, int minCapacity, int maxCapacity) {
        LangUtil.checkArg(minCapacity >= 0, "minCapacity must be >= 0");
        LangUtil.checkArg(maxCapacity >= minCapacity, "maxCapacity must be >= minCapacity %d: %d", minCapacity, maxCapacity);

        this.factory = factory;
        this.releaser = releaser;
        this.minCapacity = minCapacity;
        this.maxCapacity = maxCapacity;
        this.queue = new ArrayBlockingQueue<>(maxCapacity);

        for (int i = 0; i < minCapacity; i++) {
            queue.add(new BlockingLeaseImpl(factory.get(), releaser));
        }

        this.resourceCount = queue.size();
        this.waitingCount = 0;
    }

    @Override
    public Lease<T> acquire() {
        try {
            // try to get resource non-blocking
            BlockingLeaseImpl lease = queue.poll();
            if (lease != null) {
                return lease.acquire();
            }

            // if the max capacity is not reached, create and return a new resource
            synchronized (lock) {
                lease = queue.poll();
                if (lease != null) {
                    return lease.acquire();
                }

                if (resourceCount < maxCapacity) {
                    lease = new BlockingLeaseImpl(factory.get(), releaser);
                    resourceCount++;
                    assert resourceCount <= maxCapacity : "internal error: resourceCount > maxCapacity";
                    return lease.acquire();
                }

                waitingCount++;
            }

            // if the max capacity is reached, wait for a resource to become available
            // DO NOT use try-with-resources!!! The try only makes sure the waiting count is always correct
            // The lifecycle of the returned Lease is managed by the caller.
            try {
                lease = queue.take();
                return lease.acquire();
            } finally {
                synchronized (lock) {
                    waitingCount--;
                    assert waitingCount >= 0 : "internal error: waitingCount < 0";
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new WrappedException(e);
        }
    }

    @Override
    public @Nullable Lease<T> tryAcquire() {
        BlockingLeaseImpl lease = queue.poll();
        if (lease != null) {
            return lease.acquire();
        }

        synchronized (lock) {
            lease = queue.poll();
            if (lease != null) {
                return lease.acquire();
            }

            if (resourceCount < maxCapacity) {
                lease = new BlockingLeaseImpl(factory.get(), releaser);
                resourceCount++;
                assert resourceCount <= maxCapacity : "internal error: resourceCount > maxCapacity";
                return lease.acquire();
            }
        }

        return null;
    }

    /**
     * Returns the given resource lease back to the resource pool. If the current resource count
     * exceeds the minimum capacity and there are no threads waiting for resources, the resource
     * count is decremented. Otherwise, the lease is added back to the internal queue.
     *
     * @param lease the resource lease to be returned to the pool
     *              (should not be null and must belong to this pool)
     * @throws AssertionError if the resource count goes below zero or if the queue is full
     */
    private void putBack(BlockingLeaseImpl lease) {
        synchronized (lock) {
            if (resourceCount > minCapacity && waitingCount == 0) {
                resourceCount--;
                assert resourceCount >= 0 : "internal error: resourceCount < 0";
            } else {
                boolean accepted = queue.offer(lease);
                assert accepted : "internal error: queue is full";
            }
        }
    }
}
