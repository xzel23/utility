package com.dua3.utility.application;

import com.dua3.utility.application.imp.DarkModeDetectorInstance;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.prefs.Preferences;

/**
 * Utility class providing initialization and access to application-wide components
 * such as application preferences and recently used documents.
 * <p>
 * Preferences can be explicitly initialized via {@link #initApplicationPreferences(Preferences)}.
 * If not initialized, {@link #preferences()} will lazily create an ephemeral (non-persistent)
 * root via {@code EphemeralPreferences.root()} and log a warning.
 * <p>
 * This class is thread-safe.
 */
public final class ApplicationUtil {

    private static final Logger LOG = LogManager.getLogger(ApplicationUtil.class);

    /**
     * A thread-safe reference to the application's preferences object, using an {@link AtomicReference}.
     */
    private static final AtomicReference<@Nullable Preferences> APPLICATION_PREFERENCES = new AtomicReference<>(null);

    /**
     * The current user interface (UI) mode of the application.
     * <p>
     * This variable is initialized to {@link UiMode#LIGHT} by default.
     */
    private static final AtomicReference<UiMode> uiMode = new AtomicReference<>(UiMode.LIGHT);

    /**
     * An {@link AtomicBoolean} representing whether the application is currently in dark mode.
     * <p>
     * This static variable is used internally to supervise and manage the application's dark mode state.
     * It is initialized to {@code false}, indicating that dark mode is off by default.
     * <p>
     * The value is typically updated in response to changes in the application or system UI mode and
     * cannot be modified directly.
     */
    private static final AtomicBoolean darkMode = new AtomicBoolean(false);

    /**
     * A thread-safe list of listeners to be notified about changes in the application's dark mode state.
     */
    private static final CopyOnWriteArrayList<Consumer<Boolean>> darkModeListeners = new CopyOnWriteArrayList<>();

    /**
     * A thread-safe list of listeners to be notified about changes to the application's ui mode.
     */
    private static final CopyOnWriteArrayList<Consumer<UiMode>> uiModeListeners = new CopyOnWriteArrayList<>();

    /**
     * Private utility class constructor.
     */
    private ApplicationUtil() {
        // utility class
    }

    /**
     * Initializes the application preferences exactly once.
     * Subsequent calls throw an {@link IllegalStateException}.
     *
     * @param prefs non-null preferences instance (e.g., {@link Preferences#userNodeForPackage(Class)})
     * @throws NullPointerException  if {@code prefs} is null
     * @throws IllegalStateException if already initialized
     */
    public static void initApplicationPreferences(Preferences prefs) {
        if (!APPLICATION_PREFERENCES.compareAndSet(null, prefs)) {
            LOG.warn("application preferences already initialized, ignoring second call to initApplicationPreferences(...)");
        }
    }

    /**
     * Returns the application's {@link Preferences}.
     * <p>
     * If not explicitly initialized, an ephemeral, non-persistent root is created on first access,
     * and a warning is logged. To ensure persistence, call
     * {@link #initApplicationPreferences(Preferences)} during application startup.
     *
     * @return the application's preferences (never null)
     */
    public static Preferences preferences() {
        Preferences preferences = APPLICATION_PREFERENCES.updateAndGet(prefs -> {
            if (prefs == null) {
                LOG.warn("Application preferences not initialized; using ephemeral, non-persistent preferences. "
                        + "Call ApplicationUtil.initApplicationPreferences(...) during startup to enable persistence.");
                prefs = EphemeralPreferences.createRoot();
            }
            return prefs;
        });

        assert preferences != null : "internal error, an ephemeral instance should have been created";

        return preferences;
    }

    /**
     * Returns the singleton instance of {@link RecentlyUsedDocuments}.
     *
     * @return the singleton instance
     */
    public static RecentlyUsedDocuments recentlyUsedDocuments() {
        return RecentlyUsedDocumentsHolder.RECENTLY_USED_DOCUMENTS;
    }

    /**
     * Retrieves the current UI mode of the application.
     *
     * @return the current application UI mode, which is an instance of {@link UiMode}
     */
    public static UiMode getUiMode() {
        return uiMode.get();
    }

    /**
     * Sets the application's UI mode and updates the dark mode setting accordingly.
     * If the specified UI mode differs from the previous mode, any necessary adjustments are made,
     * including determining whether the application should utilize dark mode and invoking appropriate listeners.
     *
     * @param mode the desired UI mode for the application. Must be one of {@link UiMode#DARK}, {@link UiMode#LIGHT},
     *             or {@link UiMode#SYSTEM_DEFAULT}.
     */
    public static void setUiMode(UiMode mode) {
        UiMode previousMode = uiMode.getAndSet(mode);
        if (previousMode != mode) {
            boolean dark = switch (getUiMode()) {
                case DARK -> true;
                case LIGHT -> false;
                case SYSTEM_DEFAULT -> DarkModeDetectorInstance.get().isDarkMode();
            };
            onUpdateUiMode(mode);
            setDarkMode(dark);
        }
    }

    /**
     * Notifies all registered UI mode listeners.
     *
     * @param uiMode the UiMode
     */
    private static void onUpdateUiMode(UiMode uiMode) {
        uiModeListeners.forEach(listener -> {
            try {
                listener.accept(uiMode);
            } catch (Exception ex) {
                LOG.warn("Ignoring exception while notifying listener", ex);
            }
        });
    }

    /**
     * Notifies all registered dark mode listeners.
     *
     * @param dark a boolean indicating whether dark mode is enabled (true) or disabled (false)
     */
    private static void onUpdateDarkMode(boolean dark) {
        darkModeListeners.forEach(listener -> {
            try {
                listener.accept(dark);
            } catch (Exception ex) {
                LOG.warn("Ignoring exception while notifying listener", ex);
            }
        });
    }

    /**
     * Sets the application's dark mode to the specified value.
     * <p>
     * This method updates the dark mode setting for the application and returns the previous setting.
     *
     * @param darkMode the new dark mode state to be set; {@code true} for enabling dark mode,
     *                 {@code false} for disabling it
     */
    private static void setDarkMode(boolean darkMode) {
        LOG.debug("application dark mode set to {}", darkMode);
        if (darkMode != ApplicationUtil.darkMode.compareAndExchange(!darkMode, darkMode)) {
            onUpdateDarkMode(darkMode);
        }
    }

    /**
     * Checks whether the application is currently in dark mode.
     *
     * @return {@code true} if the application is in dark mode, {@code false} otherwise
     */
    public static boolean isDarkMode() {
        return darkMode.get();
    }

    /**
     * Adds a listener to receive updates when the application's dark mode status changes.
     * The listener is a consumer that takes a boolean, where {@code true} indicates that
     * dark mode is enabled, and {@code false} indicates that dark mode is disabled.
     *
     * @param listener the listener to be notified of dark mode changes (non-null)
     */
    public static void addUiModeListener(Consumer<UiMode> listener) {
        uiModeListeners.add(listener);
    }

    /**
     * Removes a listener for dark mode changes in the application.
     *
     * @param listener the listener to be removed; typically a {@link Consumer} that processes
     *                 a {@code Boolean} indicating the dark mode state (true if dark mode is enabled, false otherwise)
     */
    public static void removeUiModeListener(Consumer<UiMode> listener) {
        uiModeListeners.remove(listener);
    }

    /**
     * Adds a listener to receive updates when the application's dark mode status changes.
     * The listener is a consumer that takes a boolean, where {@code true} indicates that
     * dark mode is enabled, and {@code false} indicates that dark mode is disabled.
     *
     * @param listener the listener to be notified of dark mode changes (non-null)
     */
    public static void addDarkModeListener(Consumer<Boolean> listener) {
        LOG.trace("Ensure DarkModeUpdater is initialised");
        DarkModeUpdater.INSTANCE.ensureRunning();
        darkModeListeners.add(listener);
    }

    /**
     * Removes a listener for dark mode changes in the application.
     *
     * @param listener the listener to be removed; typically a {@link Consumer} that processes
     *                 a {@code Boolean} indicating the dark mode state (true if dark mode is enabled, false otherwise)
     */
    public static void removeDarkModeListener(Consumer<Boolean> listener) {
        darkModeListeners.remove(listener);
    }

    /**
     * Lazy holder for the singleton {@link RecentlyUsedDocuments}.
     */
    private static final class RecentlyUsedDocumentsHolder {
        private static final RecentlyUsedDocuments RECENTLY_USED_DOCUMENTS =
                new RecentlyUsedDocuments(preferences());
    }

    /**
     * Handles the initialization and monitoring of system dark mode changes.
     * This class manages the integration between the system's dark mode setting
     * and the application's dark mode behavior by adding a listener to detect
     * changes in the system dark mode state.
     * <p>
     * The class relies on {@link DarkModeDetector} to check if dark mode detection
     * is supported and to register listeners for monitoring the system dark mode state.
     * <p>
     * It automatically calls {@link ApplicationUtil#setDarkMode(boolean)}
     * when a dark mode change is detected, allowing the application to update its
     * dark mode setting appropriately.
     */
    private static class DarkModeUpdater {
        private static final DarkModeUpdater INSTANCE = new DarkModeUpdater();

        DarkModeUpdater() {
            DarkModeDetector dmd = DarkModeDetectorInstance.get();
            if (dmd.isDarkModeDetectionSupported()) {
                LOG.debug("system dark mode detection is supported, adding listener");
                dmd.addListener(DarkModeUpdater::onSystemDarkModeChange);
            } else {
                LOG.debug("system dark mode detection is not supported");
            }
        }

        public void ensureRunning() {
            LOG.trace("DarkModeUpdater is initialised");
            // There is nothing more to do here, the instance is created when the clas is loaded.
        }

        /**
         * Updates the application dark mode setting in response to a system dark mode change event.
         * If the current UI mode is {@code SYSTEM_DEFAULT}, the application's dark mode setting is updated to match the system's setting.
         * If the current UI mode is not {@code SYSTEM_DEFAULT}, the event is ignored.
         *
         * @param dark {@code true} if the system dark mode is enabled; {@code false} otherwise
         */
        private static void onSystemDarkModeChange(boolean dark) {
            LOG.trace("system dark mode changed to {}", dark);
            if (getUiMode() == UiMode.SYSTEM_DEFAULT) {
                LOG.trace("setting application dark mode to {}", dark);
                setDarkMode(dark);
            } else {
                LOG.trace("ignoring application dark mode change, ui mode is {}", getUiMode());
            }
        }
    }

}
