package com.dua3.utility.application;
import org.jspecify.annotations.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.Preferences;

/**
 * An in-memory, non-persistent implementation of {@link AbstractPreferences}.
 * This class stores preference data in memory using maps and does not persist
 * this data to any external storage. It can be used as a lightweight
 * alternative for transient preference storage.
 * <p>
 * Instances of this class represent nodes in a preference hierarchy. Each
 * node can store key-value pairs and have child nodes.
 * <p>
 * Thread-safety: all state mutations and reads are guarded by the intrinsic
 * monitor provided by {@link AbstractPreferences#lock}. Locks are only ever
 * acquired in parent-before-child order to avoid deadlocks across the hierarchy.
 * <p>
 * This implementation overrides all abstract methods of {@link AbstractPreferences}
 * to provide in-memory functionality. As with {@link Preferences}, keys and values
 * must be non-null; validation is performed by {@link AbstractPreferences}.
 */
public final class EphemeralPreferences extends AbstractPreferences {
    private final Map<String, String> values = new HashMap<>();
    private final Map<String, EphemeralPreferences> children = new HashMap<>();
    /**
     * Constructs a new {@code EphemeralPreferences} instance.
     * This is an in-memory, non-persistent implementation of {@link AbstractPreferences}.
     * It is used for managing preference data within a transient, hierarchical structure.
     *
     * @param parent the parent {@code AbstractPreferences} node, or {@code null} for the root node
     * @param name   the name of this preference node, relative to its parent; for the root, use ""
     */
    private EphemeralPreferences(@Nullable AbstractPreferences parent, String name) {
        super(parent, name);
    }

    @Override
    protected void putSpi(String key, String value) {
        synchronized (lock) {
            values.put(key, value);
        }
    }

    @Override
    protected @Nullable String getSpi(String key) {
        synchronized (lock) {
            return values.get(key);
        }
    }

    @Override
    protected void removeSpi(String key) {
        synchronized (lock) {
            values.remove(key);
        }
    }

    @Override
    protected void removeNodeSpi() {
        // Remove this node from its parent first (if any) under the parent's lock,
        // then clear this subtree without holding the parent lock to avoid deadlocks.
        EphemeralPreferences p = (parent() instanceof EphemeralPreferences ep) ? ep : null;
        if (p != null) {
            synchronized (p.lock) {
                p.children.remove(name());
            }
        }
        // Now clear this node and its descendants
        clearSubtree();
    }

    private void clearSubtree() {
        synchronized (lock) {
            clearSubtreeLocked();
        }
    }

    // expects this.lock held
    private void clearSubtreeLocked() {
        values.clear();
        // Snapshot children to avoid holding references while recursing with different locks
        EphemeralPreferences[] snapshot = children.values().toArray(EphemeralPreferences[]::new);
        children.clear();
        for (var child : snapshot) {
            // Each child will acquire its own lock internally
            child.clearSubtree();
        }
    }

    @Override
    protected String[] keysSpi() {
        synchronized (lock) {
            return values.keySet().toArray(String[]::new);
        }
    }

    @Override
    protected String[] childrenNamesSpi() {
        synchronized (lock) {
            return children.keySet().toArray(String[]::new);
        }
    }

    @Override
    protected AbstractPreferences childSpi(String name) {
        synchronized (lock) {
            return children.computeIfAbsent(name, n -> new EphemeralPreferences(this, n));
        }
    }

    @Override
    protected void syncSpi() {
        // nothing to do for ephemeral preferences
    }

    @Override
    protected void flushSpi() {
        // nothing to do for ephemeral preferences
    }

    /**
     * Returns the root preferences node for the ephemeral, in-memory preference hierarchy.
     * The root node has the empty name "" as required by {@link AbstractPreferences}.
     * Each invocation returns a fresh, independent hierarchy.
     *
     * @return the root node of the ephemeral, non-persistent preferences hierarchy
     */
    public static Preferences createRoot() {
        // NOTE: the root node must be passed the empty string as name
        return new EphemeralPreferences(null, "");
    }
}