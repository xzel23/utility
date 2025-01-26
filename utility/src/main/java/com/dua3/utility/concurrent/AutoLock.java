package com.dua3.utility.concurrent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.locks.Lock;
import java.util.function.Supplier;

/**
 * The {@code AutoLock} class is a utility designed for safely managing locks
 * using the auto-closeable pattern. It ensures that the lock is acquired
 * when the object is created and automatically released when the object
 * is closed.
 *
 * <p>This class is useful for managing critical sections where the lock's state
 * should be reliably controlled without manual intervention to avoid issues
 * such as deadlocks or forgotten unlocks.
 *
 * <p>The {@code AutoLock} class relies on the {@link Lock} interface for locking
 * behavior, which should be provided during its creation.</p>
 *
 * <p>Logging is performed during the creation and release of a lock, providing
 * trace-level information about the lock's lifecycle and its associated name,
 * if provided. This is helpful for debugging purposes and understanding the
 * flow of lock usage.</p>
 *
 * <p>To create an instance of {@code AutoLock}, use one of the static factory
 * methods. The lock can be associated with an optional name for traceability
 * or simply remain unnamed.</p>
 *
 * <p>Upon successful object creation, the provided lock is acquired, and it
 * remains locked until the {@link #close()} method is called, either explicitly
 * or implicitly (e.g., via a try-with-resources statement).</p>
 */
public class AutoLock implements AutoCloseable {
    private static final Logger LOG = LogManager.getLogger(AutoLock.class);

    private final Lock lock;
    private final Supplier<String> name;

    /**
     * Creates an {@code AutoLock} instance and acquires the specified lock immediately.
     * The lock will remain acquired until the {@link AutoLock#close()} method is invoked
     * (e.g., via a try-with-resources block or explicit manual call to close()).
     *
     * <p>When using this method, the {@code AutoLock} instance is unnamed and
     * trace logs will refer to it as "unnamed".</p>
     *
     * @param lock the {@link Lock} instance to be managed by the created
     * @return a new {@code AutoLock} instance that manages the specified lock.
     * @throws NullPointerException if the provided lock is {@code null}.
     */
    public static AutoLock of(Lock lock) {
        return of(lock, "unnamed");
    }

    /**
     * Creates an {@code AutoLock} instance and acquires the specified lock immediately.
     * The lock will remain acquired until the {@link AutoLock#close()} method is invoked
     * (e.g., via a try-with-resources block or explicit manual call to close()).
     *
     * <p>The provided name is used for trace-level logging, aiding in identifying
     * the lock instance and its lifecycle for debugging purposes.</p>
     *
     * @param lock the {@code Lock} instance to be managed by this {@code AutoLock}.
     *             Must be non-null and provide thread-safe locking and unlocking behavior.
     * @param name a descriptive name for the lock, used for logging purposes.
     *             Must be non-null and convertible to a string representation.
     * @return an {@code AutoLock} instance managing the specified {@code Lock}
     *         with the provided name for traceability.
     * @throws NullPointerException if the provided {@code lock} or {@code name} is null.
     */
    public static AutoLock of(Lock lock, String name) {
        return new AutoLock(lock, name::toString);
    }

    /**
     * Creates an {@code AutoLock} instance for the specified {@link Lock} and a name supplier.
     * The returned {@code AutoLock} ensures that the provided lock is acquired upon creation
     * and will be released when the {@code AutoLock} is closed.
     *
     * @param lock the {@link Lock} instance to be managed, must not be {@code null}.
     * @param name a {@link Supplier<String>} providing a name for the lock, used for logging and traceability.
     * @return an {@code AutoLock} instance managing the specified lock with the associated name.
     * @throws NullPointerException if either {@code lock} or {@code name} is {@code null}.
     */
    public static AutoLock of(Lock lock, Supplier<String> name) {
        return new AutoLock(lock, name);
    }

    private AutoLock(Lock lock, Supplier<String> name) {
        this.lock = lock;
        this.name = name;

        LOG.trace("AutoLock({}): lock [{}] - {}", name::get, () -> System.identityHashCode(this), lock::toString);
        lock.lock();
    }

    /**
     * Releases the lock managed by this {@code AutoLock} instance.
     *
     * <p>This method is invoked automatically when the {@code AutoLock} instance
     * is used within a try-with-resources block, or it can be called manually
     * to release the lock. Trace-level logging is performed to record the lock
     * release operation, including the name of the lock (if provided) and its
     * identity hash code for debugging purposes.</p>
     *
     * <p>Once this method is called, the lock is no longer held by the calling thread.
     * It is essential to ensure that this method is invoked to prevent potential
     * deadlocks or resource leaks by retaining the lock unnecessarily.</p>
     */
    @Override
    public void close() {
        LOG.trace("AutoLock({}): unlock [{}]", name::get, () -> System.identityHashCode(this));
        lock.unlock();
    }
}
