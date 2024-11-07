package com.dua3.utility.fx;

import com.dua3.utility.lang.LangUtil;
import javafx.application.Platform;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * The PlatformHelper class provides utility methods for performing tasks on the JavaFX application thread.
 * It provides methods for running tasks synchronously or asynchronously on the JavaFX application thread,
 * as well as checking if the current thread is the FX Application Thread.
 */
public final class PlatformHelper {

    /**
     * Logger instance
     */
    private static final Logger LOG = LogManager.getLogger(PlatformHelper.class);

    /**
     * Utility class private constructor.
     */
    private PlatformHelper() {
        // utility class
    }

    /**
     * Run a task on the JavaFX application thread and wait for completion.
     * Consider using {@link #runLater(Runnable)} to avoid executing tasks out of order.
     *
     * @param action the task to run
     * @throws NullPointerException if {@code action} is {@code null}
     */
    public static void runAndWait(Runnable action) {
        runAndWait(() -> {
            action.run();
            //noinspection ReturnOfNull - by design
            return null;
        });
    }

    /**
     * Run a task on the JavaFX application thread and return result.
     *
     * @param <T>    the result type
     * @param action the task to run
     * @return the result returned by action
     * @throws NullPointerException if {@code action} is {@code null}
     */
    public static <T> T runAndWait(Supplier<T> action) {
        // run synchronously on JavaFX thread
        if (Platform.isFxApplicationThread()) {
            return action.get();
        }

        // queue on JavaFX thread and wait for completion
        AtomicReference<T> result = new AtomicReference<>();
        final CountDownLatch doneLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                result.set(action.get());
            } finally {
                doneLatch.countDown();
            }
        });

        while (doneLatch.getCount() > 0) {
            try {
                doneLatch.await();
            } catch (InterruptedException e) {
                LOG.debug("interrupted", e);
                Thread.currentThread().interrupt();
            }
        }

        return result.get();
    }

    /**
     * Run a task on the JavaFX application thread.
     *
     * @param action the task to run
     * @throws NullPointerException if {@code action} is {@code null}
     */
    public static void runLater(Runnable action) {
        Platform.runLater(action);
    }

    /**
     * Checks if the current thread is the FX Application Thread.
     * Throws an exception if it is not.
     */
    public static void checkApplicationThread() {
        LangUtil.check(Platform.isFxApplicationThread(), "not on FX Application Thread");
    }

}
