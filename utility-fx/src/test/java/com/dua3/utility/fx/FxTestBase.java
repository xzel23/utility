package com.dua3.utility.fx;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Base class for JavaFX tests.
 * <p>
 * This class handles the initialization and shutdown of the JavaFX platform.
 * It ensures that the platform is initialized before any tests run and is not
 * shut down between test classes, allowing multiple JavaFX test classes to run
 * in sequence.
 */
public abstract class FxTestBase {

    private static boolean platformInitialized = false;
    private static Stage sharedStage;
    private static final Object lock = new Object();

    /**
     * Initialize the JavaFX platform if it's not already initialized.
     * This method is synchronized to prevent multiple concurrent initializations.
     */
    @BeforeAll
    public static void initializePlatform() {
        synchronized (lock) {
            if (!platformInitialized) {
                try {
                    Platform.startup(() -> {
                        System.out.println("JavaFX Platform initialized");
                    });
                    platformInitialized = true;
                    PlatformHelper.runAndWait(() -> {
                        sharedStage = new Stage();
                        sharedStage.setTitle("FxTestBase");
                        sharedStage.setWidth(800);
                        sharedStage.setHeight(600);
                    });
                } catch (IllegalStateException e) {
                    // Platform already running, which is fine
                    System.out.println("JavaFX Platform was already running");
                    platformInitialized = true;
                }
            }
        }
    }

    /**
     * This method intentionally does not call Platform.exit().
     * The platform will be shut down when the JVM exits.
     * This allows multiple JavaFX test classes to run in sequence.
     */
    @AfterAll
    public static void cleanupPlatform() {
        // Intentionally empty - we don't want to shut down the platform between test classes
        System.out.println("JavaFX test completed, keeping platform running for subsequent tests");
    }

    /**
     * Helper method to run code on the JavaFX Application Thread and wait for it to complete.
     * Any {@link Exception} thrown will be rethrown on the calling thread.
     *
     * @param runnable The code to run
     * @throws Exception If the thread is interrupted while waiting
     */
    public static void runOnFxThreadAndWait(Runnable runnable) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);

        AtomicReference<Throwable> exception = new AtomicReference<>();
        PlatformHelper.runLater(() -> {
            try {
                runnable.run();
            } catch (Throwable t) {
                // we need to catch Throwable so that AssertionErrors thrown by JUnit asseertions don't get swallowed by JavaFX
                exception.set(t);
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(20, TimeUnit.SECONDS), "JavaFX operation timed out");
        Throwable throwable = exception.get();
        if (throwable != null) {
            if (throwable instanceof RuntimeException re) {
                throw re;
            } else if (throwable instanceof Error e) {
                throw e;
            } else {
                throw new RuntimeException(throwable);
            }
        }
    }

    /**
     * Helper method to add a node to a Scene and Stage.
     *
     * @param node the node to add to a scene
     * @return the created Scene containing the PinBoard
     */
    public static Scene addToScene(Node node) {
        StackPane root = new StackPane();
        root.getChildren().add(node);
        Scene scene = new Scene(root, 800, 600);

        // Use the shared Stage to ensure the skin is initialized
        PlatformHelper.runAndWait(() -> {
            // Set the scene on the shared stage
            sharedStage.setScene(scene);

            // Make sure the stage is showing
            if (!sharedStage.isShowing()) {
                sharedStage.show();
            }

            // Process a pulse to ensure the scene graph is processed
            Platform.requestNextPulse();
        });

        return scene;
    }
}