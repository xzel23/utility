package com.dua3.utility.application;

import com.dua3.utility.data.Pair;
import com.dua3.utility.io.IoUtil;
import com.dua3.utility.lang.LangUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Predicate;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * An implementation of a most-recently-used (MRU/LRU-like) list for documents with automatic storage/retrieval via the Preferences API.
 * The list keeps at most {@code capacity} entries and moves updated/added entries to the most-recent position.
 * The provided Preferences node should be dedicated to this instance because only numeric keys managed by this class are updated on changes.
 */
public final class RecentlyUsedDocuments {
    private static final Logger LOG = LogManager.getLogger(RecentlyUsedDocuments.class);

    /**
     * The default capacity for a RecentlyUsedDocuments instance.
     */
    public static final int DEFAULT_CAPACITY = 10;

    private final int capacity;
    private final LinkedHashMap<URI, String> items = new LinkedHashMap<>();
    private final Preferences prefs;

    // Use copy-on-write to avoid ConcurrentModification during callbacks.
    private final List<UpdateListener> listeners = new java.util.concurrent.CopyOnWriteArrayList<>();

    /**
     * Constructs a RecentlyUsedDocuments instance with a default capacity.
     * The specified preferences node must be exclusively used for this instance
     * as it will be cleared during updates.
     *
     * @param prefs the preferences node to store and retrieve document information
     */
    public RecentlyUsedDocuments(Preferences prefs) {
        this(prefs, DEFAULT_CAPACITY);
    }

    /**
     * Construct a new recently-used documents list.
     * NOTE: The preferences node must be exclusive for this list, or at minimum not reuse numeric keys starting at 0 used by this class.
     *
     * @param prefs    the preferences node
     * @param capacity the maximum number of entries to retain; must be positive
     */
    public RecentlyUsedDocuments(Preferences prefs, int capacity) {
        LangUtil.checkArg("capacity", v -> v > 0, capacity);
        this.capacity = capacity;
        this.prefs = prefs;
        load();
    }

    /**
     * Read items from preferences and store in list.
     */
    private void load() {
        try {
            Arrays.stream(prefs.keys())
                    // ... sort
                    .sorted(Comparator.comparing(Integer::valueOf))
                    // ... get values
                    .map(key -> prefs.get(key, ""))
                    // ... ignore empty values
                    .filter(Predicate.not(String::isEmpty))
                    // ... split into URI and name
                    .map(s -> s.split("\n", 2))
                    // ... store
                    .forEach(item -> storeItem(item[0], item.length > 1 ? item[1] : ""));

            shrinkToFit();
        } catch (BackingStoreException e) {
            LOG.warn("error loading preferences", e);
        }
    }

    /**
     * Retrieves the URI of the most recently used document in the list.
     * If the list is empty, it returns the user's home directory as a URI.
     *
     * @return the URI of the last used document if present, or the user's home directory URI if the list is empty
     */
    public URI getLastUri() {
        return items.isEmpty() ? IoUtil.getUserHome().toUri() : items.firstEntry().getKey();
    }

    /**
     * Store item. This method will not update the backing store.
     *
     * @param uriStr the item's URI as a String
     * @param name   the item's display name
     */
    private void storeItem(String uriStr, String name) {
        URI uri = IoUtil.toURI(uriStr);
        storeItem(uri, name);
    }

    /**
     * Remove excessive items from list.
     */
    private void shrinkToFit() {
        if (items.size() > capacity) {
            URI[] keys = items.keySet().toArray(URI[]::new);
            for (int i = capacity; i < keys.length; i++) {
                items.remove(keys[i]);
            }
        }
    }

    /**
     * Store item. This method will not update the backing store.
     *
     * @param uri  the item's URI
     * @param name the item's display name; if empty, the path of the URI will be used
     * @return true, if item was added
     */
    private boolean storeItem(URI uri, String name) {
        if (name.isEmpty()) {
            //noinspection AssignmentToMethodParameter
            name = uri.getPath();
        }

        if (name == null || name.isBlank()) {
            return false;
        }

        // remove before put to force update the entry's position in the list.
        items.remove(uri);
        items.putFirst(uri, name);

        return true;
    }

    /**
     * Add update listener.
     *
     * @param listener the update listener
     */
    public void addUpdateListener(UpdateListener listener) {
        listeners.add(listener);
    }

    /**
     * Remove update listener.
     *
     * @param listener the listener to remove
     */
    public void removeUpdateListener(UpdateListener listener) {
        listeners.remove(listener);
    }

    /**
     * Put document into list (if not present) or update its position in the list (if present).
     *
     * @param uri  the document's URI
     * @param name the document's display name
     */
    public void put(URI uri, String name) {
        if (storeItem(uri, name)) {
            shrinkToFit();
            changed();
        }
    }

    /**
     * Adds a document to the recently used documents list using the provided URI.
     * If the URI is not already present in the list, it is added. If it is already present,
     * its position in the list is updated. The display name is derived from the URI's path.
     *
     * @param uri the document's URI
     */
    public void put(URI uri) {
        String name = uri.getPath().substring(uri.getPath().lastIndexOf('/') + 1);
        put(uri, name);
    }

    /**
     * Update preferences and inform listeners about update of list.
     */
    private void changed() {
        store();
        for (var listener : listeners) {
            try {
                listener.onUpdate(this);
            } catch (RuntimeException ex) {
                LOG.warn("Listener threw exception", ex);
            }
        }
    }

    /**
     * Store current list in preferences.
     */
    private void store() {
        try {
            prefs.clear();
            int idx = 0;
            for (var entry : items.entrySet()) {
                prefs.put(String.valueOf(idx++), entry.getKey() + "\n" + entry.getValue());
            }
            prefs.flush();
        } catch (BackingStoreException e) {
            LOG.warn("error storing preferences", e);
        }
    }

    /**
     * Clear the list.
     */
    public void clear() {
        items.clear();
        changed();
    }

    /**
     * Get list of items. The returned list is not backed by the recent document list (i.e. changes do not write through).
     *
     * @return the list of stored items
     */
    public List<Pair<URI, String>> entries() {
        return items.entrySet().stream().map(Pair::of).toList();
    }

    @Override
    public String toString() {
        return "RecentlyUsedDocuments{" +
                "capacity=" + capacity +
                ", items=" + items +
                '}';
    }

    /**
     * Represents a listener that receives notification when the recently used documents list is updated.
     */
    @FunctionalInterface
    public interface UpdateListener {
        /**
         * Called when the recently used documents list is updated.
         *
         * @param source the source RecentlyUsedDocuments that was updated
         */
        void onUpdate(RecentlyUsedDocuments source);
    }
}