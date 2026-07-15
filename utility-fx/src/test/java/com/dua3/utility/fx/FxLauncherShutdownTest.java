package com.dua3.utility.fx;

import javafx.application.Platform;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FxLauncherShutdownTest {

    @Test
    @Timeout(value = 15, unit = TimeUnit.SECONDS)
    void shutdownStopsTheJavaFxPlatform() throws InterruptedException {
        FxLauncher.run(() -> { });

        CountDownLatch beforeShutdown = new CountDownLatch(1);
        Platform.runLater(beforeShutdown::countDown);
        assertTrue(beforeShutdown.await(5, TimeUnit.SECONDS), "JavaFX platform did not start");

        FxLauncher.shutdown(5, TimeUnit.SECONDS);

        CountDownLatch afterShutdown = new CountDownLatch(1);
        try {
            Platform.runLater(afterShutdown::countDown);
        } catch (IllegalStateException ignored) {
            // The JavaFX runtime may reject work immediately after shutdown.
        }
        assertFalse(afterShutdown.await(1, TimeUnit.SECONDS), "JavaFX platform accepted work after shutdown");
    }
}
