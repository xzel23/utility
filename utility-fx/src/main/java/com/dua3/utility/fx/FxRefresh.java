package com.dua3.utility.fx;

import javafx.scene.Node;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A class intended for controlling possibly long-running update operations. Refreshs happen mutually exclusive, i.e.,
 * the update tasks do not have to be explicitly synchronized as long as not called directly from other code.
 * An example is updating a JavaFX node. I.e., if redraw requests come in before the current drawing finishes,
 * the application becomes sluggish or burns CPU cycles for drawing outdated data. The FxRefresher automatically
 * skips intermediate frames if redraw requests come in too fast for the drawing to come up with.
 */
public final class FxRefresh {
    private static final Logger LOG = LogManager.getLogger(FxRefresh.class);
    private static final long MAX_WAIT_MILLISECONDS = 1000;

    /**
     * The instance name (used in logging).
     */
    private final String name;

    /**
     * The revision number of the last completed update operation.
     */
    private final AtomicLong currentRevision = new AtomicLong();
    /**
     * The revision number of the last request. {@code currentRevision < requestedRevision} implies a pending update.
     */
    private final AtomicLong requestedRevision = new AtomicLong();

    // synchronization
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition trigger = lock.newCondition();

    /**
     * The active state. Refresh requests in inactive state are put on hold until activated again.
     */
    private final AtomicBoolean active = new AtomicBoolean(false);
    /**
     * the update task to execute.
     */
    private final Runnable task;
    /**
     * The update thread, null if not running or stop requested.
     */
    private volatile @Nullable Thread updateThread;

    /**
     * Constructor.
     *
     * @param name the name for the instance
     * @param task the task to call when refreshing
     */
    private FxRefresh(String name, Runnable task) {
        this.name = name;
        this.task = task;

        Thread thread = new Thread(this::refreshLoop);
        thread.setDaemon(true);
        this.updateThread = thread;

        thread.start();
    }

    /**
     * Loop of the update thread. Waits for incoming requests and calls the update task.
     */
    private void refreshLoop() {
        LOG.trace("[{}] entering refresh loop", name);

        while (true) {
            lock.lock();
            try {
                // Wait for conditions to be met: thread not stopped, active, and new revision requested
                while (updateThread != null && (!active.get() || requestedRevision.get() <= currentRevision.get())) {
                    if (!trigger.await(MAX_WAIT_MILLISECONDS, TimeUnit.MILLISECONDS)) {
                        LOG.trace("[{}] timeout, force check", name);
                    }
                }

                // Check if we should exit
                if (updateThread == null) {
                    break;
                }

                // Execute task if still active and there's a new revision
                if (active.get()) {
                    long myRevision = requestedRevision.get();
                    if (myRevision > currentRevision.get()) {
                        try {
                            LOG.trace("[{}] starting refresh with revision: {}", name, myRevision);
                            task.run();
                            LOG.trace("[{}] refreshed to revision: {}", name, myRevision);
                        } catch (Exception e) {
                            LOG.warn("task aborted, exception swallowed, current revision {} might have inconsistent state", myRevision, e);
                        }
                        currentRevision.set(myRevision);
                    } else {
                        LOG.trace("[{}] already at revision: {}", name, myRevision);
                    }
                }
            } catch (InterruptedException e) {
                LOG.trace("[{}] interrupted", name);
                Thread.currentThread().interrupt();
                break;
            } finally {
                lock.unlock();
            }
        }

        LOG.trace("[{}] exiting refresh loop", name);
    }

    /**
     * Stop the refresher.
     */
    public void stop() {
        LOG.trace("[{}] stopping", name);
        lock.lock();
        try {
            active.set(false);
            this.updateThread = null;
            trigger.signalAll();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Create new instance. The initial state is active.
     *
     * @param name the name for the instance
     * @param task the task to call when refreshing
     * @return new instance
     */
    public static FxRefresh create(String name, Runnable task) {
        return create(name, task, true);
    }

    /**
     * Create new instance.
     *
     * @param name   the name for the instance
     * @param task   the task to call when refreshing
     * @param active the initial active state
     * @return new instance
     */
    public static FxRefresh create(String name, Runnable task, boolean active) {
        FxRefresh r = new FxRefresh(name, task);
        r.setActive(active);
        return r;
    }

    /**
     * Create a refresher instance for a JavaFX {@link Node}.
     * The refresher will prevent updates when the node is hidden. The refresher is stopped when the node is
     * removed from the scene graph. The initial state is active.
     *
     * @param name the refresher name
     * @param task the task to run on refresh
     * @param node the node associated with this refresher
     * @return new instance
     */
    public static FxRefresh create(String name, Runnable task, Node node) {
        return create(name, task, node, true);
    }

    /**
     * Create a refresher instance for a JavaFX {@link Node}.
     * The refresher will prevent updates when the node is hidden. The refresher is stopped when the node is
     * removed from the scene graph.
     *
     * @param name   the refresher name
     * @param task   the task to run on refresh
     * @param node   the node associated with this refresher
     * @param active the initial active state
     * @return new instance
     */
    public static FxRefresh create(String name, Runnable task, Node node, boolean active) {
        FxRefresh r = new FxRefresh(name, () -> {
            if (!node.isVisible()) {
                LOG.trace("node is not visible, update skipped");
                return;
            }

            task.run();
        });

        // stop update thread when node is removed from scene graph
        node.parentProperty().addListener((v, o, n) -> {
            if (n == null) {
                LOG.trace("node was removed from parent, stopping refresher");
                r.stop();
            }
        });

        node.sceneProperty().addListener((v, o, n) -> {
            if (n == null) {
                LOG.trace("node was removed from scene graph, stopping refresher");
                r.stop();
            }
        });

        r.setActive(active);

        return r;
    }

    /**
     * Check if the refresher has been started. Note that it's possible that the refresher is running,
     * but inactive, i.e., if the window is hidden.
     *
     * @return true, if the refresher is running
     */
    public boolean isRunning() {
        lock.lock();
        try {
            return updateThread != null;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Check active state.
     *
     * @return true if active
     */
    public boolean isActive() {
        return active.get();
    }

    /**
     * Set the active state.
     *
     * @param flag whether to activate or deactivate the refresher
     */
    public void setActive(boolean flag) {
        LOG.trace("[{}] setActive({})", name, flag);
        lock.lock();
        try {
            active.set(flag);
            trigger.signalAll();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Request refresh. The refresh will be performed as soon as all the following are met:
     * <ul>
     *     <li>the refresher is running
     *     <li>the refresher's state is "active"
     *     <li>no other request is running
     *     <li>no newer request was queued (in that case, the older request will be skipped)
     * </ul>
     */
    public void refresh() {
        LOG.trace("[{}] refresh requested", name);
        lock.lock();
        try {
            if (updateThread == null) {
                return;
            }
            long revision = requestedRevision.incrementAndGet();
            LOG.trace("[{}] requested revision {}", name, revision);
            trigger.signalAll();
        } finally {
            lock.unlock();
        }
    }
}