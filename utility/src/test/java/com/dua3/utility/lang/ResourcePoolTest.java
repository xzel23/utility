package com.dua3.utility.lang;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

class ResourcePoolTest {

    @Test
    void acquireAndReuseSameThread() {
        AtomicInteger created = new AtomicInteger();
        AtomicInteger released = new AtomicInteger();
        AtomicReference<Object> lastReleased = new AtomicReference<>();

        Supplier<Object> factory = () -> {
            created.incrementAndGet();
            return new Object();
        };
        Consumer<Object> releaser = r -> {
            released.incrementAndGet();
            lastReleased.set(r);
        };

        ResourcePool<Object> pool = ResourcePool.newThreadBasedPool(factory, releaser);

        // first lease
        Object r1;
        try (ResourcePool.Lease<Object> lease = pool.acquire()) {
            r1 = lease.get();
            assertNotNull(r1);
            assertEquals(1, created.get(), "Factory should be called once on first acquire in thread");
            assertEquals(0, released.get(), "Releaser should not have been called yet");
        }
        assertEquals(1, released.get(), "Releaser should be called once after close");
        assertSame(r1, lastReleased.get(), "Released resource should be the same instance as acquired");

        // second lease in same thread reuses instance
        try (ResourcePool.Lease<Object> lease = pool.acquire()) {
            Object r2 = lease.get();
            assertSame(r1, r2, "Resource should be reused within the same thread");
        }
        assertEquals(2, released.get(), "Releaser should be called again after second close");
        assertEquals(1, created.get(), "Factory must not be called again within the same thread");
    }

    @Test
    void nonReentrantAcquireSameThread() {
        ResourcePool<Object> pool = ResourcePool.newThreadBasedPool(Object::new, r -> {});

        ResourcePool.Lease<Object> lease = pool.acquire();
        // cannot acquire again in same thread until closed
        assertThrows(IllegalStateException.class, pool::acquire, "Second acquire in same thread must fail while leased");

        // after closing, acquiring should work again
        lease.close();
        assertDoesNotThrow(() -> {
            try (ResourcePool.Lease<Object> ignored = pool.acquire()) {
                // no-op
            }
        });
    }

    @Test
    void doubleCloseThrows() {
        ResourcePool<Object> pool = ResourcePool.newThreadBasedPool(Object::new, r -> {});

        ResourcePool.Lease<Object> lease = pool.acquire();
        lease.close();
        assertThrows(IllegalStateException.class, lease::close, "Closing an already closed lease must fail");
    }

    @Test
    void releaserExceptionIsSwallowedAndLeaseCanBeReacquired() {
        AtomicInteger released = new AtomicInteger();
        Consumer<Object> badReleaser = r -> {
            released.incrementAndGet();
            throw new RuntimeException("boom");
        };
        ResourcePool<Object> pool = ResourcePool.newThreadBasedPool(Object::new, badReleaser);

        // close must not propagate exception
        ResourcePool.Lease<Object> lease1 = pool.acquire();
        assertDoesNotThrow(lease1::close, "Exceptions from releaser must be swallowed");
        assertEquals(1, released.get());

        // should be able to acquire again
        try (ResourcePool.Lease<Object> ignored = pool.acquire()) {
            // no-op
        }
        assertEquals(2, released.get());
    }

    @Test
    void perThreadIsolationDifferentInstances() throws InterruptedException {
        AtomicReference<Object> res1 = new AtomicReference<>();
        AtomicReference<Object> res2 = new AtomicReference<>();
        AtomicInteger created = new AtomicInteger();
        CountDownLatch acquired = new CountDownLatch(2);
        CountDownLatch finished = new CountDownLatch(2);

        ResourcePool<Object> pool = ResourcePool.newThreadBasedPool(() -> {
            created.incrementAndGet();
            return new Object();
        }, r -> {});

        Runnable task1 = () -> {
            try (ResourcePool.Lease<Object> lease = pool.acquire()) {
                res1.set(lease.get());
            } finally {
                finished.countDown();
                acquired.countDown();
            }
        };
        Runnable task2 = () -> {
            try (ResourcePool.Lease<Object> lease = pool.acquire()) {
                res2.set(lease.get());
            } finally {
                finished.countDown();
                acquired.countDown();
            }
        };

        Thread t1 = new Thread(task1, "t1");
        Thread t2 = new Thread(task2, "t2");
        t1.start();
        t2.start();

        // wait until both acquired once
        assertTrue(acquired.await(5, TimeUnit.SECONDS), "Both threads should acquire within timeout");
        // ensure threads finished
        assertTrue(finished.await(5, TimeUnit.SECONDS));

        assertNotNull(res1.get());
        assertNotNull(res2.get());
        assertNotSame(res1.get(), res2.get(), "Different threads must see different resource instances");
        assertEquals(2, created.get(), "Factory should be called once per thread");
    }
}
