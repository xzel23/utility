package com.dua3.utility.application.imp;

import com.dua3.utility.application.DarkModeDetector;
import com.dua3.utility.lang.LangUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Set;
import java.util.function.Consumer;

/**
 * An abstract base class for detecting and handling dark mode status.
 * This class provides a foundational implementation of the {@link DarkModeDetector} interface,
 * managing listeners and notifying them of dark mode state changes.
 *
 * Subclasses should implement the specific logic for detecting dark mode and invoking
 * {@link #onChangeDetected(boolean)} when a change is detected.
 *
 * The class handles:
 * - Adding and removing listeners for dark mode state changes.
 * - Logging state changes and listener actions.
 */
public abstract class DarkModeDetectorBase implements DarkModeDetector {
    private static final Logger LOG = LogManager.getLogger(DarkModeDetectorBase.class);

    private final Set<Consumer<Boolean>> listeners = LangUtil.newWeakHashSet();

    /**
     * Adds a listener for dark mode state changes.
     * The listener is a {@link Consumer} that accepts a {@code Boolean} value representing
     * the dark mode state. A value of {@code true} indicates that dark mode is enabled,
     * and {@code false} indicates that it is disabled.
     *
     * @param listener the listener to be notified of dark mode state changes;
     *                 cannot be {@code null}
     */
    @Override
    public final void addListener(Consumer<Boolean> listener) {
        LOG.debug("addListener(): {}", System.identityHashCode(listener));
        listeners.add(listener);
        monitorSystemChanges( true);
    }

    /**
     * Removes a previously added listener for dark mode state changes.
     * If the specified listener is not present in the list, no action is taken.
     *
     * @param listener the listener to be removed; must not be null
     */
    @Override
    public final void removeListener(Consumer<Boolean> listener) {
        boolean removed = listeners.remove(listener);
        LOG.debug("removeListener(): {} - removed={}", () -> System.identityHashCode(listener), () -> removed);
        monitorSystemChanges(!listeners.isEmpty());
    }

    /**
     * Notifies all registered listeners of a detected change in the dark mode status.
     *
     * @param darkMode a boolean indicating the current state of dark mode; {@code true} if dark mode is enabled, {@code false} otherwise
     */
    protected final void onChangeDetected(boolean darkMode) {
        LOG.debug("onChangeDetected(): informing listeners, darkMode={}", darkMode);
        listeners.forEach(l -> l.accept(darkMode));
    }

    /**
     * Monitors system changes for dark mode state.
     * This method is invoked to enable or disable the monitoring of system-level events
     * that indicate a change in dark mode status.
     *
     * Subclasses must implement this method to define the specific behavior for starting
     * or stopping the monitoring functionality.
     */
    protected abstract void monitorSystemChanges(boolean enable);
}
