package com.dua3.utility.application;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.atomic.AtomicReference;
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

    private static final AtomicReference<@Nullable Preferences> applicationPreferences = new AtomicReference<>(null);

    /**
     * Initializes the application preferences exactly once.
     * Subsequent calls throw an {@link IllegalStateException}.
     *
     * @param prefs non-null preferences instance (e.g., {@link Preferences#userNodeForPackage(Class)})
     * @throws NullPointerException  if {@code prefs} is null
     * @throws IllegalStateException if already initialized
     */
    public static void initApplicationPreferences(Preferences prefs) {
        if (!applicationPreferences.compareAndSet(null, prefs)) {
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
        Preferences preferences = applicationPreferences.updateAndGet(prefs -> {
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
}