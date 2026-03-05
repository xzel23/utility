package com.dua3.utility.lang;

import org.junit.jupiter.api.Test;

import java.time.Duration;
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

        try (ResourcePool<Object> pool = ResourcePool.newThreadBasedResourcePool(factory, releaser)) {
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
    }

    @Test
    void nonReentrantAcquireSameThread() {
        try (ResourcePool<Object> pool = ResourcePool.newThreadBasedResourcePool(Object::new, r -> {})) {

            ResourcePool.Lease<Object> lease = pool.acquire();
            // cannot acquire again in the same thread until closed
            assertThrows(IllegalStateException.class, pool::acquire, "Second acquire in the same thread must fail while leased");

            // after closing, acquiring should work again
            lease.close();
            assertDoesNotThrow(() -> {
                try (ResourcePool.Lease<Object> ignored = pool.acquire()) {
                    // no-op
                }
            });

        }
    }

    @Test
    void doubleCloseThrows() {
        try (ResourcePool<Object> pool = ResourcePool.newThreadBasedResourcePool(Object::new, r -> {})) {
            ResourcePool.Lease<Object> lease = pool.acquire();
            lease.close();
            assertThrows(IllegalStateException.class, lease::close, "Closing an already closed lease must fail");
        }
    }

    @Test
    void releaserExceptionIsSwallowedAndLeaseCanBeReacquired() {
        AtomicInteger released = new AtomicInteger();
        Consumer<Object> badReleaser = r -> {
            released.incrementAndGet();
            throw new RuntimeException("boom");
        };
        try (ResourcePool<Object> pool = ResourcePool.newThreadBasedResourcePool(Object::new, badReleaser)) {
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
    }

    @Test
    void perThreadIsolationDifferentInstances() throws InterruptedException {
        AtomicReference<Object> res1 = new AtomicReference<>();
        AtomicReference<Object> res2 = new AtomicReference<>();
        AtomicInteger created = new AtomicInteger();
        CountDownLatch acquired = new CountDownLatch(2);
        CountDownLatch finished = new CountDownLatch(2);

        try (ResourcePool<Object> pool = ResourcePool.newThreadBasedResourcePool(() -> {
            created.incrementAndGet();
            return new Object();
        }, r -> {})) {
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

    @Test
    void fixedSizePoolBasics() {
        AtomicInteger created = new AtomicInteger();
        try (ResourcePool<Object> pool = ResourcePool.newFixedSizeResourcePool(
                () -> {
                    created.incrementAndGet();
                    return new Object();
                },
                r -> {
                },
                1
        )) {
            assertEquals(1, created.get(), "Should pre-create resource for fixed-size pool");

            Object r1;
            try (var lease = pool.acquire()) {
                r1 = lease.get();
                assertNotNull(r1);
            }

            try (var lease = pool.acquire()) {
                assertSame(r1, lease.get(), "Should reuse the resource that was just returned");
            }
        }
    }

    @Test
    void fixedSizePoolPreCreation() {
        AtomicInteger created = new AtomicInteger();
        try (var pool = ResourcePool.newFixedSizeResourcePool(
                () -> {
                    created.incrementAndGet();
                    return new Object();
                },
                r -> {
                },
                3
        )) {
            assertEquals(3, created.get(), "Should pre-create all resources for fixed size pool");
        }
    }

    @Test
    void fixedSizePoolExhaustion() throws InterruptedException {
        try (ResourcePool<Object> pool = ResourcePool.newFixedSizeResourcePool(Object::new, r -> {
        }, 1)) {

            var lease1 = pool.acquire();

            AtomicReference<ResourcePool.Lease<Object>> lease2 = new AtomicReference<>();
            CountDownLatch latch = new CountDownLatch(1);
            Thread t = new Thread(() -> {
                lease2.set(pool.acquire());
                latch.countDown();
            });
            t.start();

            assertFalse(latch.await(100, TimeUnit.MILLISECONDS), "Second acquire should block");

            lease1.close();

            assertTrue(latch.await(1, TimeUnit.SECONDS), "Second acquire should succeed after first is closed");
            assertNotNull(lease2.get());
            lease2.get().close();
        }
    }

    @Test
    void variableSizePoolGrowthAndShrinkage() {
        AtomicInteger created = new AtomicInteger();
        AtomicInteger released = new AtomicInteger();
        try (ResourcePool<Object> pool = ResourcePool.newResourcePool(
                () -> {
                    created.incrementAndGet();
                    return new Object();
                },
                r -> released.incrementAndGet(),
                1, 2
        )) {

            assertEquals(1, created.get(), "Should pre-create minCapacity resources");

            var lease1 = pool.acquire();
            assertEquals(1, created.get());

            var lease2 = pool.acquire();
            assertEquals(2, created.get(), "Should create second resource up to maxCapacity");

            // now we have 2 resources (maxCapacity reached)

            lease1.close();
            assertEquals(1, released.get());
            // Since resourceCount (2) > minCapacity (1) and waitingCount == 0, resourceCount becomes 1 (resource retired)

            lease2.close();
            assertEquals(2, released.get());
            // Since resourceCount (1) == minCapacity (1), it should be put back in the queue

            // Acquiring again should reuse the remaining resource in the queue
            try (var lease3 = pool.acquire()) {
                assertEquals(2, created.get(), "No new resource should be created");
            }
        }
    }

    @Test
    void interruptionDuringAcquire() throws InterruptedException {
        try (ResourcePool<Object> pool = ResourcePool.newFixedSizeResourcePool(Object::new, r -> {
        }, 1)) {
            var lease1 = pool.acquire();

            CountDownLatch latch = new CountDownLatch(1);
            AtomicReference<Throwable> error = new AtomicReference<>();
            Thread t = new Thread(() -> {
                try {
                    pool.acquire();
                } catch (Throwable e) {
                    error.set(e);
                } finally {
                    latch.countDown();
                }
            });
            t.start();

            t.interrupt();

            assertTrue(latch.await(1, TimeUnit.SECONDS));
            assertInstanceOf(WrappedException.class, error.get());
            assertInstanceOf(InterruptedException.class, error.get().getCause());

            lease1.close();
        }
    }

    @Test
    void invalidCapacities() {
        assertThrows(IllegalArgumentException.class, () -> ResourcePool.newFixedSizeResourcePool(Object::new, r -> {
        }, -1));
        assertThrows(IllegalArgumentException.class, () -> ResourcePool.newResourcePool(Object::new, r -> {
        }, 2, 1));
        assertThrows(IllegalArgumentException.class, () -> ResourcePool.newResourcePool(Object::new, r -> {
        }, -1, 1));
    }

    @Test
    void concurrentAccess() throws InterruptedException {
        int threadCount = 10;
        int poolSize = 3;
        int iterations = 100;
        try (ResourcePool<Integer> pool = ResourcePool.newFixedSizeResourcePool(new Supplier<>() {
            int next = 0;
            @Override
            public synchronized Integer get() {
                return next++;
            }
        }, r -> {}, poolSize)) {

            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch endLatch = new CountDownLatch(threadCount);
            AtomicInteger errorCount = new AtomicInteger();

            for (int i = 0; i < threadCount; i++) {
                new Thread(() -> {
                    try {
                        startLatch.await();
                        for (int j = 0; j < iterations; j++) {
                            try (var lease = pool.acquire()) {
                                assertNotNull(lease.get());
                                Thread.yield();
                            }
                        }
                    } catch (RuntimeException e) {
                        errorCount.incrementAndGet();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        errorCount.incrementAndGet();
                    } finally {
                        endLatch.countDown();
                    }
                }).start();
            }

            startLatch.countDown();
            assertTrue(endLatch.await(20, TimeUnit.SECONDS)); // Windows CI is slow, give it enough time
            assertEquals(0, errorCount.get(), "No errors should occur during concurrent access");
        }
    }

    @Test
    void listBackedPoolAllowsMultipleAcquiresFromSameThread() {
        try (ResourcePool<Object> pool = ResourcePool.newFixedSizeResourcePool(Object::new, r -> {
        }, 2)) {

            ResourcePool.Lease<Object> lease1 = pool.acquire();
            ResourcePool.Lease<Object> lease2 = pool.acquire();

            assertNotNull(lease1);
            assertNotNull(lease2);
            assertNotSame(lease1.get(), lease2.get());

            lease1.close();
            lease2.close();
        }
    }

    @Test
    void threadResourcePoolTryAcquire() {
        try (ResourcePool<Object> pool = ResourcePool.newThreadBasedResourcePool(Object::new, r -> {})) {

            try (ResourcePool.Lease<Object> lease1 = pool.tryAcquire()) {
                assertNotNull(lease1, "tryAcquire should succeed when no lease is held");
                assertNotNull(lease1.get());

                assertNull(pool.tryAcquire(), "tryAcquire should return null when already leased in this thread");
            }

            try (ResourcePool.Lease<Object> lease2 = pool.tryAcquire()) {
                assertNotNull(lease2, "tryAcquire should succeed after closing previous lease");
            }
        }
    }

    @Test
    void fixedSizePoolTryAcquire() {
        try (ResourcePool<Object> pool = ResourcePool.newFixedSizeResourcePool(Object::new, r -> {}, 1)) {

            try (ResourcePool.Lease<Object> lease1 = pool.tryAcquire()) {
                assertNotNull(lease1, "tryAcquire should succeed when resource is available");

                assertNull(pool.tryAcquire(), "tryAcquire should return null when pool is exhausted");
            }

            try (ResourcePool.Lease<Object> lease2 = pool.tryAcquire()) {
                assertNotNull(lease2, "tryAcquire should succeed after resource is returned to pool");
            }
        }
    }

    @Test
    void variableSizePoolTryAcquireGrowth() {
        try (ResourcePool<Object> pool = ResourcePool.newResourcePool(Object::new, r -> {}, 1, 2)) {

            // First one should succeed (from minCapacity=1)
            ResourcePool.Lease<Object> lease1 = pool.tryAcquire();
            assertNotNull(lease1, "tryAcquire should succeed when resource is in queue");

            // Second one: should grow the pool up to maxCapacity=2
            try (ResourcePool.Lease<Object> lease2 = pool.tryAcquire()) {
                assertNotNull(lease2, "tryAcquire should grow the pool up to maxCapacity");
            }

            lease1.close();
        }
    }

    @Test
    void closeBlocksUntilAllLeasesReturned() throws InterruptedException {
        AtomicInteger released = new AtomicInteger();
        ResourcePool<Object> pool = ResourcePool.newFixedSizeResourcePool(Object::new, r -> released.incrementAndGet(), 2);

        var lease1 = pool.acquire();
        var lease2 = pool.acquire();

        CountDownLatch closeFinished = new CountDownLatch(1);
        Thread closerThread = new Thread(() -> {
            pool.close();
            closeFinished.countDown();
        });
        closerThread.start();

        // Ensure close() is actually blocking
        assertFalse(closeFinished.await(200, TimeUnit.MILLISECONDS), "close() should block while leases are out");

        lease1.close();
        assertFalse(closeFinished.await(100, TimeUnit.MILLISECONDS), "close() should still block while 1 lease remains");

        lease2.close();
        assertTrue(closeFinished.await(1, TimeUnit.SECONDS), "close() should complete after final lease is returned");
        assertEquals(2, released.get(), "Both resources should have triggered the releaser");
    }

    @Test
    void closeHandlesIdleResourcesInQueue() {
        AtomicInteger released = new AtomicInteger();
        // Pool with 5 idle resources
        ResourcePool<Object> pool = ResourcePool.newFixedSizeResourcePool(Object::new, r -> released.incrementAndGet(), 5);

        // All resources are currently in the queue.
        // close() should drain them and complete immediately (or very fast)
        assertTimeoutPreemptively(Duration.ofSeconds(1), pool::close);
    }

    @Test
    void closeWithWaitersSatisfiesThemFirst() throws InterruptedException {
        ResourcePool<Object> pool = ResourcePool.newFixedSizeResourcePool(Object::new, r -> {}, 1);

        // Hold the only resource
        var lease1 = pool.acquire();

        // Thread B is waiting
        CountDownLatch waiterAcquired = new CountDownLatch(1);
        Thread waiterThread = new Thread(() -> {
            try (var lease2 = pool.acquire()) {
                waiterAcquired.countDown();
            }
        });
        waiterThread.start();

        // Thread A calls close
        CountDownLatch closeFinished = new CountDownLatch(1);
        new Thread(() -> {
            pool.close();
            closeFinished.countDown();
        }).start();

        // Release the resource. Waiter should get it, finish, THEN close should finish.
        lease1.close();

        assertTrue(waiterAcquired.await(1, TimeUnit.SECONDS), "Waiter should have finished work before pool closed");
        assertTrue(closeFinished.await(1, TimeUnit.SECONDS), "Pool should close after waiter returns its lease");
    }

    @Test
    void acquireAfterCloseThrows() {
        ResourcePool<Object> pool = ResourcePool.newFixedSizeResourcePool(Object::new, r -> {}, 1);
        pool.close();

        assertThrows(IllegalStateException.class, pool::acquire, "Acquiring from a closed pool must fail");
        assertThrows(IllegalStateException.class, pool::tryAcquire, "tryAcquire from a closed pool must fail");
    }

    @Test
    void listBackedPoolDestroysAllResourcesOnClose() {
        AtomicInteger destroyed = new AtomicInteger();
        int size = 3;

        ResourcePool<Object> pool = ResourcePool.newFixedSizeResourcePool(
                Object::new,
                r -> {},
                r -> destroyed.incrementAndGet(),
                size
        );

        // One active lease
        var lease = pool.acquire();

        // Close the pool
        // This should immediately destroy the 2 idle resources and wait for the 1 active
        new Thread(lease::close).start();

        pool.close();

        assertEquals(size, destroyed.get(), "All resources should be destroyed exactly once");
    }

    @Test
    void threadBasedPoolDestroysAllResourcesOnClose() throws InterruptedException {
        AtomicInteger destroyed = new AtomicInteger();
        ResourcePool<Object> pool = ResourcePool.newThreadBasedResourcePool(
                Object::new,
                r -> {},
                r -> destroyed.incrementAndGet()
        );

        CountDownLatch t1Started = new CountDownLatch(1);
        CountDownLatch t2Started = new CountDownLatch(1);

        // Thread 1 acquires a resource
        Thread t1 = new Thread(() -> {
            pool.acquire();
            t1Started.countDown();
        });

        // Thread 2 acquires a resource
        Thread t2 = new Thread(() -> {
            pool.acquire();
            t2Started.countDown();
        });

        t1.start();
        t2.start();
        assertTrue(t1Started.await(1, TimeUnit.SECONDS));
        assertTrue(t2Started.await(1, TimeUnit.SECONDS));

        // Current thread also acquires one
        pool.acquire();

        // Close the pool. It should use the tracking set to kill resources in t1, t2, and main.
        pool.close();

        assertEquals(3, destroyed.get(), "Resources in all threads should be destroyed");
    }

    @Test
    void destructorExceptionsAreSwallowed() {
        Consumer<Object> badDestructor = r -> {
            throw new RuntimeException("Final boom");
        };

        ResourcePool<Object> pool = ResourcePool.newFixedSizeResourcePool(
                Object::new,
                r -> {},
                badDestructor,
                2
        );

        // Should not throw even if destructor fails
        assertDoesNotThrow(pool::close, "Exceptions in destructor must not prevent pool closure");
    }

    @Test
    void resourceIsDisposedAfterDestruction() {
        ResourcePool<Object> pool = ResourcePool.newFixedSizeResourcePool(Object::new, r -> {}, 1);

        var lease = pool.acquire();
        Object rawResource = lease.get();
        assertNotNull(rawResource);

        new Thread(() -> {
            try {Thread.sleep(500);} catch (InterruptedException ignored) {/* do nothing */}
            lease.close();
        }).start();

        pool.close();

        // After pool is closed and lease is returned/destructed
        assertThrows(NullPointerException.class, lease::get, "Lease.get() should throw after resource is disposed");
    }
}
