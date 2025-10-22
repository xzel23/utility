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

    private ApplicationUtil() {
        // utility class
    }

    private static final AtomicReference<@Nullable Preferences> APPLICATION_PREFERENCES = new AtomicReference<>(null);

    private static final class DarkModeDetectorHolder {
        private static final DarkModeDetector INSTANCE = DarkModeDetectorInstance.get();
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
     * Lazy holder for the singleton {@link RecentlyUsedDocuments}.
     */
    private static final class RecentlyUsedDocumentsHolder {
        private static final RecentlyUsedDocuments RECENTLY_USED_DOCUMENTS =
                new RecentlyUsedDocuments(preferences());
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
     * Returns the singleton instance of {@link DarkModeDetector}.
     *
     * @return the singleton instance of {@link DarkModeDetector}
     */
    public static DarkModeDetector darkModeDetector() {
        return DarkModeDetectorHolder.INSTANCE;
    }

    private static final AtomicReference<UiMode> applicationUiMode = new AtomicReference<>(UiMode.LIGHT);
    private static final AtomicBoolean applicationDarkMode = new AtomicBoolean(false);

    public static UiMode getApplicationUiMode() {
        return applicationUiMode.get();
    }

    private static class DarkModeUpdater {
        private static final DarkModeUpdater INSTANCE = new DarkModeUpdater();

        DarkModeUpdater() {
            DarkModeDetector dmd = darkModeDetector();
            if (dmd.isDarkModeDetectionSupported()) {
                LOG.debug("system dark mode detection supported, adding listener");
                dmd.addListener(dark -> onSystemDarkModeChange(dark));
            } else {
                LOG.debug("system dark mode detection not supported");
            }
        }
    }

    private static void onSystemDarkModeChange(boolean dark) {
        LOG.debug("system dark mode changed to {}", dark);
        if (getApplicationUiMode() == UiMode.SYSTEM_DEFAULT) {
            LOG.debug("setting application dark mode to {}", dark);
            setApplicationDarkMode(dark);
        } else {
            LOG.debug("ignoring application dark mode change, ui mode is {}", getApplicationUiMode());
        }
    }

    public static void setApplicationUiMode(UiMode mode) {
        UiMode previousMode = applicationUiMode.getAndSet(mode);
        if (previousMode != mode) {
            boolean dark = switch (getApplicationUiMode()) {
                case DARK -> true;
                case LIGHT -> false;
                case SYSTEM_DEFAULT -> darkModeDetector().isDarkMode();
            };
            boolean previousDark = applicationDarkMode.getAndSet(dark);
            if (previousDark != dark) {
                onUpdateApplicationDarkMode(dark);
            }
        }
    }

    private static void onUpdateApplicationDarkMode(boolean dark) {
        applicationDarkModeListeners.forEach(listener -> {
            try {
                listener.accept(dark);
            } catch (Exception ex) {
                LOG.warn("Ignoring exception while notifying listener", ex);
            }
        });
    }

    private static boolean setApplicationDarkMode(boolean darkMode) {
        return applicationDarkMode.getAndSet(darkMode);
    }
    public static boolean isApplicationDarkMode() {
        return applicationDarkMode.get();
    }

    private static final CopyOnWriteArrayList<Consumer<Boolean>> applicationDarkModeListeners = new CopyOnWriteArrayList<>();

    public static void addApplicationDarkModeListener(Consumer<Boolean> listener) {
        applicationDarkModeListeners.add(listener);
    }

    public static void removeApplicationDarkModeListener(Consumer<Boolean> listener) {
        applicationDarkModeListeners.remove(listener);
    }

}
