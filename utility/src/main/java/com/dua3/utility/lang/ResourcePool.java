package com.dua3.utility.lang;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Represents a pool of reusable resources for maintaining efficient resource management.
 *
 * @param <T> the type of resource managed by the pool
 */
public interface ResourcePool<T> {
    interface Lease<T> extends AutoCloseable {
        /** @return the managed resource */
        T get();

        /** Resets the resource for the next use. */
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
    public static <T> ResourcePool<T> newThreadBasedPool(Supplier<T> factory, Consumer<T> releaser) {
        return new ThreadResourcePool<>(factory, releaser);
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
}

/**
 * Implementation of a thread-local resource pool that provides a unique resource per thread.
 * Each thread has exclusive access to its resource, which is managed via the {@link Lease} interface.
 *
 * @param <T> the type of resource managed by the pool
 */
final class ThreadResourcePool<T> implements ResourcePool<T> {
    private static final Logger LOG = LogManager.getLogger(ThreadResourcePool.class);
    private final ThreadLocal<LeaseImpl> threadLocalLease;

    ThreadResourcePool(Supplier<T> factory, Consumer<T> releaser) {
        // We store the wrapper itself in the ThreadLocal
        this.threadLocalLease = ThreadLocal.withInitial(() ->
                new LeaseImpl(factory.get(), releaser)
        );
    }

    @Override
    public Lease<T> acquire() {
        LeaseImpl lease = threadLocalLease.get();
        if (lease.leased) {throw new IllegalStateException("resource already leased");}
        lease.leased = true;
        return lease;
    }

    // Inner class is instantiated only ONCE per thread
    private final class LeaseImpl implements Lease<T> {
        private final T resource;
        private final Consumer<T> releaser;
        private boolean leased = false;

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
    }
}
