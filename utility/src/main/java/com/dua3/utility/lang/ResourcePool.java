package com.dua3.utility.lang;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Represents a pool of reusable resources for maintaining efficient resource management.
 *
 * @param <T> the type of resource managed by the pool
 */
public interface ResourcePool<T> extends AutoCloseable {
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
         * Checks whether the resource managed by this lease is currently leased.
         *
         * @return {@code true} if the resource is leased, {@code false} otherwise
         */
        boolean isLeased();

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
        return new ThreadResourcePool<>(factory, releaser, null);
    }

    /**
     * Creates a new resource pool where resources are managed on a per-thread basis with an additional
     * destructor for resource cleanup when the pool is closed.
     * <p>
     * Each thread has its own instance of the resource, which is created using the provided factory,
     * released using the provided releaser when necessary, and cleaned up using the provided destructor
     * when the pool is closed.
     *
     * @param <T>        the type of resource managed by the pool
     * @param factory    a {@code Supplier} that creates a new resource when one is needed
     * @param releaser   a {@code Consumer} that handles the cleanup or release of resources before re-use
     * @param destructor a {@code Consumer} that performs final cleanup of resources when the pool is closed
     * @return a {@code ResourcePool} instance for managing thread-local resources
     */
    static <T> ResourcePool<T> newThreadBasedResourcePool(Supplier<T> factory, Consumer<T> releaser, Consumer<T> destructor) {
        return new ThreadResourcePool<>(factory, releaser, destructor);
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
        return new ListBackedResourcePool<>(factory, releaser, LangUtil::ignore, size, size);
    }

    /**
     * Creates a fixed-size resource pool that maintains a constant number of resources.
     * The pool ensures that at any point, the number of managed resources equals the specified size.
     * Resources are created using the provided factory and released using the specified releaser.
     *
     * @param <T>        the type of resource managed by the pool
     * @param factory    a {@code Supplier} to create new resource instances when needed
     * @param releaser   a {@code Consumer} to handle the cleanup or release of resources
     * @param destructor a {@code Consumer} that performs final cleanup of resources when the pool is closed
     * @param size       the fixed number of resources to maintain in the pool
     * @return a {@code ResourcePool} instance that manages resources with a fixed size
     * @throws IllegalArgumentException if {@code size} is negative
     */
    static <T> ResourcePool<T> newFixedSizeResourcePool(Supplier<T> factory, Consumer<T> releaser, Consumer<T> destructor, int size) {
        return new ListBackedResourcePool<>(factory, releaser, destructor, size, size);
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
        return new ListBackedResourcePool<>(factory, releaser, LangUtil::ignore, minCapacity, maxCapacity);
    }

    /**
     * Creates a resource pool with a specified minimum and maximum capacity for managing reusable resources.
     * The resources are created using the provided factory, and their cleanup or release is managed by
     * the specified releaser.
     *
     * @param <T>          the type of resource managed by the pool
     * @param factory      a {@code Supplier} to create new resource instances as needed
     * @param releaser     a {@code Consumer} to handle the cleanup or release of the resources
     * @param destructor   a {@code Consumer} that performs final cleanup of resources when the pool is closed
     * @param minCapacity  the minimum number of resources to be pre-created in the pool
     * @param maxCapacity  the maximum number of resources allowed in the pool
     * @return             a {@code ResourcePool} instance configured with the specified minimum and maximum capacities
     * @throws IllegalArgumentException if {@code minCapacity} is negative or {@code maxCapacity} is less than {@code minCapacity}
     */
    static <T> ResourcePool<T> newResourcePool(Supplier<T> factory, Consumer<T> releaser, Consumer<T> destructor, int minCapacity, int maxCapacity) {
        return new ListBackedResourcePool<>(factory, releaser, destructor, minCapacity, maxCapacity);
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

    @Override
    void close();
}

/**
 * Implementation of the {@link ResourcePool.Lease} interface for managing the lifecycle of a resource.
 *
 * @param <T> the type of the resource being leased
 */
class LeaseImpl<T> implements ResourcePool.Lease<T> {
    private static final Logger LOG = LogManager.getLogger(LeaseImpl.class);
    static final String EXCEPTION_WHEN_DESTRUCTING_RESOURCE_IGNORED = "exception when destructing resource (ignored): {}";

    private @Nullable T resource;
    private final Consumer<T> releaser;
    private boolean leased = false;

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
        return Objects.requireNonNull(resource, "get() called on disposed lease");
    }

    @Override
    public boolean isLeased() {
        return leased;
    }

    /**
     * Sets the leased state of the resource.
     *
     * @param leased {@code true} to mark the resource as leased, {@code false} to mark it as not leased
     */
    void setLeased(boolean leased) {
        this.leased = leased;
    }

    /**
     * Releases the reference to the leased resource by setting it to {@code null}.
     * This method is used to clean up and prepare the lease for eventual garbage collection.
     */
    void dispose() {
        this.resource = null;
    }

    @Override
    public void close() {
        if (!leased) {throw new IllegalStateException("resource not leased");}
        try {
            assert resource != null : "internal error: close() - resource is null";
            releaser.accept(resource);
        } catch (RuntimeException e) {
            LOG.warn("Failed to release resource, exception ignored: {}", e.getMessage(), e);
        } finally {
            leased = false;
        }
    }

    /**
     * Acquires the lease for the resource managed by this instance.
     * If the resource is already leased, this method throws an {@link IllegalStateException}.
     *
     * @return the current {@code LeaseImpl} instance after the lease has been successfully acquired
     * @throws IllegalStateException if the resource is already leased
     */
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
    private static final Logger LOG = LogManager.getLogger(ThreadResourcePool.class);

    final class ThreadLocalLeaseImpl extends LeaseImpl<T> {
        /**
         * Constructs a new {@code LeaseImpl} instance for managing the lifecycle of a resource.
         *
         * @param resource the resource being leased
         * @param releaser a {@link Consumer} to handle releasing the resource when the lease ends
         */
        ThreadLocalLeaseImpl(T resource, Consumer<T> releaser) {
            super(resource, releaser);
        }
    }

    private final @Nullable Set<ThreadLocalLeaseImpl> allLeases;
    private final Consumer<T> destructor;
    private final ThreadLocal<ThreadLocalLeaseImpl> threadLocalLease;
    private final AtomicBoolean closed = new AtomicBoolean(false);

    ThreadResourcePool(Supplier<T> factory, Consumer<T> releaser, @Nullable Consumer<T> destructor) {
        // We store the wrapper itself in the ThreadLocal
        if (destructor == null) {
            this.destructor = LangUtil::ignore;
            this.allLeases = null;
            this.threadLocalLease = ThreadLocal.withInitial(() -> new ThreadLocalLeaseImpl(factory.get(), releaser));
        } else {
            this.destructor = destructor;
            this.allLeases = ConcurrentHashMap.newKeySet();
            this.threadLocalLease = ThreadLocal.withInitial(() -> {
                ThreadLocalLeaseImpl lease = new ThreadLocalLeaseImpl(factory.get(), releaser);
                allLeases.add(lease);
                return lease;
            });
        }
    }

    @Override
    public Lease<T> acquire() {
        ThreadLocalLeaseImpl lease = threadLocalLease.get();
        if (closed.get()) {
            throw new IllegalStateException("resource pool is closed");
        }
        return lease.acquire();
    }

    @Override
    public @Nullable Lease<T> tryAcquire() {
        ThreadLocalLeaseImpl lease = threadLocalLease.get();

        if (closed.get()) {
            throw new IllegalStateException("resource pool is closed");
        }

        if (lease.isLeased()) {
            return null;
        } else {
            lease.setLeased(true);
            return lease;
        }
    }

    @Override
    public void close() {
        if (closed.getAndSet(true)) {
            throw new IllegalStateException("pool is already closed");
        }
        if (allLeases != null) {
            allLeases.forEach(lease -> {
                try {
                    T resource = lease.get();
                    lease.dispose();
                    LangUtil.applyIfNonNull(resource, destructor);
                } catch (RuntimeException e) {
                    LOG.warn(LeaseImpl.EXCEPTION_WHEN_DESTRUCTING_RESOURCE_IGNORED, e.getMessage(), e);
                }
            });
        }
        threadLocalLease.remove();
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
    private static final Logger LOG = LogManager.getLogger(ListBackedResourcePool.class);

    private final Object lock = new Object();
    private final Supplier<T> factory;
    private final Consumer<T> releaser;
    private final Consumer<T> destructor;
    private final BlockingQueue<BlockingLeaseImpl> queue;
    private final int minCapacity;
    private final int maxCapacity;
    private @Nullable Semaphore closeLock = null;
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
                synchronized (lock) {
                    if (waitingCount == 0 && (resourceCount > minCapacity || isClosed())) {
                        resourceCount--;
                        assert resourceCount >= 0 : "internal error: close() - resourceCount < 0";
                        try {
                            T resource = get();
                            dispose();
                            destructor.accept(resource);
                        } catch (RuntimeException e) {
                            LOG.warn(LeaseImpl.EXCEPTION_WHEN_DESTRUCTING_RESOURCE_IGNORED, e.getMessage(), e);
                        }
                        if (resourceCount == 0) {
                            assert closeLock != null : "internal error: close() - closeLock is null";
                            closeLock.release();
                        }
                    } else {
                        boolean accepted = queue.offer(this);
                        assert accepted : "internal error: close() - queue is full";
                    }
                }
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
    ListBackedResourcePool(Supplier<T> factory, Consumer<T> releaser, Consumer<T> destructor, int minCapacity, int maxCapacity) {
        LangUtil.checkArg(minCapacity >= 0, "minCapacity must be >= 0");
        LangUtil.checkArg(maxCapacity >= minCapacity, "maxCapacity must be >= minCapacity %d: %d", minCapacity, maxCapacity);

        this.factory = factory;
        this.releaser = releaser;
        this.destructor = destructor;
        this.minCapacity = minCapacity;
        this.maxCapacity = maxCapacity;
        this.queue = new ArrayBlockingQueue<>(maxCapacity);

        for (int i = 0; i < minCapacity; i++) {
            queue.add(new BlockingLeaseImpl(factory.get(), releaser));
        }

        this.resourceCount = queue.size();
        this.waitingCount = 0;
    }

    private boolean isClosed() {
        return closeLock != null;
    }

    // resource is returned to the caller; closing it is the responsibility of the caller
    @SuppressWarnings({"resource", "java:S2095"})
    @Override
    public Lease<T> acquire() {
        try {
            if (isClosed()) {
                throw new IllegalStateException("pool is closed");
            }

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
                    assert resourceCount <= maxCapacity : "internal error: acquire() - resourceCount > maxCapacity";
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
                    assert waitingCount >= 0 : "internal error: acquire() - waitingCount < 0";
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new WrappedException(e);
        }
    }

    // resource is returned to the caller; closing it is the responsibility of the caller
    @SuppressWarnings({"resource", "java:S2095"})
    @Override
    public @Nullable Lease<T> tryAcquire() {
        if (isClosed()) {
            throw new IllegalStateException("pool is closed");
        }

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
                assert resourceCount <= maxCapacity : "internal error: tryAcquire() - resourceCount > maxCapacity";
                return lease.acquire();
            }
        }

        return null;
    }

    @SuppressWarnings({"resource", "java:S2095"})
    @Override
    public void close() {
        synchronized (lock) {
            if (isClosed()) {
                throw new IllegalStateException("pool is already closed");
            }

            closeLock = new Semaphore(1);

            while (queue.size() > waitingCount) {
                BlockingLeaseImpl pulled = queue.poll();
                assert pulled != null : "internal error: close() - queue is empty";
                try {
                    T resource = pulled.get();
                    pulled.dispose();
                    destructor.accept(resource);
                } catch (RuntimeException e) {
                    LOG.warn(LeaseImpl.EXCEPTION_WHEN_DESTRUCTING_RESOURCE_IGNORED, e.getMessage(), e);
                }
                resourceCount--;
            }

            if (waitingCount + resourceCount > 0) {
                closeLock.acquireUninterruptibly();
            }
        }

        closeLock.acquireUninterruptibly();
        queue.clear();
        closeLock.release();
    }
}
