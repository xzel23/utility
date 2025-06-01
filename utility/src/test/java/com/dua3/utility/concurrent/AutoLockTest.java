package com.dua3.utility.concurrent;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.jupiter.api.Assertions.*;

class AutoLockTest {

    @Test
    void testAutoLockAcquiresAndReleasesLock() {
        // Create a mock Lock that tracks lock/unlock calls
        MockLock mockLock = new MockLock();

        // Initially the lock should not be locked
        assertFalse(mockLock.isLocked());

        // Create an AutoLock with the mock lock
        try (AutoLock autoLock = AutoLock.of(mockLock)) {
            // The lock should be acquired when AutoLock is created
            assertTrue(mockLock.isLocked());
        }

        // The lock should be released when AutoLock is closed
        assertFalse(mockLock.isLocked());
    }

    @Test
    void testAutoLockWithName() {
        // Create a mock Lock that tracks lock/unlock calls
        MockLock mockLock = new MockLock();

        // Create an AutoLock with a name
        try (AutoLock autoLock = AutoLock.of(mockLock, "testLock")) {
            // The lock should be acquired
            assertTrue(mockLock.isLocked());
        }

        // The lock should be released
        assertFalse(mockLock.isLocked());
    }

    @Test
    void testAutoLockWithNameSupplier() {
        // Create a mock Lock that tracks lock/unlock calls
        MockLock mockLock = new MockLock();

        // Create an AutoLock with a name supplier
        try (AutoLock autoLock = AutoLock.of(mockLock, () -> "dynamicLockName")) {
            // The lock should be acquired
            assertTrue(mockLock.isLocked());
        }

        // The lock should be released
        assertFalse(mockLock.isLocked());
    }

    @Test
    void testAutoLockWithRealLock() {
        // Use a real ReentrantLock
        ReentrantLock lock = new ReentrantLock();

        // Initially the lock should not be locked
        assertFalse(lock.isLocked());

        // Create an AutoLock with the real lock
        try (AutoLock autoLock = AutoLock.of(lock)) {
            // The lock should be acquired
            assertTrue(lock.isLocked());

            // We should be able to unlock and relock manually
            // since ReentrantLock allows the same thread to acquire the lock multiple times
            lock.unlock();
            assertFalse(lock.isLocked());
            lock.lock();
            assertTrue(lock.isLocked());
        }

        // The lock should be released when AutoLock is closed
        assertFalse(lock.isLocked());
    }

    @Test
    void testNestedAutoLocks() {
        // Use a real ReentrantLock which supports reentrant locking
        ReentrantLock lock = new ReentrantLock();

        // Initially the lock should not be locked
        assertFalse(lock.isLocked());

        // Create nested AutoLocks
        try (AutoLock outerLock = AutoLock.of(lock)) {
            // The lock should be acquired
            assertTrue(lock.isLocked());
            assertEquals(1, lock.getHoldCount());

            try (AutoLock innerLock = AutoLock.of(lock)) {
                // The lock should still be acquired, with hold count = 2
                assertTrue(lock.isLocked());
                assertEquals(2, lock.getHoldCount());
            }

            // After inner lock is closed, the lock should still be held by outer lock
            assertTrue(lock.isLocked());
            assertEquals(1, lock.getHoldCount());
        }

        // After outer lock is closed, the lock should be released
        assertFalse(lock.isLocked());
        assertEquals(0, lock.getHoldCount());
    }

    /**
     * A mock Lock implementation that tracks lock/unlock calls
     */
    private static class MockLock implements Lock {
        private final AtomicBoolean locked = new AtomicBoolean(false);

        @Override
        public void lock() {
            locked.set(true);
        }

        @Override
        public void unlock() {
            locked.set(false);
        }

        public boolean isLocked() {
            return locked.get();
        }

        // Other methods not used in this test
        @Override
        public void lockInterruptibly() {
            throw new UnsupportedOperationException("Not implemented for test");
        }

        @Override
        public boolean tryLock() {
            throw new UnsupportedOperationException("Not implemented for test");
        }

        @Override
        public boolean tryLock(long time, java.util.concurrent.TimeUnit unit) {
            throw new UnsupportedOperationException("Not implemented for test");
        }

        @Override
        public java.util.concurrent.locks.Condition newCondition() {
            throw new UnsupportedOperationException("Not implemented for test");
        }
    }
}