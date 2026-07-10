package com.dua3.utility.fx;

import com.dua3.utility.lang.LangUtil;
import javafx.application.Platform;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
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
    private static final AtomicBoolean JVM_SHUTDOWN_IN_PROGRESS = new AtomicBoolean(false);

    static {
        // Mark JVM shutdown state.
        // Do not call Platform.exit() from this hook because it can block indefinitely during JVM shutdown.
        try {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                JVM_SHUTDOWN_IN_PROGRESS.set(true);
                LOG.debug("shutdown hook: JVM shutdown detected");
            }, "JavaFX-Shutdown-Hook"));
        } catch (IllegalStateException ignored) {
            // class initialization can happen while the JVM is already shutting down
            JVM_SHUTDOWN_IN_PROGRESS.set(true);
        }
    }

    /**
     * Utility class private constructor.
     */
    private PlatformHelper() { /* utility class */ }

    /**
     * Run a task on the JavaFX application thread and wait for completion.
     * <p>
     * <strong>Note:</strong>
     * <ul>
     * <li>This method will block the calling thread until the task completes.
     * <li>Any runtime exceptions thrown by the task will be rethrown by this method.
     * <li>Consider using {@link #runLater(Runnable)} to avoid executing tasks out of order.
     * </ul>
     *
     * @param action the task to run
     * @throws NullPointerException if {@code action} is {@code null}
     * @throws RuntimeException if the task throws one
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
     * Run a task on the JavaFX application thread and return the result.
     * <p>
     * <strong>Note:</strong>
     * <ul>
     * <li>This method will block the calling thread until the task completes.
     * <li>Any runtime exceptions thrown by the task will be rethrown by this method.
     * <li>Consider using {@link #runLater(Runnable)} to avoid executing tasks out of order.
     * </ul>
     *
     * @param <T>    the result type
     * @param action the task to run
     * @return the result returned by action
     * @throws NullPointerException if {@code action} is {@code null}
     * @throws RuntimeException if the task throws one
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

        while (doneLatch.getCount() > 0) {
            try {
                doneLatch.await();
            } catch (InterruptedException e) {
                LOG.trace("interrupted", e);
                Thread.currentThread().interrupt();
            }
        }

        if (thrown[0] != null) {
            LangUtil.sneakyThrow(thrown[0]);
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
    @SuppressWarnings("java:S1181")
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

    /**
     * Shut down the JavaFX platform.
     *
     * @param timeout the maximum time to wait for the platform to shut down
     * @param unit    the time unit of the timeout argument
     * @return true if the platform shut down within the timeout, false otherwise
     */
    public static boolean shutdown(long timeout, TimeUnit unit) {
        LOG.debug("Platform shutdown requested (timeout {} {})", timeout, unit);

        // During JVM shutdown, the JavaFX event thread may already be stopping and queued runLater tasks
        // might never execute. Avoid any blocking shutdown attempts in this phase.
        if (JVM_SHUTDOWN_IN_PROGRESS.get()) {
            LOG.debug("JVM shutdown in progress, skipping JavaFX shutdown");
            return true;
        }

        // Avoid posting back to the FX thread when already on it.
        if (Platform.isFxApplicationThread()) {
            try {
                LOG.debug("calling Platform.exit() from FX thread");
                Platform.exit();
                return true;
            } catch (IllegalStateException e) {
                LOG.debug("Platform already shut down or not started");
                return true;
            } catch (Exception e) {
                LOG.warn("Failed to shut down JavaFX platform", e);
                return false;
            }
        }

        CountDownLatch latch = new CountDownLatch(1);
        try {
            Platform.runLater(() -> {
                try {
                    LOG.debug("calling Platform.exit()");
                    Platform.exit();
                } finally {
                    latch.countDown();
                }
            });
        } catch (IllegalStateException e) {
            // platform already shut down or not started
            LOG.debug("Platform already shut down or not started");
            return true;
        }

        try {
            if (latch.await(timeout, unit)) {
                LOG.debug("Platform shutdown successful");
                return true;
            } else {
                LOG.warn("Platform shutdown timed out after {} {}", timeout, unit);
                return false;
            }
        } catch (InterruptedException e) {
            LOG.warn("Interrupted while waiting for platform shutdown", e);
            Thread.currentThread().interrupt();
            return false;
        }
    }

}
