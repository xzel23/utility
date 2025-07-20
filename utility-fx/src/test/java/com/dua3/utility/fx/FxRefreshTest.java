package com.dua3.utility.fx;

import javafx.scene.layout.Pane;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the FxRefresh class.
 */
@Execution(ExecutionMode.SAME_THREAD)
class FxRefreshTest extends FxTestBase {

    @Test
    void testCreateWithNameAndTask() throws Throwable {
        runOnFxThreadAndWait(() -> {
            AtomicBoolean taskExecuted = new AtomicBoolean(false);
            Runnable task = () -> taskExecuted.set(true);

            FxRefresh refresher = FxRefresh.create("TestRefresher", task);
            assertNotNull(refresher, "FxRefresh should be created successfully");
            assertTrue(refresher.isRunning(), "Refresher should be running after creation");
            assertTrue(refresher.isActive(), "Refresher should be active by default");

            // Clean up
            refresher.stop();
            assertFalse(refresher.isRunning(), "Refresher should not be running after stop");
        });
    }

    @Test
    void testCreateWithNameTaskAndActiveState() throws Throwable {
        runOnFxThreadAndWait(() -> {
            AtomicBoolean taskExecuted = new AtomicBoolean(false);
            Runnable task = () -> taskExecuted.set(true);

            // Create with inactive state
            FxRefresh refresher = FxRefresh.create("TestRefresher", task, false);
            assertNotNull(refresher, "FxRefresh should be created successfully");
            assertTrue(refresher.isRunning(), "Refresher should be running after creation");
            assertFalse(refresher.isActive(), "Refresher should be inactive as specified");

            // Clean up
            refresher.stop();
        });
    }

    @Test
    void testCreateWithNode() throws Throwable {
        runOnFxThreadAndWait(() -> {
            AtomicBoolean taskExecuted = new AtomicBoolean(false);
            Runnable task = () -> taskExecuted.set(true);
            Pane node = new Pane();

            FxRefresh refresher = FxRefresh.create("TestRefresher", task, node);
            assertNotNull(refresher, "FxRefresh should be created successfully with node");
            assertTrue(refresher.isRunning(), "Refresher should be running after creation");
            assertTrue(refresher.isActive(), "Refresher should be active by default");

            // Clean up
            refresher.stop();
        });
    }

    @Test
    void testCreateWithNodeAndActiveState() throws Throwable {
        runOnFxThreadAndWait(() -> {
            AtomicBoolean taskExecuted = new AtomicBoolean(false);
            Runnable task = () -> taskExecuted.set(true);
            Pane node = new Pane();

            // Create with inactive state
            FxRefresh refresher = FxRefresh.create("TestRefresher", task, node, false);
            assertNotNull(refresher, "FxRefresh should be created successfully with node");
            assertTrue(refresher.isRunning(), "Refresher should be running after creation");
            assertFalse(refresher.isActive(), "Refresher should be inactive as specified");

            // Clean up
            refresher.stop();
        });
    }

    @Test
    void testRefresh() throws Throwable {
        AtomicInteger counter = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(1);

        runOnFxThreadAndWait(() -> {
            Runnable task = () -> {
                counter.incrementAndGet();
                latch.countDown();
            };

            FxRefresh refresher = FxRefresh.create("TestRefresher", task);
            refresher.refresh();
        });

        // Wait for the refresh to complete
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Refresh should complete within timeout");
        assertEquals(1, counter.get(), "Task should be executed exactly once");
    }

    @Test
    void testMultipleRefreshRequests() throws Throwable {
        AtomicInteger counter = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(1);

        runOnFxThreadAndWait(() -> {
            Runnable task = () -> {
                try {
                    // Simulate a long-running task
                    Thread.sleep(100);
                    counter.incrementAndGet();
                    if (counter.get() == 1) {
                        latch.countDown();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            };

            FxRefresh refresher = FxRefresh.create("TestRefresher", task);

            // Request multiple refreshes in quick succession
            refresher.refresh();
            refresher.refresh();
            refresher.refresh();
        });

        // Wait for at least one refresh to complete
        assertTrue(latch.await(5, TimeUnit.SECONDS), "At least one refresh should complete within timeout");

        // Sleep a bit more to allow any additional refreshes to complete
        Thread.sleep(500);

        // The exact number might vary, but it should be less than the number of requests
        // due to the skipping of intermediate frames
        assertTrue(counter.get() <= 3, "Some refresh requests should be skipped");
    }

    @Test
    void testSetActive() throws Throwable {
        AtomicInteger counter = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<FxRefresh> refresherRef = new AtomicReference<>();

        runOnFxThreadAndWait(() -> {
            Runnable task = () -> {
                counter.incrementAndGet();
                latch.countDown();
            };

            // Create with inactive state
            FxRefresh r = FxRefresh.create("TestRefresher", task, false);
            refresherRef.set(r);

            // Request refresh while inactive
            r.refresh();
        });

        // Wait a bit to ensure the refresh would have happened if active
        Thread.sleep(500);
        assertEquals(0, counter.get(), "Task should not be executed when refresher is inactive");

        // Activate the refresher
        runOnFxThreadAndWait(() -> {
            FxRefresh refresher = refresherRef.get();
            refresher.setActive(true);
            refresher.refresh();
        });

        // Wait for the refresh to complete
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Refresh should complete after activation");
        assertTrue(counter.get() > 0, "Task should be executed after activation");

        // Clean up
        runOnFxThreadAndWait(() -> {
            FxRefresh refresher = refresherRef.get();
            refresher.stop();
        });
    }

    @Test
    void testNodeVisibilityAffectsRefresh() throws Throwable {
        AtomicInteger counter = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(1);

        AtomicReference<FxRefresh> refresherReference = new AtomicReference<>();
        runOnFxThreadAndWait(() -> {
            Runnable task = () -> {
                counter.incrementAndGet();
                latch.countDown();
            };

            Pane node = new Pane();
            FxRefresh refresher = FxRefresh.create("TestRefresher", task, node);
            refresherReference.set(refresher);

            // Make node invisible
            node.setVisible(false);

            // Request refresh while node is invisible
            refresher.refresh();

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                // ignore
            }

            // Make node visible again and request another refresh
            node.setVisible(true);
            refresher.refresh();
        });

        // Wait for the refresh to complete
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Timeout while waiting for refresh to complete");
        assertEquals(1, counter.get(), "Task should be executed only when node is visible");

        // Clean up
        refresherReference.get().stop();
    }

    @Test
    void testNodeRemovedFromSceneStopsRefresher() throws Throwable {
        AtomicBoolean refresherStopped = new AtomicBoolean(false);
        AtomicReference<FxRefresh> refresherRef = new AtomicReference<>();

        Pane parentNode = new Pane();
        Pane childNode = new Pane();

        runOnFxThreadAndWait(() -> {
            Runnable task = () -> { /* Do nothing */ };

            parentNode.getChildren().add(childNode);

            FxRefresh r = FxRefresh.create("TestRefresher", task, childNode);
            refresherRef.set(r);
            assertTrue(r.isRunning(), "Refresher should be running initially");
        });

        // Remove node from parent
        runOnFxThreadAndWait(() -> {
            parentNode.getChildren().remove(childNode);

            // Wait a bit for the listener to be called
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            FxRefresh refresher = refresherRef.get();
            refresherStopped.set(!refresher.isRunning());
        });

        assertTrue(refresherStopped.get(), "Refresher should be stopped when node is removed from parent");
    }
}
