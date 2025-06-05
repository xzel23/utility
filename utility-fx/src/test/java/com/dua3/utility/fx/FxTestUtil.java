package com.dua3.utility.fx;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Utility class providing methods for testing JavaFX-based components.
 * This class is designed to assist in running tasks on the JavaFX Application Thread
 * while ensuring they complete before proceeding, which is particularly useful in test environments.
 */
public final class FxTestUtil {

    /**
     * Utility class construvtor.
     */
    private FxTestUtil() {
        // utility class
    }

    /**
     * Helper method to run code on the JavaFX Application Thread and wait for it to complete.
     * Any {@link Throwable} thrown will be rethrown on the calling thread.
     *
     * @param runnable The code to run
     * @throws Throwable If the thread is interrupted while waiting
     */
    public static void runOnFxThreadAndWait(Runnable runnable) throws Throwable {
        CountDownLatch latch = new CountDownLatch(1);

        AtomicReference<Throwable> exception = new AtomicReference<>();
        PlatformHelper.runLater(() -> {
            try {
                runnable.run();
            } catch (Throwable t) {
                exception.set(t);
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS), "JavaFX operation timed out");
        if (exception.get() != null) {
            throw exception.get();
        }
    }
}
