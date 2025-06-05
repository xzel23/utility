package com.dua3.utility.fx;

import javafx.application.Platform;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

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
}