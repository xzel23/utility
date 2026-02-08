package com.dua3.utility.fx;

import javafx.scene.Node;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

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
    private final Semaphore signal = new Semaphore(0);

    private static final int STATE_INACTIVE = 0;
    private static final int STATE_ACTIVE = 1;
    private static final int STATE_TERMINATING = 2;
    private static final int STATE_TERMINATED = 3;

    private AtomicInteger state = new AtomicInteger(STATE_INACTIVE);

    /**
     * the update task to execute.
     */
    private final Runnable task;

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

        thread.start();
    }

    /**
     * Loop of the update thread. Waits for incoming requests and calls the update task.
     */
    private void refreshLoop() {
        LOG.trace("[{}] entering refresh loop", name);

        while (true) {
            try {
                // Wait for a signal
                signal.acquire();

                int st = state.get();
                if (st == STATE_TERMINATING) break;
                if (st != STATE_ACTIVE) continue;

                long req = requestedRevision.get();
                long curr = currentRevision.get();

                if (req > curr) {
                    try {
                        task.run();
                    } catch (Exception e) {
                        LOG.warn("Task failed.", e);
                    }
                    currentRevision.set(req);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        state.set(STATE_TERMINATED);

        LOG.trace("[{}] exiting refresh loop", name);
    }

    /**
     * Stop the refresher.
     */
    public void stop() {
        int newState = state.updateAndGet(st -> Math.max(st, STATE_TERMINATING));
        LOG.trace("[{}] stop() - state changed to {}", name, newState);
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
     * Check active state.
     *
     * @return true if active
     */
    public boolean isActive() {
        return state.get() == STATE_ACTIVE;
    }

    /**
     * Set the active state.
     *
     * @param flag whether to activate or deactivate the refresher
     */
    public void setActive(boolean flag) {
        int newState = state.updateAndGet(st -> {
            int newSt = st;
            if (flag) {
                switch (st) {
                    case STATE_INACTIVE -> newSt = STATE_ACTIVE;
                    case STATE_ACTIVE -> LOG.debug("[{}] already active", name);
                    case STATE_TERMINATING, STATE_TERMINATED -> LOG.debug("[{}] already stopped", name);
                    default -> throw new IllegalStateException("invalid state: " + st);
                }
            } else {
                switch (st) {
                    case STATE_INACTIVE -> LOG.debug("[{}] already inactive", name);
                    case STATE_ACTIVE -> newSt = STATE_INACTIVE;
                    case STATE_TERMINATING, STATE_TERMINATED -> LOG.debug("[{}] already stopped", name);
                    default -> throw new IllegalStateException("invalid state: " + st);
                }
            }
            return newSt;
        });

        LOG.trace("[{}] setActive({}) - state changed to {}", name, flag, newState);
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
        if (!isActive()) {
            LOG.trace("[{}] refresh() - mot active, ignoring", name);
            return;
        }

        requestedRevision.incrementAndGet();
        signal.release(); // Wake up the thread
    }
}