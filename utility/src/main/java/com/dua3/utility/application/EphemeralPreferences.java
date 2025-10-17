package com.dua3.utility.application;

import org.jspecify.annotations.Nullable;
import java.io.Serial;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.Preferences;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * An in-memory, non-persistent implementation of {@link AbstractPreferences}.
 * This class stores preference data in memory using maps and does not persist
 * this data to any external storage. It can be used as a lightweight
 * alternative for transient preference storage.
 * <p>
 * Instances of this class represent nodes in a preference hierarchy. Each
 * node can store key-value pairs and have child nodes.
 * <p>
 * Thread-safety: all state mutations and reads are protected by a node-local
 * ReadWriteLock, and cross-node operations acquire locks in a fixed parent->child order.
 * <p>
 * This implementation overrides all abstract methods of {@link AbstractPreferences}
 * to provide in-memory functionality.
 */
public class EphemeralPreferences extends AbstractPreferences {
    @Serial
    private static final long serialVersionUID = 1L;

    private final Map<String, String> values = new HashMap<>();
    private final Map<String, EphemeralPreferences> children = new HashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * Constructs a new {@code EphemeralPreferences} instance.
     * This is an in-memory, non-persistent implementation of {@link AbstractPreferences}.
     * It is used for managing preference data within a transient, hierarchical structure.
     *
     * @param parent the parent {@code AbstractPreferences} node, or {@code null} for the root node
     * @param name   the name of this preference node, relative to its parent; for the root, use ""
     */
    public EphemeralPreferences(@Nullable AbstractPreferences parent, String name) {
        super(parent, Objects.requireNonNull(name, "name"));
    }

    @Override
    protected void putSpi(String key, String value) {
        lock.writeLock().lock();
        try {
            values.put(key, value);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    protected String getSpi(String key) {
        lock.readLock().lock();
        try {
            return values.get(key);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    protected void removeSpi(String key) {
        lock.writeLock().lock();
        try {
            values.remove(key);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    protected void removeNodeSpi() {
        // Lock order: parent (write) -> this (write) to avoid deadlocks.
        EphemeralPreferences p = (parent() instanceof EphemeralPreferences ep) ? ep : null;
        if (p != null) {
            p.lock.writeLock().lock();
        }
        lock.writeLock().lock();
        try {
            if (p != null) {
                p.children.remove(name());
            }
            // help GC by clearing this subtree eagerly
            clearSubtreeLocked();
        } finally {
            lock.writeLock().unlock();
            if (p != null) {
                p.lock.writeLock().unlock();
            }
        }
    }

    private void clearSubtree() {
        lock.writeLock().lock();
        try {
            clearSubtreeLocked();
        } finally {
            lock.writeLock().unlock();
        }
    }

    // expects write lock held
    private void clearSubtreeLocked() {
        values.clear();
        // Copy children to avoid holding references while recursing unlock/lock
        var snapshot = children.values().toArray(EphemeralPreferences[]::new);
        children.clear();
        for (EphemeralPreferences child : snapshot) {
            child.clearSubtree();
        }
    }

    @Override
    protected String[] keysSpi() {
        lock.readLock().lock();
        try {
            return values.keySet().toArray(String[]::new);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    protected String[] childrenNamesSpi() {
        lock.readLock().lock();
        try {
            return children.keySet().toArray(String[]::new);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    protected AbstractPreferences childSpi(String name) {
        lock.writeLock().lock();
        try {
            return children.computeIfAbsent(name, n -> new EphemeralPreferences(this, n));
        } finally {
            lock.writeLock().unlock();
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
     *
     * @return the root node of the ephemeral, non-persistent preferences hierarchy
     */
    public static Preferences root() {
        return new EphemeralPreferences(null, "ephemeral");
    }
}