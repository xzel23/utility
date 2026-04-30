package com.dua3.utility.fx;

import com.dua3.utility.lang.LangUtil;
import javafx.application.Platform;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
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
    private PlatformHelper() { /* utility class */ }

    /**
     * Run a task on the JavaFX application thread and wait for completion.
     * Consider using {@link #runLater(Runnable)} to avoid executing tasks out of order.
     *
     * @param action the task to run
     * @throws NullPointerException if {@code action} is {@code null}
     */
    public static void runAndWait(Runnable action) {
        // delegate to overload
        runAndWait(() -> {
            try {
                action.run();
            } catch (Exception e) {
                LOG.warn("unexpected exception in runAndWait: {}", e.getMessage(), e);
            }
            return null;
        });
    }

    /**
     * Run a task on the JavaFX application thread and return result.
     * <p>
     * If the task throws an exception or error, it will be rethrown on the calling thread.
     *
     * @param <T>    the result type
     * @param action the task to run
     * @return the result returned by action
     * @throws NullPointerException if {@code action} is {@code null}
     *
     */
    public static <T extends @Nullable Object> T runAndWait(Supplier<T> action) {
        // run synchronously when on JavaFX thread
        if (Platform.isFxApplicationThread()) {
            return action.get();
        }

        // queue on JavaFX thread and wait for completion
        AtomicReference<T> result = new AtomicReference<>();
        final CountDownLatch doneLatch = new CountDownLatch(1);
        @Nullable Throwable[] thrown = new Throwable[1];
        Platform.runLater(() -> {
            try {
                result.set(action.get());
            } catch (Exception e) {
                thrown[0] = e;
            } finally {
                doneLatch.countDown();
            }
        });
        if (thrown[0] != null) {
            LangUtil.sneakyThrow(thrown[0]);
        }

        while (doneLatch.getCount() > 0) {
            try {
                doneLatch.await();
            } catch (InterruptedException e) {
                LOG.trace("interrupted", e);
                Thread.currentThread().interrupt();
            }
        }

        return result.get();
    }

    /**
     * Run a task on the JavaFX application thread and log any exceptions.
     *
     * @param action the task to run
     * @throws NullPointerException if {@code action} is {@code null}
     */
    public static void runLater(Runnable action) {
        runLater(action, t -> LOG.warn("unexpected exception in runLater: {}", t.getMessage(), t));
    }

    /**
     * Run a task on the JavaFX application thread and handle any exceptions.
     * <p>
     * Note that the error handler is called on the JavaFX application thread son there is
     * no use in simply mapping the exception to a different type. Also it is not possible
     * to pass the exception to the caller as the task is run asynchronously.
     * <p>
     * If the exception should be thrown to the caller, {@link #runAndWait(Runnable)} has
     * to be used instead.
     *
     * @param action the task to run
     * @param errorHandler the error handler to use
     * @throws NullPointerException if {@code action} is {@code null}
     */
    public static void runLater(Runnable action, Consumer<Throwable> errorHandler) {
        Platform.runLater(() -> {
            try {
                action.run();
            } catch (Throwable t) {
                errorHandler.accept(t);
            }
        });
    }

    /**
     * Checks if the current thread is the FX Application Thread.
     * Throws an exception if it is not.
     */
    public static void checkApplicationThread() {
        if (!Platform.isFxApplicationThread()) {
            LOG.error("not on FX Application Thread");
            throw new IllegalStateException("not on FX Application Thread");
        }
    }

}
